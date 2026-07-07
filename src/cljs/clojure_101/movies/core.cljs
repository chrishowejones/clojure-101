(ns clojure-101.movies.core
  (:require [reagent.core :as r]
            [cljs-http.client :as http])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defonce person (r/atom nil))

(defn add-film-page
  []
  [:div "Add a film page"])

(defn- film-attributes-view
  [title studio release-year]
  [:div.text-left
   [:div.row
    [:div.col-4 "Title:"]
    [:div.col-8 title]]
   (when studio
     [:div.row
      [:div.col-4 "Studio:"]
      [:div.col-8 studio]])
   (when release-year
     [:div.row
      [:div.col-4 "Year of release:"]
      [:div.col-8 release-year]])])

(defn list-films-panel
  []
  [:div
   (if @person
     [:div
      [:row [:h2 "Films"]]
      (for [{:keys [title studio release-year]} (:films @person)]
        [:div {:key title}
         (film-attributes-view title studio release-year)
         [:hr]])]
     [:p "Loading data..."])])

(defn movies-index-page
  [id]
  (let [fetch-data! (fn []
                      (go (let [response (<! (http/get "http://localhost:3000/api/peopledb/c8818314-b2fe-46f4-8157-e0f7c57ffde3"
                                                       {:with-credentials? false}))]
                            ;; Update the atom, which automatically triggers a UI re-render
                            (reset! person (:body response)))))]
    ;; Trigger the API call immediately when the component initializes
    (fetch-data!)

    (fn []
      [:div.container-fluid
       [:link
        {:type "text/css"
         :href "https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css"
         :rel "stylesheet"}]
       [:div.navbar.navbar-dark.bg-dark.shadow-sm
        [:div.container.d-flex.justify-content-between
         [:h1.navbar-brand.align-items-center.text-light "Persons Films"]
         [:a {:href "/"} "Home"]]]
       [:section
        [:div.container.jumbotron.bg-white.text-center
         (when-let [{:keys [first-name last-name]} @person]
           [:div
            [:row [:h2 "Person"]
             [:div.text-left
              [:div.row
               [:div.col-4 "First Name:"]
               [:div.col-6 first-name]]
              [:div.row
               [:div.col-4 "Last Name:"]
               [:div.col-6 last-name]]]]
            [:hr]
            [:row [:p]]
            [list-films-panel]])
         [:row [:p]]
         [:row
          [:p
           [:a.btn.btn-primary {:href "/add-film"} "Add a Film"]]]]]])))


(comment

  (def responseAtom (atom nil))

  (go
    (let [response (<! (http/get "https://www.bbc.co.uk/"))]
      (reset! responseAtom response)))

  @responseAtom

  (-> (js/fetch "https://www.bbc.co.uk/")
      (.then (fn [res] (js/console.log "status" (.-status res))))
      (.catch (fn [err] (js/console.log "fetch error" err))))

  (go
    (let [response (<! (http/get "http://localhost:3000/api/peopledb" {:with-credentials? false}))]
      (reset! responseAtom response)))

  (js/console.log js/XMLHttpRequest)

  ,)
