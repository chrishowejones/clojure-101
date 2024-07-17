(ns clojure-101.api
  (:require
   [cheshire.core :as json]
   [clojure-101.api-spec :as api-spec]
   [clojure.spec.alpha :as s]
   [compojure.core :refer [defroutes GET POST]]
   [ring.util.response :refer [content-type response status]]
   [clojure-101.postgres :as postgres]))

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

(defn most-popular-studio-db [ds]
  (-> ds
      postgres/find-popular-studio
      first
      json/generate-string))

(defn add-person
  "Accepts a map representing a Person and stores it. Returns Person or returns error map if Person is illegal spec."
  [people unvalidated-person]
  (let [person (s/conform ::api-spec/person unvalidated-person)
        add-person-with-id  (fn [people person]
                              (let [id (inc (apply max (into [0] (map :id people))))]
                                (conj people (assoc person :id id))))]
    (if (s/invalid? person)
      (assoc {} :error  (s/explain-str ::api-spec/person unvalidated-person))
      (->> person
           (swap! people add-person-with-id)
           first))))

(defn find-films-for-people
  [people ds]
  (letfn [(get-films-for-person [person]
            (->> person
                 :id
                 (postgres/find-films-for-person ds)
                 (assoc person :films)))]
    (map get-films-for-person people)))

(defn get-all-people
  [{ds :ds}]
  (-> (postgres/find-all-people ds)
      (find-films-for-people ds)
      response
      (content-type "application/json")))

(defn add-person-to-db [ds unvalidated-person]
  (let [person (s/conform ::api-spec/person unvalidated-person)]
    (if (s/invalid? person)
      (assoc {} :error  (s/explain-str ::api-spec/person unvalidated-person))
      (let [person-minus-films (dissoc person :films)
            films (:films person)
            person-in-db (postgres/create-person ds person-minus-films)
            assoc-films (fn [films] (if (seq films)
                                      (assoc person-in-db :films films)
                                      person-in-db))]
        (->> films
             (postgres/create-films-for-person ds (:id person-in-db))
             (map #(dissoc % :id :person-id))
             assoc-films)))))

(defroutes routes
  (GET "/" [] "add some links to routes here.")
  (GET "/people" []
    (-> @people
        response
        (content-type "application/json")))
  (GET "/peopledb" req
    (get-all-people req))
  (GET "/popular-studio" []
    (-> @people
        most-popular-studio
        response
        (content-type "application/json")))
  (GET "/popular-studio-db" {ds :ds}
    (-> (most-popular-studio-db ds)
        response
        (content-type "application/json")))
  (POST "/people" req
    (let [person-json (-> req :body slurp)]
      (-> (response (add-person people (json/decode person-json true)))
          (status 201)
          (content-type "application/json"))))
  (POST "/peopledb" {ds :ds :as req}
    (let [person-json (-> req :body slurp)]
      (-> (add-person-to-db ds (json/decode person-json true))
          response
          (status 201)
          (content-type "application/json")))))

(comment

  @people

  (def people-test [{:id 1,
                     :first-name "Chris",
                     :last-name "Howe-Jones",
                     :films
                     [{:title "Star Wars: Episode IV - A New Hope",
                       :studio "20th Century Fox",
                       :release-year "1977"}
                      {:title "Raiders of the Lost Ark",
                       :studio "Paramount",
                       :release-year "1981"}
                      {:title "The Godfather",
                       :studio "Paramount",
                       :release-year "1972"}]}
                    {:id 2,
                     :first-name "Cerys",
                     :middle-name "Eilonwy",
                     :last-name "Howe-Jones",
                     :films
                     [{:title "Truman Show", :studio "Paramount", :release-year "1998"}
                      {:title "Elemental", :studio "Disney", :release-year "2023"}
                      {:title "Up", :studio "Disney", :release-year "2009"}
                      {:title "Cinderella", :studio "Disney", :release-year "1950"}]}
                    {:id 3,
                     :first-name "Danielle",
                     :last-name "Howe-Jones",
                     :nickname "Dan"}])

  (remove #(= 1 (:id %)) people-test)

  (swap! people (fn [people] (remove #(= 1 (:id %)) people-test)))


  (add-person people {:first-name "Jonny" :last-name "Hobbs" :films [{:title "Toy Story" :release-year "1995" :studio "Disney"}]})

  (reset! people (into [] (json/decode people-json true)))

  (frequencies ["fred" "fred" "bill" "chris" "fred"])

  (-> @people
      most-popular-studio
      response
      (content-type "application/json"))

  )
