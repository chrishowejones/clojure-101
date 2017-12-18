(ns clojure-101.api
  (:require [compojure.core :refer [GET defroutes]]
            [hiccup.page :refer [html5]]
            [cheshire.core :as json]))

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

(defn most-popular-studio
  "takes in json string, extracts films and within that studio then determines
   frequencies and extracts max before turning into json string"
  [people-json]
  (->> (json/decode people-json true)
       (sequence (comp (mapcat :films) (map :studio)))
       frequencies
       (reduce (fn [[mk mv] [k v]] (if (< mv v) [k v] [mk mv])))
       (zipmap [:film :count])
       json/generate-string))

(defroutes routes
  (GET "/" [] "add some links to routes here!")
  (GET "/people" [] (json/decode people-json))
  (GET "/popular-studio" [] (most-popular-studio people-json)))


(comment

  (def people (json/decode people-json true))

  ;; get nickname

  ;; get films


  ;; then get studio for each


  ;; then determine number of occurances of each film


  ;; then reduce over collection to get max occurance


  ;; then convert to json string


  )
