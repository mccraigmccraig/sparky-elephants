(ns ^:figwheel-no-load sparky-elephants.dev
  (:require [sparky-elephants.core :as core]
            [figwheel.client :as figwheel :include-macros true]
            [weasel.repl :as weasel]
            [reagent.core :as r]))

(enable-console-print!)

(figwheel/watch-and-reload
  :websocket-url "ws://localhost:3449/figwheel-ws"
  :jsload-callback core/mount-root)

;; figwheel runs a repl... don't need weasel repl too
;; (weasel/connect "ws://localhost:9001" :verbose true)

(core/init!)
