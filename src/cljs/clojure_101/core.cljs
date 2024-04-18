(ns clojure-101.core
    (:require [reagent.core :as reagent :refer [atom]]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [clojure-101.tictactoe.core :as ttt]))

;; -------------------------
;; Views

(defn home-page []
  [:div [:h2 "Welcome to clojure-101 demo"]
   [:div [:a {:href "/about"} "go to about page"]]
   [:div [:a {:href "/tictactoe"} "go to play tic-tac-toe"]]])

(defn about-page []
  [:div [:h2 "About clojure-101"]
   [:div [:a {:href "/"} "go to the home page"]]])

;; -------------------------
;; Routes

(defonce page (atom #'home-page))

(defn current-page []
  [:div [@page]])

(secretary/defroute "/" []
  (reset! page #'home-page))

(secretary/defroute "/about" []
  (reset! page #'about-page))

(secretary/defroute "/tictactoe" []
  (reset! page #'ttt/tic-tac-toe))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))

(comment

  (js/alert "Hi Chris")
  @page
  (reset! page #'home-page)
  (reset! page #'about-page)
  (reset! page #'ttt/tic-tac-toe)



  )
