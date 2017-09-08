(ns franzy.admin.types
  "Types for working with Kafka servers.")

(defrecord TopicMetadata
  [topic partitions-metadata error error-code])

(defrecord TopicMetadataResponse
  [brokers correlation-id topics-metadata])

(defrecord PartitionMetadata
  [partition-id leader replicas isr error error-code])

(defrecord ConsumerThreadId
  [thread-id consumer])

(defrecord LeaderAndIsr
  [leader isr leader-epoch zk-version])

(defrecord Endpoint
  [id endpoints])

(defrecord BrokerEndpoint
  [id host port])

(defrecord Broker
  [id endpoints])

(defrecord ReassignedPartitionsContext
  [new-replicas])

(defrecord LeaderIsrAndControllerEpoch
  [controller-epoch leader-and-isr])

(defrecord PartitionAndReplica
  [topic partition replica])
