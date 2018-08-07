(ns clojure-101.api
  (:require [cheshire.core :as json]
            [clojure-101.api-spec :as api-spec]
            [clojure.spec.alpha :as s]
            [compojure.core :refer [defroutes GET POST]]
            [ring.middleware.anti-forgery :as ring.anti-forgery]))

(def people-json
  "[{\"first-name\":\"Chris\",\"last-name\":\"Howe-Jones\",
  \"films\": [
  {\"title\":\"Star Wars: Episode IV – A New Hope\",\"studio\":\"20th Century Fox\",\"release-year\":\"1977\"},
  {\"title\":\"Raiders of the Lost Ark\",\"studio\":\"Paramount\",\"release-year\":\"1981\"},
  {\"title\":\"The Godfather\",\"studio\":\"Paramount\",\"release-year\":\"1972\"}
  ]},
  {\"first-name\":\"Cerys\",\"middle-name\":\"Eilonwy\",\"last-name\":\"Howe-Jones\",
  \"films\": [
  {\"title\":\"Bambi\",\"studio\":\"Disney\",\"release-year\":\"1942\"},
  {\"title\":\"Despicable Me\",\"studio\":\"Universal\",\"release-year\":\"2010\"},
  {\"title\":\"Truman Show\",\"studio\":\"Paramount\",\"release-year\":\"1998\"}
  ]},
  {\"first-name\":\"Danielle\",\"last-name\":\"Howe-Jones\",\"nickname\":\"Dan\"}]")

(def people (atom (json/decode people-json true)))

(defn most-popular-studio
  "takes in json string, extracts films and within that studio then determines
   frequencies and extracts max before turning into json string"
  [people-decoded]
  (let [people (s/conform :clojure-101.api-spec/people people-decoded)]
    (when (s/invalid? people)
      (throw (IllegalArgumentException. (s/explain-str :clojure-101.api-spec/people people-decoded))))
    (->> people
         (sequence (comp (mapcat :films) (map :studio)))
         frequencies
         (reduce (fn [[mk mv] [k v]] (if (< mv v) [k v] [mk mv])))
         (zipmap [:studio :count])
         json/generate-string)))

(defn add-person [people person]
  (let [person (s/conform :clojure-101.api-spec/person person)]
    (when (s/invalid? person)
      (throw (IllegalArgumentException. (s/explain-str :clojure-101.api-spec/person person))))
    (conj people person)))

(defroutes routes
  (GET "/" [] "add some links to routes here!")
  (GET "/people" [] (do (println ring.anti-forgery/*anti-forgery-token*)
                        (json/encode @people)))
  (GET "/popular-studio" [] (most-popular-studio @people))
  (POST "/people" req
        (let [person-json (-> req :body slurp)]
          (swap! people add-person (json/decode person-json true))
          (-> {}
              (assoc :body person-json)
              (assoc :status 201)))))


(comment

  (s/conform :clojure-101.api-spec/people [{:first-name "Cerys" :last-name "howe-jones"
                        :films [{:title "A new hope" :studio "Paramount" :release-year "1977"}]}])

  (def people (json/decode people-json true))
  people

  ;; get nickname


  ;; get films


  ;; then get studio for each




  ;; then determine number of occurances of each film


  ;; then reduce over collection to get max occurance


  ;; then convert to json string


  )
