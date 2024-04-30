(ns clojure-101.server
  (:require [clojure-101.handler :refer [app]]
            [config.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]]
            [nrepl.server :refer [start-server]])
  (:gen-class))

(defonce nrepl-server (atom nil))

(defn -main [& args]
  (let [port (Integer/parseInt (or (env :port) "3000"))
        nrepl-port (when-let [nrepl-port (env :nrepl-port)]
                     (Integer/parseInt nrepl-port))]

    (when nrepl-port
      (reset! nrepl-server (start-server :port nrepl-port)))
    (run-jetty app {:port port :join? false})))

(comment
  (-main)

  (def server (run-jetty app {:port 3000 :join? true}))

  )
