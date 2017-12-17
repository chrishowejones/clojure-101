(ns clojure-101.tictactoe.view.board
  (:require
    [clojure-101.tictactoe.logic.board :as board]
    [clojure-101.tictactoe.view.svg.cell :as cell]
    [clojure-101.tictactoe.view.svg.constants :as cst]
    [clojure-101.tictactoe.view.svg.utils :as utils]))

(defn render-board
  "Render the board:
   * Creates a SVG panel
   * Render the cells in it"
  [board {:keys [on-move]}]
  (utils/square-svg-panel
    {:model-size board/size
     :pixel-size cst/board-pixel-size}
    (for [cell board]
      [cell/render-cell cell on-move])))
