(ns franzy.clients.mocks.consumer.protocols)

(defprotocol MockKafkaConsumerLifecycle
  (closed? [this]))

(defprotocol KafkaPoller
  (schedule-nop-poll! [this])
  (schedule-poll! [this ^Runnable task]))

(defprotocol KafkaRecordWriter
  (add-record! [this consumer-record]))

(defprotocol KafkaPartitionRebalancer
  (rebalance! [this topic-partitions]))

(defprotocol KafkaOffsetWriter
  (beginning-offsets! [this topic-partition-offset-map])
  (ending-offsets! [this topic-partition-offset-map]))

(defprotocol KafkaPartitionWriter
  (update-partitions! [this topic partitions-info]))
