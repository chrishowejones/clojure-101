(ns clojure-101.middleware
  (:require [ring.middleware.defaults
             :refer
             [api-defaults site-defaults wrap-defaults]]))

(defn wrap-middleware [handler]
  (wrap-defaults handler site-defaults))

(defn wrap-api-middleware [handler]
  (wrap-defaults handler api-defaults))
