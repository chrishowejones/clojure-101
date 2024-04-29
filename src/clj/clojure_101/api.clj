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
  {\"title\":\"Star Wars: Episode IV – A New Hope\",\"studio\":\"20th Century Fox\",\"release-year\":\"1977\"},
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
  "takes in map of people, extracts films and within that studio then determines
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
                              (let [id (inc (apply max (map :id people)))]
                                (conj people (assoc person :id id))))]
    (if (s/invalid? person)
      (assoc {} :error  (s/explain-str ::api-spec/person person-decoded))
      (->> person
           (swap! people add-person-with-id)
           first))))

(defn- overwrite-films
  [people id films]
  (let [person (first (filter #(= id (:id %)) people))
        person-films-replaced (assoc person :films films)]
    (-> (remove #(= id (:id %)) people)
        (conj person-films-replaced))))

(defn add-films
  "Accepts the people atom, the id of a person and map representing the films to be added to that person (replacing any existing films).
   Returns Person with films or returns error map if Films is illegal spec."
  [people id films-unvalidated]
  (let [films (s/conform ::api-spec/films (:films films-unvalidated))]
    (if (s/invalid? films)
      (assoc {} :error (s/explain-str ::api-spec/films (:films films-unvalidated)))
      (swap! people overwrite-films id films))))

(defroutes routes
  (GET "/" [] "add some links to routes here.")
  (GET "/people" [] (-> @people
                        response
                        (content-type "application/json")))
  (GET "/popular-studio" [] (-> @people
                                most-popular-studio
                                response
                                (content-type "application/json")))
  (POST "/people/:id/films" [id :as req]
    (let [films (-> req :body slurp (json/decode true))]
      (-> people
          (add-films (read-string id) films)
          response
          (status 201)
          (content-type "application/json"))))
  (POST "/people" req
    (let [person-json (-> req :body slurp)]
      (-> (response (add-person people (json/decode person-json true)))
          (status 201)
          (content-type "application/json")))))

(comment

  @people

  (s/conform ::api-spec/people [{:first-name "Cerys" :last-name "howe-jones" :id 1
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

  (add-person people {:first-name "Jonny" :last-name "Hobbs"})
  (swap! people first)
  @people

  (reset! people (json/decode people-json true))

  (reduce (fn [[mk mv] [k v]] (if (< mv v) [k v] [mk mv])) [{:studio ""} 0] {})

  (frequencies
   [{:studio "fred"} {:studio "fred"}])

  )
