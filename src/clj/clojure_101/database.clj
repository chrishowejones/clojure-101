(ns clojure-101.database)

(defn create-person [store-new-person store-new-films person]
  (let [person-id (random-uuid)
        person-with-id (assoc person :id person-id)
        person-minus-films (dissoc person-with-id :films)
        films (:films person)
        films-for-db (map #(assoc % :person-id person-id) films)]
    (store-new-person person-minus-films)
    (store-new-films films-for-db)
    person-with-id))
