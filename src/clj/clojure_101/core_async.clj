(ns clojure-101.core-async
  (:require [clojure.core.async
             :refer
             [<! <!! >! >!! alts! alts!! chan close! go thread]]))

(let [c (chan 10)]
  (>!! c "hello")
  (let [val (<!! c)]
    (close! c)
    val))


(let [c (chan)]
  (thread (>!! c "hello in a thread"))
  (let [val (<!! c)]
    (close! c)
    val))

(let [c (chan)]
  (go (>! c "hello from go block"))
  (let [val (<!! (go (<! c)))]
   (close! c)
   val))

(let [c1 (chan)
      c2 (chan)]
  (thread (while true
              (let [[v ch] (alts!! [c1 c2])]
                (println "Read" v "from" ch))))
  (>!! c1 "hi")
  (>!! c2 "there"))

(let [c1 (chan)
      c2 (chan)]
  (go (while true
          (let [[v ch] (alts! [c1 c2])]
            (println "Read" v "from" ch "in go block"))))
  (go (>! c1 "hi"))
  (go (>! c2 "there")))
