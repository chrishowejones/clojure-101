(ns clojure-101.movies.core
  (:require [reagent.core :as r]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [reitit.frontend.easy :as rfe])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defonce person (r/atom nil))
(defonce message (r/atom nil))

(defn fetch-person! [id]
  (go (let [response (<! (http/get (str "http://localhost:3000/api/peopledb/" id)
                                   {:with-credentials? false}))]
        ;; Update the atom, which automatically triggers a UI re-render
        (reset! person (:body response)))))

(defn add-film-form
  []
  (let [form-state (r/atom {:title "" :studio "" :release-year ""})]
    (fn []
      (let [{:keys [title studio release-year]} @form-state]
        [:form {:on-submit (fn [e]
                             (let [id (:id @person)]
                               (.preventDefault e)
                               (println "Submitted:" @form-state)
                               (go (let [response (<! (http/post (str "http://localhost:3000/api/peopledb/" id "/film")
                                                                 {:with-credentials? false
                                                                  :json-params @form-state}))]
                                     (println response)
                                     (reset! message {:message "Film added!" :status :primary})
                                     (rfe/push-state :movies {:id id})))))}
         [:div.row.text-left
          [:label.col-4 "Title:"]
          [:input.col-6 {:type "string"
                         :value title
                         :on-change (fn [e]
                                      (println (.. e -target -value))
                                      (swap! form-state (fn [v]
                                                          (assoc v :title (.. e -target -value)))))}]]
         [:div.row.text-left
          [:label.col-4 "Studio:"]
          [:input.col-6 {:type "string"
                         :value studio
                         :on-change (fn [e]
                                      (println (.. e -target -value))
                                      (swap! form-state (fn [v]
                                                          (assoc v :studio (.. e -target -value)))))}]]
         [:div.row.text-left
          [:label.col-4 "Release year:"]
          [:input.col-6 {:type "number"
                         :min "1900"
                         :max "2099"
                         :step "1"
                         :value release-year
                         :on-change (fn [e]
                                      (println (.. e -target -value))
                                      (swap! form-state (fn [v]
                                                          (assoc v :release-year (.. e -target -value)))))}]]
         [:div.row [:p]]
         [:div.row.d-flex.justify-content-center.align-content-center
          [:button.btn-primary {:type "submit"} "Save"]]
         [:hr]]))))

(defn add-film-page
  []
  (fn []
    [:div.container-fluid
     [:link
      {:type "text/css"
       :href "https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css"
       :rel "stylesheet"}]
     [:div.navbar.navbar-dark.bg-dark.shadow-sm
      [:div.container.d-flex.justify-content-between
       [:h1.navbar-brand.align-items-center.text-light "Add Film"]
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
          [add-film-form]])]]]))

(defn- film-attributes-view
  [id title studio release-year]
  (fn []
    [:form {:on-submit (fn [e]
                         (.preventDefault e)
                         (go (let [response (<! (http/delete (str "http://localhost:3000/api/film/" id)
                                                           {:with-credentials? false}))]
                               (println "Deleted!: " response)
                               (if (>= (:status response) 400)
                                 (reset! message {:message "Film not deleted!" :status :danger})
                                 (reset! message {:message "Film deleted!" :status :primary}))
                               (fetch-person! (:id @person)))))}
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
         [:div.col-8 release-year]])
      [:div.row [:p]]
      [:div.row.d-flex.justify-content-center.align-content-center
       [:button.btn-primary {:type "submit"} "Delete"]]]]))

(defn list-films-panel
  []
  [:div
   (if @person
     [:div
      [:row [:h2 "Films"]]
      (for [{:keys [id title studio release-year]} (:films @person)]
        [:div {:key title}
         [film-attributes-view id title studio release-year]
         [:hr]])]
     [:p "Loading data..."])])

(defn movies-index-page
  [id]
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
       (when-let [{:keys [message status]} @message]
         (cond
           (= :primary status) [:div.alert.alert-primary {:role "alert"} message]
           (= :danger status) [:div.alert.alert-danger {:role "alert"} message]))
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
         [:a.btn.btn-primary {:href (rfe/href :add-film {:id id})} "Add a Film"]]]]]]))


(comment

  (def responseAtom (atom nil))

  (go
    (let [response (<! (http/get "https://www.bbc.co.uk/"))]
      (reset! responseAtom response)))

  @responseAtom

  @message

  (-> (js/fetch "https://www.bbc.co.uk/")
      (.then (fn [res] (js/console.log "status" (.-status res))))
      (.catch (fn [err] (js/console.log "fetch error" err))))

  (go
    (let [response (<! (http/get "http://localhost:3000/api/peopledb" {:with-credentials? false}))]
      (reset! responseAtom response)))

  (rfe/push-state :movies {:id (:id @person)})

  ,)
