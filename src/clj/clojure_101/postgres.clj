(ns clojure-101.postgres
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [clojure.string :as str]))

(defn create-person
  [ds person]
  (when person
    (if (:id person)
      (sql/insert! ds :person person)
      (let [new-id (random-uuid)]
        (sql/insert! ds :person (assoc person :id new-id))))))

(defn create-films-for-person
  [ds films]
  (sql/insert-multi! ds :film films))

(defn delete-film
  [ds id]
  (sql/delete! ds :film ["id = ?" id]))

(defn find-all-people
  [ds]
  (sql/query ds ["select * from person"]))

(defn find-person
  [ds id]
  (first
   (sql/query ds ["select * from person where id = ?::uuid" id])))

(defn find-people-by-name
  "Find peeople by name. Simple fuzzy search to find subset of string (case insensitive)."
  [ds name]
  (let [[first-search-str second-search-str & _rest] (str/split name #"\s")]
    (->> (if (str/blank? second-search-str)
           ["select * from person where first_name ilike ? or last_name ilike ?"
            (str "%" first-search-str "%")
            (str "%" first-search-str "%")]
           ["select * from person where first_name ilike ? and last_name ilike ?"
            (str "%" first-search-str "%")
            (str "%" second-search-str "%")])
         (sql/query ds))))

(defn find-films-for-person
  [ds person-id]
  (sql/query ds ["select id, title, studio, release_year from film where person_id = ?" person-id]))

(defn find-popular-studio
  [ds]
  (sql/query ds ["select studio, count
                  from
                  (select studio, count(studio) as count
                  from film
                  group by studio
                  order by count desc) as studiocount
                  limit 1"]))

(comment

  (def db-spec {:dbtype "postgresql"
                :dbname "clojure101"
                :host "localhost"
                :user "clojure101"
                :password "clojure101"})
  (def ds (jdbc/with-options (jdbc/get-datasource db-spec) next.jdbc/unqualified-snake-kebab-opts))

  (find-popular-studio ds)

  (find-all-people ds)

  (create-person ds {:first-name "Chris" :last-name "Howe-Jones"})

  (require 'clojure-101.handler)
  (create-person (clojure-101.handler/get-datasource db-spec)
                 {:first-name "Chris" :last-name "Howe-Jones" :id #uuid "dbcdae30-ab93-4ef7-b7dd-85ce933d8729"})

  (sql/delete! ds :person ["id = 'ce1f1823-1a9e-4b60-be18-9d522fd9472e'"])
  (sql/delete! ds :film ["person_id = 'dbcdae30-ab93-4ef7-b7dd-85ce933d8729'"])

  (sql/query ds ["select studio, count(studio) as count
                  from film
                  group by studio
                  order by count desc"])

  (create-films-for-person ds 1 [{:title "Star Wars: Episode IV - A New Hope",
                                  :studio "20th Century Fox",
                                  :release-year "1977"}
                                 {:title "Raiders of the Lost Ark",
                                  :studio "Paramount",
                                  :release-year "1981"}
                                 {:title "The Godfather",
                                  :studio "Paramount",
                                  :release-year "1972"}])

  (find-films-for-person ds 1)
  (find-person ds "727d3923-73cd-434e-a166-cd13b0478eaf")
  (find-people-by-name ds "c")

  )
