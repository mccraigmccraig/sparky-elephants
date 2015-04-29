(ns sparky-elephants.server
  (:require [sparky-elephants.handler :refer [app]]
            [aleph.http :as http])
  (:gen-class))

(defn create-server
  [port]
  (http/start-server app {:port port}))

(defonce server-atom (atom nil))

(defn start-or-restart-server
  [port]
  (swap! server-atom (fn [old-server]
                       (when old-server (.close old-server))
                       (create-server port))))

 (defn -main [& args]
   (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
     (start-or-restart-server port)))
