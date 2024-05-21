(ns clojure-101.api
  (:require
   [cheshire.core :as json]
   [clojure-101.api-spec :as api-spec]
   [clojure.spec.alpha :as s]
   [compojure.core :refer [defroutes GET POST]]
   [ring.util.response :refer [content-type response status]]))

(def people-json
  "[{\"id\":1,\"first-name\":\"Chris\",\"last-name\":\"Howe-Jones\",
  \"films\": [
  {\"title\":\"Star Wars: Episode IV - A New Hope\",\"studio\":\"20th Century Fox\",\"release-year\":\"1977\"},
  {\"title\":\"Raiders of the Lost Ark\",\"studio\":\"Paramount\",\"release-year\":\"1981\"},
  {\"title\":\"The Godfather\",\"studio\":\"Paramount\",\"release-year\":\"1972\"}
  ]},
  {\"id\":2,\"first-name\":\"Cerys\",\"middle-name\":\"Eilonwy\",\"last-name\":\"Howe-Jones\",
  \"films\": [
  {\"title\":\"Truman Show\",\"studio\":\"Paramount\",\"release-year\":\"1998\"},
  {\"title\":\"Elemental\",\"studio\":\"Disney\",\"release-year\":\"2023\"},
  {\"title\":\"Up\",\"studio\":\"Disney\",\"release-year\":\"2009\"},
  {\"title\":\"Cinderella\",\"studio\":\"Disney\",\"release-year\":\"1950\"}
  ]},
  {\"id\":3,\"first-name\":\"Danielle\",\"last-name\":\"Howe-Jones\",\"nickname\":\"Dan\"}]")

(def people (atom (json/decode people-json true)))

(defn most-popular-studio
  "Takes in map of people, extracts films and within that studio then determines
   frequencies and extracts max before turning into json string"
  [people-decoded]
  (let [people (s/conform ::api-spec/people people-decoded)]
    (when (s/invalid? people)
      (throw (IllegalArgumentException. (s/explain-str ::api-spec/people people-decoded))))
    (->> people
         (sequence (comp (mapcat :films) (map :studio)))
         frequencies
         (reduce (fn [[mk mv] [k v]] (if (< mv v) [k v] [mk mv])) ["" 0])
         (zipmap [:studio :count])
         json/generate-string)))

(defn add-person
  "Accepts a map representing a Person and stores it. Returns Person or returns error map if Person is illegal spec."
  [people person-decoded]
  (let [person (s/conform ::api-spec/person person-decoded)
        add-person-with-id  (fn [people person]
                              (let [id (inc (apply max (into [0] (map :id people))))]
                                (conj people (assoc person :id id))))]
    (if (s/invalid? person)
      (assoc {} :error  (s/explain-str ::api-spec/person person-decoded))
      (->> person
           (swap! people add-person-with-id)
           first))))

(defroutes routes
  (GET "/" [] "add some links to routes here.")
  (GET "/people" []
    (-> @people
        response
        (content-type "application/json")))
  (GET "/popular-studio" []
    (-> @people
        most-popular-studio
        response
        (content-type "application/json")))
  (POST "/people" req
    (let [person-json (-> req :body slurp)]
      (-> (response (add-person people (json/decode person-json true)))
          (status 201)
          (content-type "application/json")))))

(comment

  @people

  (add-person people {:first-name "Jonny" :last-name "Hobbs" :films [{:title "Toy Story" :release-year "1995" :studio "Disney"}]})

  (reset! people (into [] (json/decode people-json true)))

  (frequencies ["fred" "fred" "bill" "chris" "fred"])

  (-> @people
      most-popular-studio
      response
      (content-type "application/json"))

  )
