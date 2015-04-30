(ns sparky-elephants.wrap-reload
  (:use [ns-tracker.core :only (ns-tracker)])
  (:require [clojure.tools.logging :as log]))

(defn wrap-reload
  "Reload namespaces of modified files before the request is passed to the
  supplied handler.

  Takes the following options:
  :dirs - A list of directories that contain the source files.
  Defaults to [\"src\"].
  :additional-ns - A list of namespaces to be recompiled in the case
  that any source files have been modified"
  [handler & [{:as options}]]
  (let [source-dirs (:dirs options ["src/clj"])
        modified-namespaces (ns-tracker source-dirs)
        additional-ns (:additional-ns options []) ]
    (fn [request]
      (when-let [mns (some-> (modified-namespaces)
                             not-empty
                             (concat additional-ns)
                             set)]
        (log/info (str "reloading: " (prn-str mns)))
        (doseq [ns-sym mns]
          (require ns-sym :reload)))
      (handler request))))
