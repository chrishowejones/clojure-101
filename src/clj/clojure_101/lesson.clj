(ns clojure-101.lesson
  (:require [clojure.string :as string]))

;; number literals
1
;; Data structures

;; lists -constructing
(list 1 2 3)
`(1 2 3)

;; accessing lists (and sequences)
(first (list 1 2 3))
(second (list 1 2 3))
(nth (list 1 2 3) 2)


;; the first item in every list is a function!
(+ 1 2 3)
(inc 10)
(dec 10)
(str [1 2 3])

;; strings
"Chris"
(str `chris)
(str 123)

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
;; => ClassCastException class java.lang.Long cannot be cast to class clojure.lang.IFn (java.lang.Long is in module java.base of loader 'bootstrap'; clojure.lang.IFn is in unnamed module of loader 'app')  clojure-101.lesson/eval19782 (form-init17792907167632930503.clj:45)

;; sets
#{1 2 3}
;; => #{1 3 2}
;; #{1 2 3 1}
;; => IllegalArgumentException Duplicate key: 1  clojure.lang.PersistentHashSet.createWithCheck (PersistentHashSet.java:68)
#{"1" 2 "Chris"}
;; => #{2 "Chris" "1"}


;; let scope
(let [name "Chris"]
  (string/upper-case name))
;; => "CHRIS"


;; conditionals
;; if
(if nil "true" "false")
;; => "false"
(if false "true" "false")
;; => "false"
(if 0 "true" "false")
;; => "true"
(if true "true" "false")
;; => "true"
(if [] "true" "false")
;; => "true"

;; cond
(cond
  (< 2 1) "1 is less than 2"
  (> 1 2) "1 is greater than 2"
  :else "else")
;; => "1 is less than 2"

;; def and immutablility
(def a 1)

a
(let [a 2]
  a)
a

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
nums
;; => [1 2 3]

;; have fn
((fn [x] (+ x 1)) 10)
;; => 11
(def increment (fn [x] (+ x 1)))
(increment 10)
;; => 11

(defn increment-again [x]
  (+ x 1))
(increment-again 10)
;; => 11
