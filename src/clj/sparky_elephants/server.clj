(ns sparky-elephants.server
  (:require sparky-elephants.handler
            [aleph.http :as http])
  (:gen-class))

(defn create-server
  [port & [{:keys [handler] :or {handler sparky-elephants.handler/app}}]]
  (http/start-server handler {:port port}))

(defonce server-atom (atom nil))

(defn start-or-restart-server
  [port & [opts]]
  (swap! server-atom (fn [old-server]
                       (when old-server (.close old-server))
                       (create-server port opts))))

(defn stop-server
  []
  (swap! server-atom (fn [old-server]
                       (when old-server (.close old-server))
                       nil)))

 (defn -main [& args]
   (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
     (start-or-restart-server port)))
