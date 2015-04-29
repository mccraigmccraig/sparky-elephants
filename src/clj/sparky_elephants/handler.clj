(ns sparky-elephants.handler
  (:require [plumbing.core :refer [assoc-when]]
            [clojure.tools.logging :as log]
            [cognitect.transit :as transit]
            [aleph.http :as http]
            [manifold.stream :as s]
            [clj-kafka.consumer.zk :as kcon]
            [clj-kafka.core :as k]
            [clj-kafka.zk :as kzk]
            [clj-kafka.producer :as kprod]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [selmer.parser :refer [render-file]]
            [prone.middleware :refer [wrap-exceptions]]
            [environ.core :refer [env]])
  (:import [java.io ByteArrayOutputStream ByteArrayInputStream]))

(defn ->transit-json
  "convert a value to a transit-json string"
  [v]
  (let [out (ByteArrayOutputStream.)
        writer (transit/writer out :json)]
    (transit/write writer v)
    (.toString out)))

(defn transit-json->
  "read a transit-json string"
  [s]
  (let [in (ByteArrayInputStream. (.getBytes s))
        reader (transit/reader in :json)]
    (transit/read reader)))

(defn error-response
  [msg]
  {:status 400
   :headers {"content-type" "application/text"}
   :body msg})

(def non-websocket-request
  (error-response "Expected a websocket request."))

(def no-user-id
  (error-response "No user id."))

(defn echo-handler
  [req]
  (if-let [socket (try
                    @(http/websocket-connection req)
                    (catch Exception e
                      nil))]
    (s/connect socket socket)
    non-websocket-request))

(def kafka-consumer-config {"zookeeper.connect" "localhost:2181"
                            "group.id" "clj-kafka.consumer" ;; default consumer group : client can override
                            "auto.offset.reset" "smallest"
                            "auto.commit.enable" "true"})

(def kafka-producer-config {"metadata.broker.list" "localhost:9092"
                            "serializer.class" "kafka.serializer.DefaultEncoder"
                            "partitioner.class" "kafka.producer.DefaultPartitioner"})

(defonce kafka-producer (atom nil))

(defn get-kafka-producer
  []
  (swap! kafka-producer
         (fn [old-producer]
           (if old-producer
             old-producer
             (kprod/producer kafka-producer-config)))))

(defn extract-destinations
  [message]
  (some->> message
           (re-seq #"@(\S+)")
           (map last)))

(defn send-message
  [message]
  (doseq [user-id (extract-destinations message)]
    (kprod/send-message (get-kafka-producer) (kprod/message (str "mailbox-" user-id) (.getBytes message)))))

(defn mailbox-handler
  [req]
  (if-let [user-id (get-in req [:params :id])]
    (if-let [socket (try @(http/websocket-connection req)
                         (catch Exception e nil))]
      (do
        (let [client-uuid (get-in req [:params :client-uuid])
              _ (log/info (str "ABOUT TO CONNECT MAILBOX: " user-id ", CLIENT-UUID: " client-uuid))
              kc (kcon/consumer (assoc-when kafka-consumer-config "group.id" (when client-uuid (str "client-" client-uuid))))]
          (try
            (let [msg-seq (kcon/messages kc (str "mailbox-" user-id))
                  str-seq (map (fn [m] (-> m :value String. ->transit-json)) msg-seq)
                  value-stream (s/->source str-seq)]

              (s/on-closed socket (fn []
                                    (log/info (str "CLOSING MAILBOX CONSUMER: " user-id))
                                    (kcon/shutdown kc)))

              (s/connect value-stream socket)

              (s/consume (fn [m]
                           (let [v (transit-json-> m)]
                             (log/info (str "RX: " v))
                             (send-message v)))
                         socket)

              (log/info (str "CONNECTED MAILBOX: " user-id)))

            (catch Throwable e
              (log/info (str "CLOSING MAILBOX CONSUMER ON ERROR: " user-id))
              (kcon/shutdown kc)))))

      non-websocket-request)
    no-user-id))


(defroutes routes
  (GET "/" [] (render-file "templates/index.html" {:dev (env :dev?)}))
  (GET "/echo" [] echo-handler)
  (GET "/mailbox/:id" [] mailbox-handler)
  (resources "/")
  (not-found "Not Found"))

(def app
  (let [handler (wrap-defaults routes site-defaults)]
    (if (env :dev?) (wrap-exceptions handler) handler)))
