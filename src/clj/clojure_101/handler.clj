(ns clojure-101.handler
  (:require [clojure-101.api :as api]
            [clojure-101.middleware :refer [wrap-api-middleware wrap-middleware]]
            [compojure.core :refer [context defroutes GET]]
            [compojure.route :refer [not-found resources]]
            [config.core :refer [env]]
            [hiccup.page :refer [html5 include-css include-js]]
            [next.jdbc :as jdbc]
            [clojure-101.database :as database]
            [clojure-101.config :as config]))

(def mount-target
  [:div#app
      [:h3 "ClojureScript has not been compiled!"]
      [:p "please run "
       [:b "lein figwheel"]
       " in order to start the compiler"]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn loading-page []
  (html5
    (head)
    [:body {:class "body-container"}
     mount-target
     (include-js "/js/app.js")]))

#_(def db-spec {:dbtype "postgresql"
              :dbname "clojure101"
              :host "localhost"
              :user "clojure101"
              :password "clojure101"})

(defn get-datasource [db-spec]
  (some-> db-spec
          jdbc/get-datasource
          (jdbc/with-options next.jdbc/unqualified-snake-kebab-opts)))

(defn wrap-ds [handler database]
  (fn [req]
    (if database
      (->> (get-datasource database)
           (assoc req :ds)
           handler)
      (handler req))))

(defroutes routes
  (-> (context "/api" [] api/routes)
      (wrap-ds (:database @config/config))
      wrap-api-middleware)
  (wrap-middleware (GET "/*" [] (loading-page)))
  (resources "/")
  (not-found "Not Found"))

(def app #'routes)

(comment

  (def ds (get-datasource 
           {:jdbcUrl "jdbc:postgresql://localhost:5432/clojure101?user=clojure101&password=clojure101"}))

  (get-datasource (:database @config/config))

  (require 'clojure-101.postgres)
  (clojure-101.postgres/find-all-people ds)
;;
  )
