(ns clojure-101.movies.api
  (:require
   [reagent.core :as r])
  (:require-macros
    [reagent.ratom :refer [reaction]]))

(defonce films (r/atom {}))

(defn fetch-all-films
  []
  ())
