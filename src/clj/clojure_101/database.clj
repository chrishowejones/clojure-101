(ns clojure-101.database)

(defn create-person [store-new-person store-new-films person]
  (let [person-id (random-uuid)
        person-with-id (assoc person :id person-id)
        person-minus-films (dissoc person-with-id :films)
        films (:films person)
        films-for-db (map #(assoc % :person-id person-id :id (random-uuid)) films)]
    (store-new-person person-minus-films)
    (store-new-films films-for-db)
    person-with-id))

(defn find-films-for-people
  [people fetch-films-for-person]
  (letfn [(get-films-for-person [person]
            (->> person
                 :id
                 (fetch-films-for-person)
                 (assoc person :films)))]
    (map get-films-for-person people)))

