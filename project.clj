(defproject clojure-101 "0.1.0-SNAPSHOT"
  :description "Example code for Clojure and Clojurescript."
  :url "http://github.com/chrishowejones/clojure-101"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [ring-server "0.5.0"]
                 [reagent "0.7.0"]
                 [reagent-utils "0.2.1"]
                 [ring "1.6.2"]
                 [ring/ring-defaults "0.3.1"]
                 [compojure "1.6.0"]
                 [hiccup "1.0.5"]
                 [yogthos/config "0.9"]
                 [org.clojure/clojurescript "1.9.946"
                  :scope "provided"]
                 [secretary "1.2.3"]
                 [venantius/accountant "0.2.4"
                  :exclusions [org.clojure/tools.reader]]
                 [cheshire "5.8.0"]
                 [org.clojure/core.logic "0.8.11"
                  :exclusions [org.clojure/clojure]]
                 [org.clojure/spec.alpha "0.2.168"]]

  :plugins [[lein-environ "1.0.2"]
            [lein-cljsbuild "1.1.5"]
            [lein-asset-minifier "0.2.7"
             :exclusions [org.clojure/clojure]]]

  :ring {:handler clojure-101.handler/app
         :uberwar-name "clojure-101.war"}

  :min-lein-version "2.5.0"

  :uberjar-name "clojure-101.jar"

  :main clojure-101.server

  :clean-targets ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]]

  :source-paths ["src/clj" "src/cljc"]
  :resource-paths ["resources" "target/cljsbuild"]

  :minify-assets
  {:assets
   {"resources/public/css/site.min.css" "resources/public/css/site.css"}}

  :cljsbuild
  {:builds {:min
            {:source-paths ["src/cljs" "src/cljc" "env/prod/cljs"]
             :compiler
             {:output-to        "target/cljsbuild/public/js/app.js"
              :output-dir       "target/cljsbuild/public/js"
              :source-map       "target/cljsbuild/public/js/app.js.map"
              :optimizations :advanced
              :pretty-print  false}}
            :app
            {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
             :figwheel {:on-jsload "clojure-101.core/mount-root"}
             :compiler
             {:main "clojure-101.dev"
              :asset-path "/js/out"
              :output-to "target/cljsbuild/public/js/app.js"
              :output-dir "target/cljsbuild/public/js/out"
              :source-map true
              :optimizations :none
              :pretty-print  true}}


            :devcards
            {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
             :figwheel {:devcards true}
             :compiler {:main "clojure-101.cards"
                        :asset-path "js/devcards_out"
                        :output-to "target/cljsbuild/public/js/app_devcards.js"
                        :output-dir "target/cljsbuild/public/js/devcards_out"
                        :source-map-timestamp true
                        :optimizations :none
                        :pretty-print true}}
            }
   }


  :figwheel
  {:http-server-root "public"
   :server-port 3449
   :nrepl-port 7002
   :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"
                      ;; "cider.nrepl/cider-middleware"
                      ;; "refactor-nrepl.middleware/wrap-refactor"
                      ]
   :css-dirs ["resources/public/css"]
   :ring-handler clojure-101.handler/app}



  :profiles {:dev {:repl-options {:init-ns clojure-101.repl
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

                   :dependencies [[binaryage/devtools "0.9.7"]
                                  [ring/ring-mock "0.3.1"]
                                  [ring/ring-devel "1.6.2"]
                                  [prone "1.1.4"]
                                  [figwheel-sidecar "0.5.14"]
                                  [org.clojure/tools.nrepl "0.2.13"]
                                  [com.cemerick/piggieback "0.2.2"]
                                  [devcards "0.2.3" :exclusions [cljsjs/react]]
                                  [pjstadig/humane-test-output "0.8.3"]
                                  ]

                   :source-paths ["env/dev/clj"]
                   :plugins [[lein-figwheel "0.5.14"]
                             #_[cider/cider-nrepl "0.10.0-SNAPSHOT"]
                             #_[org.clojure/tools.namespace "0.3.0-alpha2"
                              :exclusions [org.clojure/tools.reader]]
                             #_[refactor-nrepl "2.0.0-SNAPSHOT"
                              :exclusions [org.clojure/clojure]]
                             ]

                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :env {:dev true}}

             :uberjar {:hooks [minify-assets.plugin/hooks]
                       :source-paths ["env/prod/clj"]
                       :prep-tasks ["compile" ["cljsbuild" "once" "min"]]
                       :env {:production true}
                       :aot :all
                       :omit-source true}})
