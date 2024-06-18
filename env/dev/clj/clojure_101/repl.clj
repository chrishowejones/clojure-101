(ns clojure-101.repl
  (:require [clojure-101.handler :refer [app]]
            [ring.middleware.file :refer [wrap-file]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.server.standalone :refer [serve]]
            [ragtime.jdbc :as ragtime]
            [ragtime.repl :as repl]))

(defn load-config []
  {:datastore  (ragtime/sql-database "jdbc:postgresql://localhost:5432/clojure101?user=clojure101&password=clojure101")
   :migrations (ragtime/load-resources "migrations")})

(defn migrate []
  (repl/migrate (load-config)))

(defn rollback []
  (repl/rollback (load-config)))

(defonce server (atom nil))

(defn get-handler []
  ;; #'app expands to (var app) so that when we reload our code,
  ;; the server is forced to re-resolve the symbol in the var
  ;; rather than having its own copy. When the root binding
  ;; changes, the server picks it up without having to restart.
  (-> #'app
      ; Makes static assets in $PROJECT_DIR/resources/public/ available.
      (wrap-file "resources")
      ; Content-Type, Content-Length, and Last Modified headers for files in body
      (wrap-file-info)))

(defn start-server
  "used for starting the server in development mode from REPL"
  [& [port]]
  (let [port (if port (Integer/parseInt port) 3000)]
    (reset! server
            (serve (get-handler)
                   {:port port
                    :auto-reload? true
                    :join? false}))
    (println (str "You can view the site at http://localhost:" port))))

(defn stop-server []
  (.stop @server)
  (reset! server nil))


(comment

  (migrate)
  (rollback)

  (require '[next.jdbc :as jdbc])

  (def db-spec {:dbtype "postgresql"
                :dbname "postgres"
                :host "localhost"
                :user "postgres"
                :password "password"})

  (def ds (jdbc/get-datasource db-spec))

  (jdbc/execute! ds ["select now();"])

  @server

  (start-server)

  (stop-server)


  )
