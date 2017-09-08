(ns franzy.examples.producer.partition-info
  (:require [franzy.clients.producer.client :as producer]
            [franzy.common.metadata.protocols :refer :all]
            [franzy.clients.producer.defaults :as pd]
            [franzy.serialization.serializers :as serializers]
            [franzy.serialization.nippy.serializers :as nippy-serializers]
            [franzy.examples.configuration :as config]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Producer Partition Info
;;
;; This example demonstrates how a consumer can query partition info from the cluster. This metadata is useful for a
;; making some pro-active choices about production and for diagnosing issues. It can also be used to implement your
;; own producer.
;;
;; Things + Stuff, a winning combination:
;;
;; 1. Not all your partitions may have a leader elected yet. As such, this will impact your production.
;; 2. Kafka is distributed. Read up on ISRs, replicas, and leaders related to partitioning to understand this info better.
;; 3. Sometimes a producer can be slow. This may be due to something happening with its partition leader. If you suspect
;;    this, it's easy to check this info.
;; 4. You get a nice Clojure map back here. Do with it as you see fit.
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn producer-partition-info []
  (let [pc {:bootstrap.servers config/kafka-brokers
            :client.id         "follow-the-gourd"}
        key-serializer (serializers/keyword-serializer)
        value-serializer (nippy-serializers/nippy-serializer)
        options (pd/make-default-producer-options)
        topic (:nippy-topic config/topics-to-create)]
    (with-open [p (producer/make-producer pc key-serializer value-serializer options)]
      ;;Suppose we're about to produce to a topic, and for some reason we want some information about its partitions....
      ;;Now suppose we also want to know how far along the latest offset is, purely for "personal" purposes.....
      ;;Suppose you just call below:
      (partitions-for p topic))))
