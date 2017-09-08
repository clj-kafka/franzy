(ns franzy.examples.producer.list-topics
  (:require [franzy.clients.consumer.client :as consumer]
            [franzy.common.metadata.protocols :refer :all]
            [franzy.examples.configuration :as config]
            [franzy.serialization.nippy.deserializers :as nippy-deserializers]
            [franzy.serialization.deserializers :as deserializers]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Consumer Topics List
;;
;; This example demonstrates how a producer can query topic metadata from the cluster. This metadata is useful for a
;; making some pro-active choices about consumptions and for diagnosing issues. It can also be used to implement your
;; own consumer.
;;
;; A note from your broadcasting partners:
;;
;; 1. Not all your partitions may have a leader elected yet. As such, this will impact which topics you can consume from.
;; 2. Kafka is distributed. Read up on ISRs, replicas, and leaders related to partitioning to understand this info better.
;; 3. Sometimes a producer can be slow. This may be due to something happening with its partition leader. If you suspect
;;    this, it's easy to check this info.
;; 4. You get a nice Clojure map back here. Do with it as you see fit.
;; 5. Each partition may have a different state. Pay attention. You can stall your consumers if you're waiting on a partition
;;    depending on your options. This info can help.
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn consumer-topics-list []
  (let [cc {:bootstrap.servers config/kafka-brokers
            :group.id          "love-in-the-time-of-phone-apps"
            :client.id         "spoons-too-big"}
        key-serializer (deserializers/keyword-deserializer)
        value-serializer (nippy-deserializers/nippy-deserializer)]
    (with-open [p (consumer/make-consumer cc key-serializer value-serializer)]
      ;;Suppose we created a consumer and we want to see what topics are available to consumer from....
      ;;We could use admin, or just do this:
      (list-topics p)
      ;;pick a topic and enjoy its riches or see which topics are having issues, blown up, etc.
      )))
