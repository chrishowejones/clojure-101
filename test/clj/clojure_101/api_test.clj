(ns clojure-101.api-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [clojure-101.api :as api]
   [clojure-101.postgres :as postgres]))

(deftest add-person
  (let [people (atom [])]
   (testing "Add a valid person and ensure they're present"
     (is (= {:first-name "Fred" :last-name "Bloggs" :id 1}
            (api/add-person people {:first-name "Fred" :last-name "Bloggs"})))
     (is (= [{:first-name "Fred" :last-name "Bloggs" :id 1}]
            @people)))
   (testing "Add an invalid person and ensure they fail"
     (is (= {:error
             "val: {:first-name \"Fred\"} fails spec: :clojure-101.api-spec/person predicate: (contains? % :last-name)\n"}
            (api/add-person people {:first-name "Fred"}))))))

(deftest popular-studio
  (testing "Check most popular studio returned for a single person single film"
    (is (= "{\"studio\":\"studio1\",\"count\":1}"
           (api/most-popular-studio
            [{:first-name "a" :last-name "a" :films [{:title "film1" :studio "studio1" :release-year "2024"}]}]))))
  (testing "Check most popular studio returned for a single person several films"
    (is (= "{\"studio\":\"studio1\",\"count\":2}"
           (api/most-popular-studio
            [{:first-name "a" :last-name "a"
              :films [{:title "film1" :studio "studio1" :release-year "2024"}
                      {:title "film2" :studio "studio2" :release-year "2024"}
                      {:title "film3" :studio "studio1" :release-year "2024"}]}]))))
  (testing "Check most popular studio returned for a several people several films"
    (is (= "{\"studio\":\"studio2\",\"count\":4}"
           (api/most-popular-studio
            [{:first-name "a" :last-name "a"
              :films [{:title "film1" :studio "studio1" :release-year "2024"}
                      {:title "film2" :studio "studio2" :release-year "2024"}
                      {:title "film3" :studio "studio1" :release-year "2024"}]}
             {:first-name "b" :last-name "b"
              :films [{:title "film4" :studio "studio2" :release-year "2024"}
                      {:title "film5" :studio "studio2" :release-year "2024"}
                      {:title "film6" :studio "studio2" :release-year "2024"}]}]))))
  (testing "Check most popular studio is blank for no people or for people without films"
    (is (= "{\"studio\":\"\",\"count\":0}"
           (api/most-popular-studio [])))
    (is (= "{\"studio\":\"\",\"count\":0}"
           (api/most-popular-studio [{:first-name "a" :last-name "a"}
                                     {:first-name "b" :last-name "b"}])))))

(deftest add-person-to-db
  (testing "Add a person using the database"
    (let [person-no-films (api/create-person {:first-name "Fred" :last-name "Bloggs"} identity identity)]
      (is (= {:first-name "Fred" :last-name "Bloggs"}
             (dissoc person-no-films :id)))
      (is (uuid? (:id person-no-films))))
    (let [person-with-films (api/create-person {:first-name "Fred" :last-name "Bloggs"
                                                :films [{:title "dummy film" :studio "studio" :release-year "2024"}]}
                                               identity
                                               identity)]
      (is (= {:first-name "Fred" :last-name "Bloggs"
              :films [{:title "dummy film" :studio "studio" :release-year "2024"}]}
             (dissoc person-with-films :id)))
      (is (uuid? (:id person-with-films))))
    (testing "Adding a person with invalid films using the database results in error."
      (is (= {:error
              "val: {:first-name \"Fred\"} fails spec: :clojure-101.api-spec/person predicate: (contains? % :last-name)\n"}
             (api/create-person {:first-name "Fred"} nil nil)))
      (is (= {:error
              "In: [:films 0] val: {:title \"title\", :studio \"studio\"} fails spec: :clojure-101.api-spec/film at: [:films] predicate: (contains? % :release-year)\n"}
             (api/create-person {:first-name "Fred" :last-name "Bloggs"
                                 :films [{:title "title" :studio "studio"}]}
                                nil
                                nil))))))

(deftest post-person-db
  (testing "Post a new person using the database"
    (let [person-with-films (api/post-person "{\"first-name\":\"Fred\",\"last-name\":\"Bloggs\",\"films\":[{\"title\":\"dummy film\",\"studio\":\"studio\",\"release-year\":\"2024\"}]}"
                                             identity
                                             identity)]
      (is (= {:status 201
              :headers {"Content-Type" "application/json"}
              :body
              {:first-name "Fred" :last-name "Bloggs"
               :films [{:title "dummy film" :studio "studio" :release-year "2024"}]}}
             (update person-with-films :body dissoc :id)))
      (is (uuid? (get-in person-with-films [:body :id]))))
    (testing "Adding a person with invalid films using the database results in error."
      (is (= {:status 500
              :headers {"Content-Type" "application/json"}
              :body {:error
                     "val: {:first-name \"Fred\"} fails spec: :clojure-101.api-spec/person predicate: (contains? % :last-name)\n"}}
             (api/post-person "{\"first-name\":\"Fred\"}" nil nil)))
      (is (= {:status 500
              :headers {"Content-Type" "application/json"}
              :body {:error
                     "In: [:films 0] val: {:title \"title\", :studio \"studio\"} fails spec: :clojure-101.api-spec/film at: [:films] predicate: (contains? % :release-year)\n"}}
             (api/post-person "{\"first-name\":\"Fred\",\"last-name\":\"Bloggs\",\"films\":[{\"title\":\"title\",\"studio\":\"studio\"}]}"
                              nil
                              nil))))))

(deftest get-all-people
  (testing "Get all people from database"
    
    (let [fetch-all-people (fn []
                            [{:id 1
                              :first-name "Fred" :last-name "Bloggs"}
                             {:id 2
                              :first-name "Joe" :last-name "Smith"}])
          fetch-films-for-person (fn [person-id]
                                  (get {1 [{:title "dummy film" :studio "studio" :release-year "2024"}]
                                        2 [{:title "another film" :studio "another studio" :release-year "1990"}]}
                                       person-id))]
      (is (= {:status 200
              :headers {"Content-Type" "application/json"}
              :body
              [{:id 1
                :first-name "Fred" :last-name "Bloggs"
                :films [{:title "dummy film" :studio "studio" :release-year "2024"}]}
               {:id 2
                :first-name "Joe" :last-name "Smith"
                :films [{:title "another film" :studio "another studio" :release-year "1990"}]}]}
             (api/get-all-people fetch-all-people fetch-films-for-person))))))

(deftest popular-studio-db
  (testing "Check most popular studio returned from database"
    (with-redefs [postgres/find-popular-studio (fn [_]
                                                 [{:studio "studio1" :count 1}])]
     (is (= "{\"studio\":\"studio1\",\"count\":1}"
            (api/most-popular-studio-db nil))))))
