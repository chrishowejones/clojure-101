(ns clojure-101.config
  (:require [aero.core :as aero]
            [clojure.java.io :as io]))

(def config
  (delay (some-> (io/resource "config.edn")
                 aero/read-config)))

(comment
  (System/getenv "USER")
  (System/getenv "db_spec")

  (:database @config)
  ((fnil aero/read-config {}) (io/resource "config.edn"))

;;
  )
