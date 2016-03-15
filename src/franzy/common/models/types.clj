(ns franzy.common.models.types
  (:require [schema.core :as s]
            [franzy.common.models.schema :as fs]))

(defrecord TopicPartition
  [topic partition])

(s/defn make-topic-partition :- fs/TopicPartition
  [m]
  (map->TopicPartition m))

(defrecord Node
  [id host port])

(s/defn make-node :- fs/Node
  [m]
  (map->Node m))

(defrecord MetricName
  [name description group tags])

(s/defn make-metric-name :- fs/KafkaMetricName
  [m]
  (map->MetricName name))

(defrecord Metric [metric-name value])

(s/defn make-metric :- fs/KafkaMetric
  [m]
  (map->Metric m))

(defrecord PartitionInfo
  [topic partition leader replicas in-sync-replicas])

(s/defn make-partition-info :- fs/PartitionInfo
  [m]
  (map->PartitionInfo m))
