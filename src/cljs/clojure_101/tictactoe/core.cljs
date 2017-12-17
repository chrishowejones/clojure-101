(ns clojure-101.tictactoe.core
  (:require
   [clojure-101.tictactoe.view.frame :as frame]
   [clojure-101.tictactoe.store :as store]))

(defn tic-tac-toe
  "Main entry point, assemble:
   * the game state
   * the game view"
  []
  [:div
   (frame/render @store/current-turn
                  {:on-restart #(store/send-event! :restart)
                   :on-undo #(store/send-event! :undo)
                   :on-move #(store/send-event! %)})
   [:div [:a {:href "/"} "go to the home page"]]])
