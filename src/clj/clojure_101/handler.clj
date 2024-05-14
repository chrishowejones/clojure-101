(ns clojure-101.handler
  (:require [clojure-101.api :as api]
            [clojure-101.middleware :refer [wrap-api-middleware wrap-middleware]]
            [compojure.core :refer [context defroutes GET]]
            [compojure.route :refer [not-found resources]]
            [config.core :refer [env]]
            [hiccup.page :refer [html5 include-css include-js]]))

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

(defroutes routes
  (wrap-api-middleware (context "/api" [] api/routes))
  (wrap-middleware (GET "/" [] (loading-page)))
  (resources "/")
  (not-found "Not Found"))

(def app #'routes)
