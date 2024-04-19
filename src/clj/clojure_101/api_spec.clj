(ns clojure-101.api-spec
  (:require [clojure.spec.alpha :as s]))

(s/def ::first-name string?)
(s/def ::last-name string?)
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
                                            :films [{:title "A new hope" :studio "Paramount" :release-year "1977"}]
                                            :optional 12.0}])

  ;; => [{:first-name "chris",
  ;;      :last-name "howe-jones",
  ;;      :films
  ;;      [{:title "A new hope", :studio "Paramount", :release-year "1977"}]}]

  (s/conform ::people [{:first-name "chris" :last-name "howe-jones"
                        :films [{:title "A new hope" :studio "Paramount"}]}])
  ;; => :clojure.spec.alpha/invalid

  (s/explain-data ::people [{:first-name "chris" :last-name "howe-jones"
                        :films [{:title "A new hope" :studio "Paramount"}]}])

  (require 'cheshire.core)
  (s/explain-str ::person
                 (cheshire.core/decode
                  "{ \"first-name\":\"Dexter\", \"last-name\": \"Dog\",\"films\":[{\"title\":\"Up\",\"studio\":\"Pixar\"}]}"
                  true))
  ;; => "In: [:films 0] val: {:title \"Up\", :studio \"Pixar\"} fails spec: :clojure-101.api-spec/film at: [:films] predicate: (contains? % :release-year)\n"

  )
