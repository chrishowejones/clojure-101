(ns clojure-101.sudoku
  (:require
   [clojure.core.logic :refer [all lvar succeed everyg run ==]]
   [clojure.core.logic.fd :as fd]))

(def hints
  [2 0 7 0 1 0 5 0 8
   0 0 0 6 7 8 0 0 0
   8 0 0 0 0 0 0 0 6
   0 7 0 9 0 6 0 5 0
   4 9 0 0 0 0 0 1 3
   0 3 0 4 0 1 0 2 0
   5 0 0 0 0 0 0 0 1
   0 0 0 2 9 4 0 0 0
   3 0 6 0 8 0 4 0 9])

(defn rows
  "split vector of lvars representing game board into rows"
  [vars]
  ;;  split into rows as a vector within a vector
  (->> vars
       (partition 9)
       (map vec)
       (into [])))

(defn get-square
  "Given rows and the toop left x y coord - return a sequence representing
  a square"
  [rows x y]
  (for [x2 (range x (+ x 3)) y2 (range y (+ y 3))]
    (get-in rows [x2 y2])))

(defn cols
  "Given rows return columns"
  [rows]
  ;; given rows create a sequence of vectors representing cols
  (apply map vector rows))

(defn init [vars hints]
  (if (seq vars)
    (let [hint (first hints)]
      (all
       (if-not (zero? hint)
         (== (first vars) hint)
         succeed)
       (init (next vars) (next hints))))
    succeed))

(defn sudokufd [hints]
 (let [vars (repeatedly 81 lvar)
       rows (rows vars)
       cols (cols rows)
       sqs (for [x (range 0 9 3)
                 y (range 0 9 3)]
             (get-square rows x y))]
   (run 1 [q]
     (== q vars)
     (everyg #(fd/in % (fd/domain 1 2 3 4 5 6 7 8 9)) vars)
     (init vars hints)
     (everyg fd/distinct rows)
     (everyg fd/distinct cols)
     (everyg fd/distinct sqs))))


(comment

  (count hints)

  (rows hints)
  (-> hints rows cols)

  (sudokufd hints)

  {:hints (rows hints)
   :solution (->> hints
                  sudokufd
                  first
                  rows)}

  (get-square (into [] (map vec (partition 9 hints))) 3 3)

  )
