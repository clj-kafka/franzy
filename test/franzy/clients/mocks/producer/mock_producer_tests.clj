(ns franzy.clients.mocks.producer.mock-producer-tests
  (:require [midje.sweet :refer :all]
            [schema.core :as s]
            [franzy.clients.producer.protocols :refer :all]
            [franzy.clients.mocks.producer.protocols :refer :all]
            [franzy.clients.mocks.protocols :refer :all]
            [franzy.common.metadata.protocols :refer :all]
            [franzy.clients.mocks.producer.client :as producer]
            [franzy.serialization.serializers :as serializers]
            [franzy.clients.producer.partitioners :as partitioners]
            [franzy.clients.producer.defaults :as defaults]
            [franzy.clients.producer.types :as pt])
  (:import (franzy.clients.producer.client FranzProducer)
           (org.apache.kafka.common KafkaException)))

(fact
  "A mock producer can be created using a real producer with the implementations swapped with mock implementations."
  (let [p (producer/make-mock-producer (serializers/keyword-serializer) (serializers/edn-serializer))]
    (nil? p) => false
    (instance? FranzProducer p) => true
    (.close p)))

(fact
  "A mock producer can accept a partitioner implementation when constructed."
  (with-open [p (producer/make-mock-producer true (partitioners/default-partitioner) (serializers/keyword-serializer) (serializers/edn-serializer) nil)]
    (nil? p) => false
    (instance? FranzProducer p) => true))

(fact
  "A mock producer can accept producer options when constructed."
  (with-open [p (producer/make-mock-producer true (partitioners/default-partitioner) (serializers/keyword-serializer) (serializers/edn-serializer) (defaults/make-default-producer-options))]
    (nil? p) => false
    (instance? FranzProducer p) => true))

(defn make-mock-producer
  ([] (make-mock-producer true))
  ([auto-complete?]
   (producer/make-mock-producer auto-complete? (partitioners/default-partitioner) (serializers/keyword-serializer) (serializers/edn-serializer) (defaults/make-default-producer-options))))

(fact
  "The standard protocols for a producer are implemented on a mock producer"
  (with-open [p (make-mock-producer)]
    (let [metadata (send-sync! p (pt/->ProducerRecord "distortion-pedals" 0 :big-muff "most excellent."))]
      metadata => {:topic "distortion-pedals" :partition 0 :offset 0})))

(fact
  "Producer history is viewable, in order, for testing producer output when using a mock producer, via history."
  (with-open [p (make-mock-producer)]
    (let [pr-first {:topic "overpriced-pedals" :partition 0 :key :boss-flanger :value "wooosh"}
          pr-second {:topic "overpriced-pedals" :partition 0 :key :boss-chorus :value "ooo-ooo-oo"}
          pr-third {:topic "overpriced-pedals" :partition 1 :key :behringer-geq :value "equalizing things"}]
      (send-sync! p pr-first)
      (send-sync! p pr-second)
      (send-sync! p pr-third)
      (history p) => [pr-first pr-second pr-third])))

(fact
  "The producer history is clearable."
  (with-open [p (make-mock-producer)]
    (let [pr-first {:topic "overpriced-pedals" :partition 0 :key :boss-flanger :value "wooosh"}
          pr-second {:topic "overpriced-pedals" :partition 0 :key :boss-chorus :value "ooo-ooo-oo"}
          pr-third {:topic "overpriced-pedals" :partition 1 :key :behringer-geq :value "equalizing things"}]
      (send-sync! p pr-first)
      (send-sync! p pr-second)
      (send-sync! p pr-third)
      (history p) => [pr-first pr-second pr-third]
      (clear-history! p)
      (empty? (history p)) => true)))

(fact
  "It is possible to force the next uncompleted call to the mock producer to be an exception."
  (with-open [p (make-mock-producer false)]
    (let [pr-first {:topic "overpriced-pedals" :partition 0 :key :boss-flanger :value "wooosh"}
          pr-second {:topic "overpriced-pedals" :partition 0 :key :boss-chorus :value "ooo-ooo-oo"}
          pr-third {:topic "overpriced-pedals" :partition 1 :key :behringer-geq :value "equalizing things"}
          _ (write-exception! p (KafkaException. "wtf"))
          f1 (send-async! p pr-first)
          f2 (send-async! p pr-second)
          f3 (send-async! p pr-third)])))

(fact
  "It is possible to force the next uncompleted call to the mock producer."
  (with-open [p (make-mock-producer false)]
    (let [pr-first {:topic "overpriced-pedals" :partition 0 :key :boss-flanger :value "wooosh"}
          pr-second {:topic "overpriced-pedals" :partition 0 :key :boss-chorus :value "ooo-ooo-oo"}
          pr-third {:topic "overpriced-pedals" :partition 1 :key :behringer-geq :value "equalizing things"}
          completed? (complete-next! p)
          f1 (send-async! p pr-first)
          f2 (send-async! p pr-second)
          f3 (send-async! p pr-third)]
      completed? => false)))

(fact
  "Metric calls work with a mock producer, but don't collect anything."
  (with-open [p (make-mock-producer)]
    (let [metric-results (metrics p)]
      (nil? metric-results) => false
      (empty? metric-results) => true
      (map? metric-results) => true)))
