(ns clojure-101.postgres
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]))

(defn create-person
  [ds person]
  (when person
    (sql/insert! ds :person person)))

(defn create-films-for-person
  [ds person-id films]
  (let [films-with-person-id (map #(assoc % :person-id person-id) films)]
    (sql/insert-multi! ds :film films-with-person-id)))

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
