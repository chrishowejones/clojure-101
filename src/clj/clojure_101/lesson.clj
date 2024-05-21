(ns clojure-101.lesson
  (:require [clojure.string :as string]))

;; number literals
1
;; Data structures

;; lists -constructing
(list 1 2 3)
`(1 2 3)

;; the first item in every list is a function!
(+ 1 2 3)
(inc 10)
(dec 10)
(str [1 2 3])

;; accessing lists (and sequences)
(first (list 1 2 3))
(second (list 1 2 3))
(nth (list 1 2 3) 2)

;; strings
"Chris"
(str `chris)
(str 123)
(string/upper-case "chris")

;; vectors
[1 2 3]
(vector 1 2 3)
(vec (list 1 2 3))
(vec {:a 1 :b 2})

;; keywords
:a
:name
(keyword "key")

;; maps
{:a 1 :name "Chris" 1 2}
(get {:a 1 :name "Chris" 1 2} :a)
(get {:a 1 :name "Chris" 1 2} :name)
(get {:a 1 :name "Chris" 1 2} 1)
(get {:a 1 :name "Chris" 1 2} :z)
(get {:a 1 :name "Chris" 1 2 :z nil} :z)
(get {:a 1 :name "Chris" 1 2} :z :missing)
(get {:a 1 :name "Chris" 1 2 :z nil} :z :missing)
({:a 1 :name "Chris" 1 2} :a)
(:a {:a 1 :name "Chris" 1 2})

(:name {:a 1 :name "Chris" 1 2})
(def me {:name "Chris" :title "Jedi"})
me
(:name me)
(def me-nil nil)
(:name me-nil)
;; (me-nil :name)

;;(1 {:a 1 :name "Chris" 1 2})

;; sets
#{1 2 3}
(set [1 1 2 2 3])
;; #{1 2 3 1}

#{"1" 2 "Chris" 1}

;; let scope
(let [first-name "Chris"]
  (string/upper-case first-name))


;; conditionals
;; if
(if nil "true" "false")

(if false "true" "false")

(if 0 "true" "false")

(if true "true" "false")

(if [] "true" "false")


;; cond
(let [n 1]
 (cond
   (< n 2) "n is less than 2"
   (> n 2) "n is greater than 2"
   :else "else"))

;; def and immutablility
(def a 1)
a
(let [a 2]
  a)
a

;; fns
(def my-str (fn [x] (str x)))
(my-str 123)

(defn my-str2 [x] (str x))
(my-str2 123)

;; more immutability
(def nums [1 2 3])

nums
;; => [1 2 3]
(conj nums 4)
;; => [1 2 3 4]
nums
;; => [1 2 3]
(let [nums (conj nums 4)]
  nums)
;; => [1 2 3 4]
(conj nums 4)

nums
;; => [1 2 3]
