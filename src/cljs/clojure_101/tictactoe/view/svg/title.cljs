(ns clojure-101.tictactoe.view.svg.title
  (:require [clojure-101.tictactoe.logic.turn :as turn]
            [clojure.string :as s]))


(defn- get-winner-name
  [turn]
  (if-let [winner (turn/get-winner turn)]
    (s/capitalize (name winner))))

(defn get-title
  [turn]
  (if-let [winner-name (get-winner-name turn)]
    (str winner-name " wins")
    (if (turn/game-over? turn)
      "Draw game"
      "Naughts and Crosses")))

(comment

  (get-winner-name @clojure-101.tictactoe.store/current-turn)



  )
