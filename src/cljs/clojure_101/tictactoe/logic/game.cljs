(ns clojure-101.tictactoe.logic.game
  (:require
    [clojure-101.tictactoe.logic.turn :as turn]))


(defn new-game []
  [turn/start-turn])

(defn current-turn
  [game]
  (peek game))

(defn play-move
  "Play current player move at the provided coordinate"
  [game coord]
  (if-let [new-turn (turn/next-turn (current-turn game) coord)]
    (conj game new-turn)
    game))

(defn undo-last-move
  "Remove the last game if there is enough game played"
  [game]
  (if (< 1 (count game)) (pop game) game))

(defn handle-event
  "Callback to dispath the event on the game"
  [game event]
  (cond
    (= event :restart) (new-game)
    (= event :undo) (undo-last-move game)
    :else (play-move game event)))
