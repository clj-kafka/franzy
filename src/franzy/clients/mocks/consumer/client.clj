(ns franzy.clients.mocks.consumer.client
  (:require [franzy.clients.consumer.protocols :refer :all]
            [franzy.clients.mocks.consumer.protocols :refer :all]
            [franzy.clients.mocks.protocols :refer :all]
            [franzy.clients.consumer.client :refer :all]
            [franzy.clients.codec :as codec]
            [franzy.clients.consumer.defaults :as defaults]
            [schema.core :as s]
            [franzy.clients.consumer.schema :as cs])
  (:import (franzy.clients.consumer.client FranzConsumer)
           (org.apache.kafka.clients.consumer MockConsumer)))

(extend-type FranzConsumer
  KafkaRecordWriter
  (add-record! [this consumer-record]
    (->>
      consumer-record
      (codec/map->consumer-record)
      (.addRecord ^MockConsumer (.consumer this))))
  MockKafkaConsumerLifecycle
  (closed? [this]
    (.closed ^MockConsumer (.consumer this)))
  KafkaPartitionRebalancer
  (rebalance! [this topic-partitions]
    "Simulate a rebalance event."
    (->> topic-partitions
         (map codec/map->topic-partition)
         (.rebalance ^MockConsumer (.consumer this))))
  KafkaPoller
  (schedule-nop-poll [this]
    (.scheduleNopPollTask ^MockConsumer (.consumer this)))
  (schedule-poll [this task]
    (.schedulePollTask ^MockConsumer (.consumer this) task))

  KafkaOffsetWriter
  (beginning-offsets! [this topic-partition-offset-map]
    "Given a map of topic partitions to offset numbers,
    sets the beginning offset for each topic partition key to the given offset value."
    (->> topic-partition-offset-map
         (codec/map->topic-partition-offset-number)
         (.updateBeginningOffsets ^MockConsumer (.consumer this))))
  (ending-offsets! [this topic-partition-offset-map]
    "Given a map of topic partitions to offset numbers,
    sets the beginning offset for each topic partition key to the given offset value."
    (->> topic-partition-offset-map
         (codec/map->topic-partition-offset-number)
         (.updateEndOffsets ^MockConsumer (.consumer this))))
  KafkaPartitionWriter
  (update-partitions! [this topic partitions-info]
    "Updates the partition info for a topic, given a list of topic partition info.

    Example:

    `(update-partitions! c topic {:topic topic :partition 0
                                  :leader   {:id 1234 :host \"127.0.0.1\" :port 2112}
                                  :replicas [{:id 1234 :host \"127.0.0.1\" :port 2112}]
                                  :in-sync-replicas [{:id 1234 :host \"127.0.0.1\" :port 2112}]})`"
    (->> partitions-info
         (map codec/map->partition-info)
         (.updatePartitions ^MockConsumer (.consumer this) topic)))
  KafkaExceptionWriter
  (write-exception! [this e]
    "Complete the earliest uncompleted call with the given error."
    (.setException ^MockConsumer (.consumer this) e)))

(s/defn make-mock-consumer :- FranzConsumer
  ([offset-reset-strategy]
    (make-mock-consumer offset-reset-strategy nil))
  ([offset-reset-strategy :- (s/either s/Keyword s/Str)
    options :- (s/maybe cs/ConsumerOptions)]
    (-> offset-reset-strategy
        (codec/keyword->offset-reset-strategy)
        (MockConsumer.)
        (FranzConsumer. (defaults/make-default-consumer-options options)))))
