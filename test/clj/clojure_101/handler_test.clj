(ns clojure-101.handler-test
  (:require
   [cheshire.core :as json]
   [clojure-101.api :as api]
   [clojure-101.handler :refer [app]]
   [clojure.test :refer [deftest is]]
   [ring.mock.request :refer [request body content-type]]))

(deftest check-root-api-handler
  (is (= "add some links to routes here."
         (->> (request :get "/api")
              app
              :body))))

(deftest check-people-api-handler
  (is (= (json/encode @api/people)
         (->> (request :get "/api/people")
              app
              :body))))

(deftest check-add-people-handler
  (let [response-body (-> (request :post "/api/people")
                          (body (json/encode {:first-name "Fred" :last-name "Bloggs"}))
                          (content-type "application/json")
                          app
                          :body)]
    (is (= {:first-name "Fred" :last-name "Bloggs"}
           (-> response-body
               (json/decode keyword)
               (dissoc :id))))
    (is (int? (-> response-body
                  (json/decode keyword)
                  :id) ))))

(deftest check-popular-studio-handler
  (let [response-body (-> (request :get "/api/popular-studio") app :body)]
    (is (= "{\"studio\":\"Paramount\",\"count\":3}"
           response-body))))
