(ns clojure-101.cards
  (:require [reagent.core :as reagent :refer [atom]]
            [clojure-101.tictactoe.store :as store]
            [clojure-101.core :as core]
            [clojure-101.tictactoe.core :as ttt]
            [devcards.core :as dc])
  (:require-macros
   [devcards.core
    :as dc
    :refer [defcard defcard-doc defcard-rg deftest]]))

(defcard-rg first-card
  [:div>h1 "This is your first devcard! It's not very interesting is it!"])

(defcard-rg home-page-card
  [core/home-page])

(defcard-rg tic-tac-toe-page-card
   "Tic Tac Toe page"
  [ttt/tic-tac-toe]
  store/app-state
  {:inspect-data true})

(defcard-rg rg-example
  "some docs"
  (fn [data-atom _] [:div "this works as well"
                     [:p (str "My counter = " (dec (:counter @data-atom)))]])
  (reagent/atom {:counter 10})
  {:inspect-data true})


(reagent/render [:div] (.getElementById js/document "app"))

;; remember to run 'lein figwheel devcards' and then browse to
;; http://localhost:3449/cards
