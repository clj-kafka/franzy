(ns franzy.examples.embedded.kafka
  (:require [franzy.embedded.broker :as broker]
            [franzy.embedded.protocols :refer :all]
            [franzy.embedded.defaults :as defaults])
  (:import (franzy.embedded.broker StartableEmbeddedKafkaBroker)))

(def embedded-kafka-config
  {:host.name                   "127.0.0.1"
   :port                        9092
   :broker.id                   (defaults/next-broker-id)
   :advertised.host.name        "127.0.0.1"
   :replica.socket.timeout.ms   5000
   :log.flush.interval.messages 1
   :zookeeper.connect           "127.0.0.1:2181"})

(def kafka-broker
  (broker/make-startable-broker embedded-kafka-config))

(defn start-broker []
  (startup kafka-broker))

(defn stop-broker []
  (.close ^StartableEmbeddedKafkaBroker kafka-broker))

