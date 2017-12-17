(ns clojure-101.tictactoe.view.frame
  (:require
    [clojure-101.tictactoe.view.board :as board]
    [clojure-101.tictactoe.view.panel :as panel]))


(defn render
  "Rendering the main frame of the game,
   takes as input the callbacks to trigger events"
  [{:keys [board] :as turn} callbacks]
  [:div
   [panel/render-top-panel turn callbacks]
   [board/render-board board callbacks]])
