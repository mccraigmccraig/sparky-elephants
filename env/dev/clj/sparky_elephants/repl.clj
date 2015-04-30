(ns sparky-elephants.repl
  (:require sparky-elephants.wrap-reload
            sparky-elephants.handler
            sparky-elephants.server
            [ring.middleware file-info file])
  (:use sparky-elephants.handler
        sparky-elephants.dev))

(def middleware
  [['ring.middleware.file/wrap-file "resources"]
   ['ring.middleware.file-info/wrap-file-info]])

(defn compile-app
  []
  (reduce (fn [a [h & args]]
            (apply (resolve h) a args))
          sparky-elephants.handler/app
          middleware))

(defn recompile-handler
  "a handler which recompiles the webapp from config on each request"
  []
  (fn [request]
    (let [handler (compile-app)]
      (handler request))))

(defn reload-handler
  []
  (sparky-elephants.wrap-reload/wrap-reload (recompile-handler)
                                            {:dirs ["src/clj"]
                                             :additional-ns ['sparky-elephants.handler]}))

(defn start-server
  "used for starting the server in development mode from REPL"
  [& [port]]
  (let [port (if port (Integer/parseInt port) 10000)]
    (sparky-elephants.server/start-or-restart-server port {:handler (reload-handler)})
    (println (str "You can view the site at http://localhost:" port))))

(defn stop-server []
  (sparky-elephants.server/stop-server))
