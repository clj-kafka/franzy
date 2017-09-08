(ns franzy.clients.mocks.producer.client
  (:require [franzy.clients.mocks.producer.protocols :refer :all]
            [franzy.clients.mocks.protocols :refer :all]
            [franzy.clients.producer.client :refer :all]
            [franzy.clients.producer.partitioners :as partitioners]
            [franzy.clients.codec :as codec]
            [franzy.clients.producer.defaults :as defaults]
            [schema.core :as s]
            [franzy.clients.producer.schema :as ps])
  (:import (franzy.clients.producer.client FranzProducer)
           (org.apache.kafka.clients.producer MockProducer Partitioner)
           (org.apache.kafka.common.serialization Serializer)))

(extend-type FranzProducer
  KafkaMockProducerLifecycle
  (complete-next! [this]
    "Complete the earliest uncompleted call successfully."
    (.completeNext ^MockProducer (.producer this)))
  KafkaHistorian
  (history [this]
    "Get the list of sent records since the last call to (clear p)."
    (->> (.history ^MockProducer (.producer this))
         (codec/decode)))
  (clear-history! [this]
    "Clear the stored history of sent records."
    (.clear ^MockProducer (.producer this)))
  KafkaExceptionWriter
  (write-exception! [this e]
    "Complete the earliest uncompleted call with the given error."
    (.errorNext ^MockProducer (.producer this) e)))

(s/defn make-mock-producer :- FranzProducer
  ([key-serializer :- Serializer
    value-serializer :- Serializer]
    (make-mock-producer true nil key-serializer value-serializer nil))
  ([auto-complete? :- s/Bool
    key-serializer :- Serializer
    value-serializer :- Serializer]
    (make-mock-producer auto-complete? nil key-serializer value-serializer nil))
  ([auto-complete? :- s/Bool
    partitioner :- Partitioner
    key-serializer :- Serializer
    value-serializer :- Serializer
    options :- (s/maybe ps/ProducerOptions)]
    (-> (MockProducer. auto-complete? (or partitioner (partitioners/default-partitioner))
                       key-serializer value-serializer)
        (FranzProducer. (defaults/make-default-producer-options options)))))

