(ns clojure-101.api-spec
  (:require [clojure.spec.alpha :as s]))

(s/def ::first-name string?)
(s/def ::last-name string?)
(s/def ::another number?)
(s/def ::optional double?)
(s/def ::title string?)
(s/def ::studio string?)
(s/def ::release-year string?)

(s/def ::film (s/keys :req-un [::title ::studio ::release-year]))

(s/def ::films (s/coll-of ::film :kind vector?))

(s/def ::person
    (s/keys :req-un [::first-name ::last-name]
            :opt-un [::films ::optional]))

(s/def ::people
  (s/coll-of ::person))


(comment

  (s/valid? ::person {:first-name "chris" :last-name "howe-jones"})
  (s/valid? ::people [{:first-name "chris" :last-name "howe-jones"}])
  (s/conform :clojure-101.api-spec/people [{:first-name "chris" :last-name "howe-jones"
                        :films [{:title "A new hope" :studio "Paramount" :release-year "1977"}]}])
  (s/conform ::people [{:first-name "chris" :last-name "howe-jones"
                        :films '({:title "A new hope" :studio "Paramount" :release-year "1977"})}])


  )
