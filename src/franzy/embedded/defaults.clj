(ns franzy.embedded.defaults
  (:require [franzy.common.io.filesystem :as files])
  (:import (java.util UUID)))

(defn make-default-log-directory []
  (files/make-temp-absolute-path "ek" (str (UUID/randomUUID))))

(defn default-config []
  {:host.name                   "localhost"
   :port                        9092
   :broker.id                   0
   :advertised.host.name        "localhost"
   :replica.socket.timeout.ms   5000
   :log.flush.interval.messages 1
   :log.dir                     (make-default-log-directory)
   :zookeeper.connect           "localhost:2181"})
