(ns clojure-101.middleware
  (:require [prone.middleware :refer [wrap-exceptions]]
            [ring.middleware.defaults
             :refer
             [api-defaults site-defaults wrap-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]))

(defn wrap-middleware [handler]
  (-> handler
      (wrap-defaults site-defaults)
      wrap-exceptions))

(defn wrap-api-middleware [handler]
  (-> handler
      (wrap-defaults api-defaults)
      wrap-exceptions
      wrap-json-response))
