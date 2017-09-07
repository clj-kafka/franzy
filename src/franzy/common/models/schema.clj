(ns franzy.common.models.schema
  "Schemas used throughout Kafka"
  (:require [schema.core :as s]
            [franzy.common.schema :as fs])
  (:import (java.util.concurrent TimeUnit)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Schemas that are used nearly everywhere inside Kafka
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def TopicPartition
  "Schema for a pairing of topic and partition, as used by Kafka to uniquely target topics and partitions"
  {(s/required-key :topic)     fs/NonEmptyString
   (s/optional-key :partition) fs/SPosInt                   ;TODO: may want to make this required
   })

(def Node
  "Schema for a Kafka Node."
  {(s/required-key :id)   s/Int
   (s/required-key :host) fs/NonEmptyString
   (s/required-key :port) fs/SPosInt
   })

(def KafkaMetricName
  "Schema for a Kafka MetricName."
  ;;based on the constructor avalanche, this is what it seems to be, however could makes these all required
  {(s/required-key :name)        fs/NonEmptyString
   (s/optional-key :description) (s/maybe s/Str)
   (s/required-key :group)       s/Str
   (s/optional-key :tags)        (s/maybe {s/Keyword fs/NonEmptyString})})

(def KafkaMetric
  "Schema for a KafkaMetric."
  {(s/required-key :metric-name) fs/NonEmptyString
   ;;Kafka returns -Infinity/Infinity since not nullable double, however we might want nil or to drop this key
   (s/required-key :value)       (s/maybe fs/CoercableDouble)})

;;Note sure if any of the below values can be maybe, but adding in case Kafka/Codec returns crazy stuff
(def PartitionInfo
  "Schema for Kafka PartitionInfo"
  {(s/required-key :topic)            fs/NonEmptyString
   (s/required-key :partition)        fs/SPosInt
   (s/required-key :leader)           (s/maybe Node)
   (s/required-key :replicas)         (s/maybe [Node])
   (s/required-key :in-sync-replicas) (s/maybe [Node])})

(def Cluster
  "Schema for Kafka client cluster metadata."
  {(s/optional-key :nodes) [Node]
   (s/optional-key :partitions) [PartitionInfo]
   (s/optional-key :unauthorized-topics) [s/Str]})

(def SecurityProtocolEnum
  "Schema for a Security Protocol."
  ;;TODO: better, cleaner keyword support
  ;(apply s/enum (SecurityProtocol/getNames))
  ;;These must be uppercase when they are sent to Kafka
  (s/enum "PLAINTEXT", "SSL", "SASL_PLAINTEXT", "SASL_SSL", "TRACE"
          :plaintext :ssl :sasl_plaintext :sasl_ssl))

;;TODO: better enum handling here, should accept keywords
(def TimeUnitEnum
  "Schema for a time unit."
  (apply s/enum (TimeUnit/values)))

(def KafkaAck
  "Schema for a Kafka ack value."
  (s/enum "-1", "0", "1", "all", -1, 0, 1))

(def BootstrapServerAddress
  "Schema for a Kafka Bootstrap Server Address."
  {(s/required-key :host) s/Str
   (s/required-key :port) fs/SPosInt})

(def CleanupPolicy
  "Schema for Cleanup Policy"
  (s/enum "delete" "compact" :delete :compact))

;;TODO: more specific types - these need to be checked against Kafka source still, just throwing general reasonable defaults
(def TopicConfiguration
  "Schema for Kafka Topic Configuration

  See http://kafka.apache.org/documentation.html#topic-config"
  {(s/optional-key :cleanup.policy)            CleanupPolicy
   (s/optional-key :delete.retention.ms)       fs/SPosLong
   (s/optional-key :flush.messages)            fs/SPosLong
   (s/optional-key :flush.ms)                  fs/SPosLong
   (s/optional-key :index.interval.bytes)      fs/SPosLong
   (s/optional-key :max.message.bytes)         fs/SPosLong
   (s/optional-key :min.cleanable.dirty.ratio) fs/PosDouble
   (s/optional-key :min.insync.replicas)       fs/PosInt
   (s/optional-key :retention.bytes)           fs/SPosLong
   (s/optional-key :retention.ms)              fs/SPosLong
   (s/optional-key :segment.bytes)             fs/SPosLong
   (s/optional-key :segment.index.bytes)       fs/SPosLong
   (s/optional-key :segment.ms)                fs/SPosLong
   (s/optional-key :segment.jitter.ms)         fs/SPosLong})
