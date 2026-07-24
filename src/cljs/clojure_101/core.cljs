(ns clojure-101.core
  (:require [clojure-101.movies.core :as movies]
            [clojure-101.person.core :as person]
            [clojure-101.tictactoe.core :as ttt]
            [reagent.core :as reagent :refer [atom]]
            [reitit.frontend :as rf]
            [reitit.frontend.controllers :as rfc]
            [reitit.frontend.easy :as rfe]))

;; -------------------------
;; Views

(defn home-page []
  [:div [:h2 "Welcome to clojure-101 demo"]
   [:div [:a {:href "/about"} "go to about page"]]
   [:div [:a {:href "/tictactoe"} "go to play tic-tac-toe"]]
   [:div [:a {:href "/search-users"} "go to user search"]]])

(defn about-page []
  [:div [:h2 "About clojure-101"]
   [:div [:p "This is a simple reagent app and a RESTful API in Clojure."]]
   [:div [:a {:href "/"} "go to the home page"]]])

;; -------------------------
;; Routes

(defonce page (atom home-page))
(defonce route-match (atom nil))

(def routes
  [["/" {:name :home :view (fn [] [home-page])}]
   ["/about" {:name :about :view (fn [] [about-page])}]
   ["/tictactoe" {:name :tictactoe :view (fn [] [ttt/tic-tac-toe])}]
   ["/search-users" {:name :search-users
                     :view (fn [] [person/search-users-page])
                     :controllers [{:start (fn [_] (reset! person/response-atom nil))}]}]
   ["/movies/:id" {:name :movies
                   :view (fn [{:keys [path]}]
                           [movies/movies-index-page (:id path)])
                   :controllers [{:parameters {:path [:id]}
                                  :start (fn [{:keys [path]}]
                                           (movies/fetch-person! (:id path)))
                                  :stop (fn [_]
                                          (reset! movies/person nil)
                                          (reset! movies/message nil))}]}]
   ["/movies/:id/add-film" {:name :add-film
                            :view (fn [{:keys [path]}] [movies/add-film-page (:id path)])
                            :controllers [{:parameters {:path [:id]}
                                           :start (fn [{:keys [path]}]
                                                    (movies/fetch-person! (:id path)))
                                           :stop (fn [_]
                                                   (reset! movies/person nil))}]}]])

(defn current-page []
  (when @route-match
    (let [view (:view (:data @route-match))]
      [view (:parameters @route-match)])))

(defn init-routes! []
  (rfe/start!
   (rf/router routes)
   (fn [new-match]
     (swap! route-match
            (fn [old-match]
              (when new-match
                (assoc new-match
                       :controllers (rfc/apply-controllers (:controllers old-match) new-match))))))
   {:use-fragment false}))  ;; false = real paths like /movies/123, true = /#/movies/123

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (init-routes!)
  (mount-root))

(comment

  (js/alert "Hi Chris")
  @page
  (rfe/push-state :home)
  (rfe/push-state :about)
  (rfe/push-state :tictactoe)
  (rfe/push-state :movies {:id "c8818314-b2fe-46f4-8157-e0f7c57ffde3"})


  ,)
