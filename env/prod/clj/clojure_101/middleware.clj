(ns clojure-101.middleware
  (:require [ring.middleware.defaults
             :refer
             [api-defaults site-defaults wrap-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]))

(defn wrap-middleware [handler]
  (-> handler
      (wrap-defaults site-defaults)
      wrap-json-response))

(defn wrap-api-middleware [handler]
  (-> handler
      (wrap-defaults api-defaults)
      wrap-json-response))
