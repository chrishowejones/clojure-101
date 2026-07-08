(ns clojure-101.person.core
  (:require
   [cljs-http.client :as http]
   [cljs.core.async :refer [go]]
   [reitit.frontend.easy :as rfe]
   [reagent.core :as r]))

(defonce response-atom (r/atom nil))

(defn search-form []
  (let [form-state (r/atom {:name ""})]
    (fn []
      [:form {:on-submit (fn [e]
                           (.preventDefault e)
                           (println "Submitted:" @form-state)
                           (go (let [response (<! (http/post "http://localhost:3000/api/peopledb/search"
                                                             {:with-credentials? false
                                                              :json-params @form-state}))]
                                 (println response)
                                 (reset! response-atom response))))}
       [:label "Name:"]
       [:input {:type "string"
                :value (:name @form-state)
                :on-change (fn [e]
                             (println (.. e -target -value))
                             (swap! form-state (fn [v]
                                                 (assoc v :name (.. e -target -value)))))}]
       [:button {:type "submit"} "Search"]])))

(defn user-row
  [first-name last-name id]
  [:div.row
   [:div.col-4 "Name:"]
   [:div.col-6 (str first-name " " last-name)]
   [:div.col-2 [:a.btn.btn-primary {:href (rfe/href :movies {:id id})} "View"]]])

(defn search-users-page []
  [:div.container-fluid
   [:link
    {:type "text/css"
     :href "https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css"
     :rel "stylesheet"}]
   [:div.navbar.navbar-dark.bg-dark.shadow-sm
    [:div.container.d-flex.justify-content-between
     [:h1.navbar-brand.align-items-center.text-light "Search for Person"]
     [:a {:href "/"} "Home"]]]
   [:section
    [:div.container.jumbotron.bg-white.text-center
     [search-form]
     (when-let [body (:body @response-atom)]
       [:div
        [:hr]
        [:row [:h2 "People"]]
        (for [{:keys [first-name last-name id]} body]
          [:div.text-left {:key (str first-name ":" last-name)}
           [user-row first-name last-name id]])])]]])

(comment
  @response-atom
  )
