(ns clojure-101.tictactoe.logic.board)

(def size "The size of the board" 3)

(def coordinates
  ;; TODO implement using for
  )

(def coordinates? (set coordinates))
(def empty-board ;; TODO implement using zipmap and repeat
  )

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
  ;; TODO complete implementation
  )

(defn full-board?
  "Verifies whether the board has any empty cell left"
  [board]
  ;; implement using not-any?
  )

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
