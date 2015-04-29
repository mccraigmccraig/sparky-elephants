(defproject sparky-elephants "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/clj" "src/cljs"]

  :dependencies [[org.clojure/clojure "1.7.0-beta2"]

                 [ch.qos.logback/logback-classic "1.1.3"]
                 [org.slf4j/slf4j-api "1.7.12"]
                 [org.slf4j/jcl-over-slf4j "1.7.12"]
                 [org.slf4j/log4j-over-slf4j "1.7.12"]
                 [org.slf4j/jul-to-slf4j "1.7.12"]
                 [org.clojure/tools.logging "0.3.1"]

                 [ring-server "0.4.0"]
                 [cljsjs/react "0.13.1-0"]
                 [reagent "0.5.0"]
                 [reagent-forms "0.5.0"]
                 [reagent-utils "0.1.4"]
                 [org.clojure/clojurescript "0.0-3196" :scope "provided"]
                 [ring "1.3.2"]
                 [ring/ring-defaults "0.1.4"]
                 [prone "0.8.1"]
                 [compojure "1.3.3"]

                 ;; additional clojure libs
                 [prismatic/plumbing "0.4.2"] ;; utils
                 [aleph "0.4.0"] ;; async webserver
                 [clj-kafka "0.2.8-0.8.1.1"] ;; simple kafka client
                 [com.cognitect/transit-clj "0.8.271"]

                 ;; clojurescript libs
                 [selmer "0.8.2"]
                 [environ "1.0.0"]
                 [secretary "1.2.3"]

                 ;; additional clojurescript libs
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"] ;; async
                 [jarohen/chord "0.6.0"] ;; websockets
                 [com.lucasbradstreet/cljs-uuid-utils "1.0.1"] ;; uuids
                 ]

  :plugins [[lein-cljsbuild "1.0.4"]
            [lein-environ "1.0.0"]
            [lein-ring "0.9.1"]
            [lein-asset-minifier "0.2.2"]]

  :ring {:handler sparky-elephants.handler/app
         :uberwar-name "sparky-elephants.war"}

  :min-lein-version "2.5.0"

  :uberjar-name "sparky-elephants.jar"

  :main sparky-elephants.server

  :clean-targets ^{:protect false} ["resources/public/js"]

  :minify-assets
  {:assets
    {"resources/public/css/site.min.css" "resources/public/css/site.css"}}

  :cljsbuild {:builds {:app {:source-paths ["src/cljs"]
                             :compiler {:output-to     "resources/public/js/app.js"
                                        :output-dir    "resources/public/js/out"
                                        :asset-path   "js/out"
                                        :optimizations :none
                                        :pretty-print  true}}}}

  :profiles {:dev {:repl-options {:init-ns sparky-elephants.repl
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

                   :dependencies [[ring-mock "0.1.5"]
                                  [ring/ring-devel "1.3.2"]
                                  [leiningen "2.5.1"]
                                  [figwheel "0.2.7"]
                                  [weasel "0.6.0"]
                                  [com.cemerick/piggieback "0.2.1"]
                                  [org.clojure/tools.nrepl "0.2.10"]
                                  [pjstadig/humane-test-output "0.7.0"]]

                   :source-paths ["env/dev/clj"]
                   :plugins [[lein-figwheel "0.2.7"]]

                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :figwheel {:http-server-root "public"
                              :server-port 3449
                              :css-dirs ["resources/public/css"]
                              :ring-handler sparky-elephants.handler/app}

                   :env {:dev? true}

                   :cljsbuild {:builds {:app {:source-paths ["env/dev/cljs"]
                                              :compiler {:main "sparky-elephants.dev"
                                                         :source-map true}}
                                        }
                               }}

             :uberjar {:hooks [leiningen.cljsbuild minify-assets.plugin/hooks]
                       :env {:production true}
                       :aot :all
                       :omit-source true
                       :cljsbuild {:jar true
                                   :builds {:app
                                             {:source-paths ["env/prod/cljs"]
                                              :compiler
                                              {:optimizations :advanced
                                               :pretty-print false}}}}}})
