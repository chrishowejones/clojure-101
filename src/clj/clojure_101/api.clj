(ns clojure-101.api
  (:require [cheshire.core :as json]
            [clojure-101.api-spec :as api-spec]
            [clojure.spec.alpha :as s]
            [compojure.core :refer [defroutes GET POST]]))

(def people-json
  "[{\"first-name\":\"Chris\",\"last-name\":\"Howe-Jones\",
  \"films\": [
  {\"title\":\"Star Wars: Episode IV – A New Hope\",\"studio\":\"20th Century Fox\",\"release-year\":\"1977\"},
  {\"title\":\"Raiders of the Lost Ark\",\"studio\":\"Paramount\",\"release-year\":\"1981\"},
  {\"title\":\"The Godfather\",\"studio\":\"Paramount\",\"release-year\":\"1972\"}
  ]},
  {\"first-name\":\"Cerys\",\"middle-name\":\"Eilonwy\",\"last-name\":\"Howe-Jones\",
  \"films\": [
  {\"title\":\"Truman Show\",\"studio\":\"Paramount\",\"release-year\":\"1998\"},
  {\"title\":\"Elemental\",\"studio\":\"Disney\",\"release-year\":\"2023\"},
  {\"title\":\"Up\",\"studio\":\"Disney\",\"release-year\":\"2009\"},
  {\"title\":\"Cinderella\",\"studio\":\"Disney\",\"release-year\":\"1950\"}
  ]},
  {\"first-name\":\"Danielle\",\"last-name\":\"Howe-Jones\",\"nickname\":\"Dan\"}]")

(def people (atom (json/decode people-json true)))

(defn most-popular-studio
  "takes in map of people, extracts films and within that studio then determines
   frequencies and extracts max before turning into json string"
  [people-decoded]
  (let [people (s/conform ::api-spec/people people-decoded)]
    (when (s/invalid? people)
      (throw (IllegalArgumentException. (s/explain-str ::api-spec/people people-decoded))))
    (->> people
         (sequence (comp (mapcat :films) (map :studio)))
         frequencies
         (reduce (fn [[mk mv] [k v]] (if (< mv v) [k v] [mk mv])))
         (zipmap [:studio :count])
         json/generate-string)))

(defn add-person
  "Accepts a map representing a Person and stores it. Returns Person or returns error map if Person is illegal spec."
  [people person-decoded]
  (let [person (s/conform ::api-spec/person person-decoded)]
    (if (s/invalid? person )
      (assoc {} :error  (s/explain-str ::api-spec/person person-decoded))
      (do
        (swap! people conj person)
        person))))

(defroutes routes
  (GET "/" [] "add some links to routes here!")
  (GET "/people" [] (json/encode @people))
  (GET "/popular-studio" [] (most-popular-studio @people))
  (POST "/people" req
        (let [person-json (-> req :body slurp)]
          (-> {}
              (assoc :body (-> people
                               (add-person (json/decode person-json true))
                               json/encode))
              (assoc :status 201)))))


(comment

  (s/conform ::api-spec/people [{:first-name "Cerys" :last-name "howe-jones"
                                 :films [{:title "A new hope" :studio "Paramount" :release-year "1977"}]}])
  ;; => [{:first-name "Cerys",
  ;;      :last-name "howe-jones",
  ;;      :films
  ;;      [{:title "A new hope", :studio "Paramount", :release-year "1977"}]}]

  (s/explain-str :clojure-101.api-spec/person
                 (json/decode
                  "{
                 \"first-name\": \"Fiona\",
                 \"last-name\": \"Hobbs\",
                 \"films\": [
                           {
                            \"title\": \"Toy Story\",
                            \"studio\": \"Pixar\",
                            \"release-year\": \"1996\"
                            },
                           {
                            \"title\": \"Fifty Shades of Grey\",
                            \"studio\": \"Paramount\",
                            \"release-year\": \"2015\"
                            }
                           ]
                 }" true))
  ;; => "Success!\n"

  (def people-map (json/decode people-json true))
  people-map
  ;; => ({:first-name "Chris",
  ;;      :last-name "Howe-Jones",
  ;;      :films
  ;;      [{:title "Star Wars: Episode IV – A New Hope",
  ;;        :studio "20th Century Fox",
  ;;        :release-year "1977"}
  ;;       {:title "Raiders of the Lost Ark",
  ;;        :studio "Paramount",
  ;;        :release-year "1981"}
  ;;       {:title "The Godfather",
  ;;        :studio "Paramount",
  ;;        :release-year "1972"}]}
  ;;     {:first-name "Cerys",
  ;;      :middle-name "Eilonwy",
  ;;      :last-name "Howe-Jones",
  ;;      :films
  ;;      [{:title "Bambi", :studio "Disney", :release-year "1942"}
  ;;       {:title "Despicable Me",
  ;;        :studio "Universal",
  ;;        :release-year "2010"}
  ;;       {:title "Truman Show",
  ;;        :studio "Paramount",
  ;;        :release-year "1998"}]}
  ;;     {:first-name "Danielle", :last-name "Howe-Jones", :nickname "Dan"})

  ;; get nickname
  (sequence
   (comp (map :nickname)
         (filter (complement nil?)))
   people-map)

  ;; get films
  (mapcat :films people-map)

  ;; then get studio for each
  (->> people-map
       (mapcat :films)
       (map :studio))



  ;; then determine number of occurances of each film
  (->> people-map
       (mapcat :films)
       (map :studio)
       frequencies)

  ;; then reduce over collection to get max occurance
  (->> people-map
       (mapcat :films)
       (map :studio)
       frequencies
       (reduce (fn [[max-studio max-value] [studio value]]
                 (if (< max-value value)
                   [studio value]
                   [max-studio max-value]))))

  ;; then convert to json string
  (->> people-map
       (mapcat :films)
       (map :studio)
       frequencies
       (reduce (fn [[max-studio max-value] [studio value]]
                 (if (< max-value value)
                   [studio value]
                   [max-studio max-value])))
       json/encode)

  (->> people-map
       (sequence (comp (mapcat :films)
                       (map :studio)))
       frequencies
       (reduce
        (fn [[_ max-value :as max]  [_ value :as curr]]
          (if (< max-value value)
            curr
            max)))
       json/encode)

  (most-popular-studio people-map)
  (def people2 (atom []))
  (add-person people2 {:first-name "Dexter" :last-name "Howe-Jones"})

  people2



  )
