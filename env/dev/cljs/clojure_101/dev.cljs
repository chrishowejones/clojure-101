(ns ^:figwheel-no-load clojure-101.dev
  (:require
    [clojure-101.core :as core]
    [devtools.core :as devtools]))

(devtools/install!)

(enable-console-print!)

(core/init!)
