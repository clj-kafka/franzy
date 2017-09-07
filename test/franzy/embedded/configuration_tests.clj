(ns franzy.embedded.configuration-tests
  (:require [midje.sweet :refer :all]
            [franzy.embedded.defaults :as defaults]
            [franzy.embedded.configuration :as config])
  (:import (clojure.lang IPersistentMap)
           (kafka.server KafkaConfig)))

(facts
  "Kafka configs can be created a few ways"
  (fact
    "You can create and view the broker defaults."
    (instance? IPersistentMap (defaults/default-config)) => true)
  (fact
    "You can make a concrete Kafka configuration object."
    (instance? KafkaConfig (config/make-kafka-config)) => true)
  (fact
    "You can pass a config map and it will be created as a Kafka config."
    (instance? KafkaConfig (config/make-kafka-config {:host.name         "127.0.0.1"
                                                      :port              9092
                                                      :broker.id         0
                                                      :zookeeper.connect "127.0.0.1:2181"})) => true)
  (fact
    "You cannot pass values in your config map that are not valid Kafka configs."
    (config/make-kafka-config {:creepy-cereal-mascots ["Lucky Charms Guy" "Toucan Sam" "Raisin Bran Sun"]}) => (throws Exception)))
