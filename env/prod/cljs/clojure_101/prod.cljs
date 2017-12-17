(ns clojure-101.prod
  (:require [clojure-101.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
