(ns franzy.examples.producer.producer-metrics
  (:require [taoensso.timbre :as timbre]
            [franzy.clients.producer.protocols :refer :all]
            [franzy.common.metadata.protocols :refer :all]
            [franzy.common.models.types :as mt]
            [franzy.examples.configuration :as config]
            [franzy.clients.producer.client :as producer]
            [franzy.serialization.serializers :as serializers]
            [franzy.serialization.nippy.serializers :as nippy-serializers]
            [franzy.clients.producer.types :as pt])
  (:import (java.util UUID)))

(defn measure-metrics []
  (let [cc {:bootstrap.servers config/kafka-brokers
            :client.id         "crunch-n-munch"}
        topic (:nippy-topic config/topics-to-create)
        first-partition 0
        second-partition 1
        third-partition 2
        ;;just for fun, demonstrating using a topic partition as a record
        topic-partitions [(mt/->TopicPartition topic 0)]
        key-serializer (serializers/keyword-serializer)
        value-serializer (nippy-serializers/nippy-serializer)]
    (with-open [p (producer/make-producer cc key-serializer value-serializer)]
      ;;first, let's produce a bit to generate some stats....
      ;;we'd need to do a lot more to get anything inteteresting
      (let [producer-record-map-p1 {:topic     topic
                                    :partition first-partition
                                    ;;let's use a string UUID key just because.....
                                    :key       (-> (UUID/randomUUID) (str) (keyword))
                                    :value     "the glory of love"}
            producer-record-map-p2 {:topic     topic
                                    :partition second-partition
                                    :key       (-> (UUID/randomUUID) (str) (keyword))
                                    :value     {:name        "Jarome"
                                                :occupations ["torturer" "stacher" "swordsmith slayer"]}}
            producer-record-p3 (pt/->ProducerRecord topic third-partition (-> (UUID/randomUUID) (str) (keyword))
                                                    {:will-kill-you-tomorrow true
                                                     :was-roberts-before-me  true
                                                     :bad-ideas              ["Selling crack online" "Starting singing TV shows"

                                                                              "Betting against Michael Ironside being in a military role in a show or movie"]})]
        ;;let us begin the assault of contrived data.....
        (dotimes [_ 50]
          (send-sync! p producer-record-map-p1)
          (send-sync! p producer-record-map-p2)
          (send-sync! p producer-record-p3))
        ;;now we measure our weak actions
        (metrics p)))))
