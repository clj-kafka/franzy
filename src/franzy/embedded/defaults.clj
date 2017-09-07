(ns franzy.embedded.defaults
  (:require [franzy.common.io.filesystem :as files])
  (:import (java.util UUID)))

(defn make-default-log-directory []
  (files/make-temp-absolute-path "ek" (str (UUID/randomUUID))))

(def broker-id (atom 0))

(defn next-broker-id
  ([] (next-broker-id false))
  ([reset-broker-id?]
   (if reset-broker-id?
     (reset! broker-id 0)
     (swap! broker-id inc))))

(defn default-config
  ([] (default-config false))
  ([reset-broker-id?]
   {:host.name                   "127.0.0.1"
    :port                        9092
    :broker.id                   (next-broker-id reset-broker-id?)
    :advertised.host.name        "127.0.0.1"
    :replica.socket.timeout.ms   5000
    :log.flush.interval.messages 1
    :log.dir                     (make-default-log-directory)
    :zookeeper.connect           "127.0.0.1:2181"}))
