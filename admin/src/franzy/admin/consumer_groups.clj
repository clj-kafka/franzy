(ns franzy.admin.consumer-groups
  (:require [franzy.admin.codec :as codec]
            [franzy.common.configuration.codec :as config-codec])
  (:import (kafka.utils ZkUtils)
           (kafka.admin AdminUtils)
           (org.I0Itec.zkclient.exception ZkNoNodeException)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Reading Consumer Groups
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn consumer-groups
  "Returns all consumer groups in in the scope of the current connection."
  [^ZkUtils zk-utils]
  (->> (.getConsumerGroups zk-utils)
       (codec/decode)))

(defn consumer-groups-by-topic [^ZkUtils zk-utils ^String topic]
  "Returns all the consumer groups for a given topic name."
  (->> (.getAllConsumerGroupsForTopic zk-utils topic)
       (codec/decode)))

(defn consumers-groups-per-topic
  "Returns all the consumers for each topic that are part of a given consumer group,
  optionally excluding internal topics."
  [^ZkUtils zk-utils ^String group ^Boolean exclude-internal-topics?]
  (->>
    (.getConsumersPerTopic zk-utils group exclude-internal-topics?)
    (codec/decode)))

(defn consumer-group-active?
  "Checks to see if a provided consumer group name is active.

  If the group does not exist, throws a Zookeeper ZkNoNodeException internally."
  [^ZkUtils zk-utils ^String group]
  (try
    (AdminUtils/isConsumerGroupActive zk-utils group)
    (catch ZkNoNodeException e)))

(defn consumers-in-group
  "Returns a list of consumers in a group.

  If the group does not exist in Zookeeper, a ZkNoNodeException will be thrown internally, but caught."
  [^ZkUtils zk-utils ^String group]
  (try
    (->> (.getConsumersInGroup zk-utils group)
         (codec/decode))
    (catch ZkNoNodeException e)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Writing Consumer Groups
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn delete-consumer-group!
  "Deletes the whole directory of the given consumer group in Zookeeper if the group is inactive.

  Returns true if succeeded."
  [^ZkUtils zk-utils ^String group]
  (AdminUtils/deleteConsumerGroupInZK zk-utils group))


;;TODO: can probably combine this with delete-consumer-group-in-zk and just dispatch accordingly
(defn delete-consumer-groups-for-topic!
  "Deletes consumer groups for a topic in Zookeeper if the group is inactive.
  If the group is provided, only the given group will be deleted, otherwise
  all groups are deleted.

  Throws a NoNodeException if the consumer group does not exist in Zookeeper."
  ([^ZkUtils zk-utils ^String topic]
   (AdminUtils/deleteAllConsumerGroupInfoForTopicInZK zk-utils topic))
  ([^ZkUtils zk-utils ^String topic ^String group]
   (AdminUtils/deleteConsumerGroupInfoForTopicInZK zk-utils group topic)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Zookeeper
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn consumer-partition-owner-path
  "Returns the path in Zookeeper for a consumer group name, topic, and partition."
  [^ZkUtils zk-utils ^String group ^String topic ^Integer partition]
  (.getConsumerPartitionOwnerPath zk-utils group topic partition))

(defn consumers-path []
  "Returns the consumers path in Zookeeper."
  (ZkUtils/ConsumersPath))
