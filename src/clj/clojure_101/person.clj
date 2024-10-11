(ns clojure-101.person
  (:require [clojure-101.api-spec :as api-spec]
            [clojure.spec.alpha :as s]))

(defn validate-person [unvalidated-person]
  (let [person (s/conform ::api-spec/person unvalidated-person)]
    (if (s/invalid? person)
      {:error (s/explain-str ::api-spec/person unvalidated-person)}
      person)))

(defn prepare-films-to-store
  [films]
  (map #(assoc % :id (random-uuid)) films))

(defn prepare-person-to-store
  [validated-person]
  (-> validated-person
      (assoc :id (random-uuid))
      (update :films prepare-films-to-store)))
