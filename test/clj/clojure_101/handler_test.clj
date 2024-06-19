(ns clojure-101.handler-test
  (:require
   [cheshire.core :as json]
   [clojure-101.api :as api]
   [clojure-101.handler :refer [app]]
   [clojure.test :refer [deftest is]]
   [ring.mock.request :refer [request body content-type]]
   [clojure-101.postgres :as postgres]))

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

(deftest check-peopledb-api-handler
  (with-redefs [postgres/find-all-people (fn [_]
                                             [{:id 1
                                               :first-name "Fred" :last-name "Bloggs"}
                                              {:id 2
                                               :first-name "Joe" :last-name "Smith"}])
                  postgres/find-films-for-person (fn [_ person-id]
                                                   (get {1 [{:title "dummy film" :studio "studio" :release-year "2024"}]
                                                         2 [{:title "another film" :studio "another studio" :release-year "1990"}]}
                                                        person-id))]
    (is (= (json/encode
            [{:id 1
              :first-name "Fred" :last-name "Bloggs"
              :films [{:title "dummy film" :studio "studio" :release-year "2024"}]}
             {:id 2
              :first-name "Joe" :last-name "Smith"
              :films [{:title "another film" :studio "another studio" :release-year "1990"}]}])
           (->> (request :get "/api/peopledb")
                app
                :body)))))

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

(deftest check-add-peopledb-handler
  (with-redefs [postgres/create-person (fn [_ person]
                                         (assoc person :id 1))
                postgres/create-films-for-person (fn [_ _ films]
                                                   films)]
    (let [response-body (-> (request :post "/api/peopledb")
                            (body (json/encode {:first-name "Fred" :last-name "Bloggs"}))
                            (content-type "application/json")
                            app
                            :body)]
      (is (= {:id 1 :first-name "Fred" :last-name "Bloggs"}
             (-> response-body
                 (json/decode keyword)))))
    (let [response-body (-> (request :post "/api/peopledb")
                            (body (json/encode {:first-name "Fred"
                                                :last-name "Bloggs"
                                                :films [{:title "dummy film" :studio "studio" :release-year "2024"}]}))
                            (content-type "application/json")
                            app
                            :body)]
      (is (= {:id 1 :first-name "Fred"
              :last-name "Bloggs"
              :films [{:title "dummy film" :studio "studio" :release-year "2024"}]}
             (-> response-body
                 (json/decode keyword)))))))

(deftest check-popular-studio-handler
  (let [response-body (-> (request :get "/api/popular-studio") app :body)]
    (is (= "{\"studio\":\"Paramount\",\"count\":3}"
           response-body))))
