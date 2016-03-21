(ns franzy.examples.embedded.dev
  (:require [franzy.examples.embedded.kafka :as kafka]
            [franzy.examples.embedded.zookeeper :as zookeeper]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Naive dev environment
;;
;; Things to note:
;; 1. You shouldn't be using def for either the embedded zookeeper instance or kafka instance, instead create with a func
;;    and hold on to the reference somehow, in an atom, component, map, whatever
;; 2. This is the absolute barebones, see franzy-embedded and travel-zoo for more
;; 3. If you want to use this with examples, make sure your configuration.clj matches what is here, re-eval if you must
;; 4. I recommend using something similar to the stuart sierra component reloaded workflow. If someone wants an example
;;    I can provided one, but I'm leaving this simple REPL friendly version for people who just want to fire something up
;;    to evaluate.
;; 5. Make sure you bootstrap any topics if you use this with the other examples, see bootstrap.clj
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn start-env []
  (zookeeper/start-embedded-zk)
  (kafka/start-broker))

(defn stop-env []
  (kafka/stop-broker)
  (zookeeper/close-embedded-zk))
