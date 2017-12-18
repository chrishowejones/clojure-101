(ns clojure-101.tictactoe.logic.board)

(def size "The size of the board" 3)

(def coordinates
  (for [x (range size) y (range size)] [x y]))

(def coordinates? (set coordinates))
(def empty-board (zipmap coordinates (repeat :owner/none)))

(defn get-owner-at
  "Get the owner associated to the cell"
  [board coord]
  {:pre [(coordinates? coord)]}
  (get board coord))

(defn has-owner?
  "Check whether the coord has an owner associated to it"
  [board coord]
  {:pre [(coordinates? coord)]}
  (not= (get-owner-at board coord) :owner/none))

(defn convert-cell
  "Assign the cell [x y] to a new player"
  [board player coord]
  {:pre [(coordinates? coord)
         (not (has-owner? board coord))]}
  (assoc board coord player))

(defn full-board?
  "Verifies whether the board has any empty cell left"
  [board]
  (not-any? #{:owner/none} (vals board)))

(comment

  coordinates

  coordinates?

  empty-board

  (convert-cell empty-board :owner/circle [0 0])

  (full-board? empty-board)

  (def full
    (zipmap coordinates (repeat :owner/cross)))

  full

  (full-board? full)

  )
