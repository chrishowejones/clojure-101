(ns clojure-101.api-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [clojure-101.api :as api]))

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
