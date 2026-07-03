(ns clojure-101.postgres
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]))

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

(defn find-all-people
  [ds]
  (sql/query ds ["select * from person"]))

(defn find-films-for-person
  [ds person-id]
  (sql/query ds ["select title, studio, release_year from film where person_id = ?" person-id]))

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

  (sql/delete! ds :person ["id = 'dbcdae30-ab93-4ef7-b7dd-85ce933d8729'"])
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

  )
