(ns clojure-101.api
  (:require [cheshire.core :as json]
            [clojure-101.api-spec :as api-spec]
            [clojure-101.database :as database]
            [clojure-101.postgres :as postgres]
            [clojure.spec.alpha :as s]
            [clojure.tools.logging :as log]
            [compojure.core :refer [defroutes DELETE GET POST]]
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

(defn get-all-people
  [fetch-people fetch-films-for-person]
  (-> (fetch-people)
      (database/find-films-for-people fetch-films-for-person)
      response
      (content-type "application/json")))

(defn get-person
  [fetch-person fetch-films-for-person]
  (-> (fetch-person)
      (database/find-films-for-person fetch-films-for-person)
      response
      (content-type "application/json")))

(defn get-people-by-name [fetch-people-by-name fetch-films-for-person]
  (-> (fetch-people-by-name)
      (database/find-films-for-people fetch-films-for-person)
      response
      (content-type "application/json")))

(defn create-person
  "Create a new person. Takes a map representing an unvalidated person, a function to store a new person and a function to store new films for a person.
   Returns a map representing the newly stored person with their films."
  [unvalidated-person store-new-person store-new-films]
  (let [person (s/conform ::api-spec/person unvalidated-person)]
    (if (s/invalid? person)
      (assoc {} :error  (s/explain-str ::api-spec/person unvalidated-person))
      (database/create-person store-new-person store-new-films person))))

(defn create-film
  "Create a new film for a person. Takes a map representing a person id, an unvalidated film, a function to store a new film.
   Returns a map representing the newly stored film."
  [person-id unvalidated-film store-new-films]
  (let [film (s/conform ::api-spec/film unvalidated-film)]
    (if (s/invalid? film)
      (assoc {} :error  (s/explain-str ::api-spec/film unvalidated-film))
      (database/create-film person-id film store-new-films))))

(defn delete-film
  "Delete a film. Takes an id for the film to be deleted and a function to execute against the database."
  [film-id delete-film-db]
  (let [db-response (delete-film-db film-id)]
    (if (= 1 (:next.jdbc/update-count db-response))
      {:status :ok}
      {:error :not-deleted})))

(defn post-person
  "Takes a json string representing a person and their films, a function to store a new person and a function to store new films for that person.
   Returns an HTTP response with a body containing the newly stored person with films or an error."
  [person-json store-new-person store-new-films]
  (let [new-person (create-person person-json store-new-person store-new-films)
        wrap-person-in-response (fn [status-code] (-> new-person
                                                      response
                                                      (status status-code)
                                                      (content-type "application/json")))]
    (if (:error new-person)
      (wrap-person-in-response 500)
      (wrap-person-in-response 201))))

(defn post-film
  "Takes a json string representing a film for a person, a function to store new films for that person.
   Returns an HTTP response with a body containing the newly stored person with films or an error."
  [person-id film-json store-new-films]
  (let [new-film (create-film person-id film-json store-new-films)
        wrap-film-in-response (fn [status-code] (-> new-film
                                                    response
                                                    (status status-code)
                                                    (content-type "application/json")))]
    (if (:error new-film)
      (wrap-film-in-response 500)
      (wrap-film-in-response 201))))

(defn remove-film
  "Takes a film id, a function to delete the film.
   Returns an HTTP response with a body containing the count of films deleted or an error"
  [film-id delete-film-db]
  (let [delete-film-response (delete-film (parse-uuid film-id) delete-film-db)
        wrap-film-in-response (fn [status-code] (-> delete-film-response
                                                    response
                                                    (status status-code)
                                                    (content-type "application/json")))]
    (if (:error delete-film-response)
      (wrap-film-in-response 404)
      (wrap-film-in-response 204))))

(defroutes routes
  (GET "/" [] "I updated this.")
  (GET "/people" []
    (-> @people
        response
        (content-type "application/json")))
  (GET "/peopledb" req
    (let [{ds :ds} req
          fetch-all-people (partial postgres/find-all-people ds)
          fetch-films-for-person (partial postgres/find-films-for-person ds)]
      (get-all-people fetch-all-people fetch-films-for-person)))
  (GET "/peopledb/:id" [id :as req]
    (let [{:keys [ds]} req
          fetch-person (partial postgres/find-person ds id)
          fetch-films-for-person (partial postgres/find-films-for-person ds)]
      (get-person fetch-person fetch-films-for-person)))
  (POST "/peopledb/search" req
    (let [{:keys [ds body]} req
          name (:name body)
          fetch-people-by-name (fn [] (postgres/find-people-by-name ds name))
          fetch-films-for-person (partial postgres/find-films-for-person ds)]
      (get-people-by-name fetch-people-by-name fetch-films-for-person)))
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
  (POST "/peopledb" {ds :ds person-json :body}
    (let [store-new-person (partial postgres/create-person ds)
          store-new-films (partial postgres/create-films-for-person ds)]
      (post-person person-json store-new-person store-new-films)))
  (POST "/peopledb/:id/film" {ds :ds params :params film :body}
    (let [{:keys [id]} params
          store-new-film (partial postgres/create-films-for-person ds)]
      (log/info "create film:" film ", id:" id)
      (post-film id film store-new-film)))
  (DELETE "/film/:id" {ds :ds params :params}
          (let [{:keys [id]} params
                delete-film-db (partial postgres/delete-film ds)]
            (log/info "delete film id:" id)
            (remove-film id delete-film-db))))

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

  (remove (comp not nil?) [nil 1 nil nil 2])

  ;;
  )
