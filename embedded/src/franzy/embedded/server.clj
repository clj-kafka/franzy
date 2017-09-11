(ns franzy.embedded.server
  (:require [franzy.embedded.configuration :as config])
  (:import (kafka.server KafkaServerStartable KafkaServer)
           (org.apache.kafka.common.utils SystemTime)
           ()
           (scala.collection.immutable List)))

(defn make-server
  "Creates an Embedded Kafka Server.

  A map of broker configuration values may be passed to specify any desired broker parameters,
  otherwise a default setting is used that will start the broker on your local host with the default ports.

  Please prefer passing your own configuration unless the defaults match
  your desired usage."
  (^KafkaServer [] (make-server nil nil))
  (^KafkaServer [broker-config] (make-server broker-config nil))
  (^KafkaServer [broker-config thread-name-prefix]
   (-> broker-config
       (config/make-kafka-config)
       (KafkaServer. (SystemTime.) thread-name-prefix (List/empty)))))

(defn make-startable-server
  "Creates a startable version of an Embedded Kafka Server with slightly less exposed functionality.

  This server may be more amenable for command-line usage."
  (^KafkaServerStartable [] (make-startable-server nil))
  (^KafkaServerStartable
  [broker-config]
   (-> broker-config
       (config/make-kafka-config)
       (KafkaServerStartable.))))
