(ns franzy.admin.partitions
  (:require [franzy.admin.codec :as codec]
            [franzy.common.configuration.codec :as config-codec])
  (:import (kafka.utils ZkUtils)
           (kafka.admin AdminUtils RackAwareMode$Enforced$)
           (org.I0Itec.zkclient ZkClient)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Reading Topic Partitions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn partitions-for
  "Returns all partitions for each topic, given a collection of topic names."
  [^ZkUtils zk-utils topics]
  (->> topics
       (codec/encode)
       (.getPartitionsForTopics zk-utils)
       (codec/decode)))

(defn all-topic-partitions
  "Returns all partitions for all topics."
  [^ZkUtils zk-utils]
  (->> (.getAllPartitions zk-utils)
       (codec/decode)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Writing Topic Partitions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn redefine-partition-count!
  "Sets the number of partitions for a topic, given the topic, number of partitions, replica assignment, and a flag
  to check if the broker is available before attempting to redefine the count.

  Note that the number of partitions for a topic can only be increased without first deleting partitions.
  For example, if you have 5 partitions for a given topic, you can set the number of partitions to 10, but not to 5, 4, 3, etc...

  Please be sure you know what you are doing when you repartitioning a topic.
  Repartitoning may result in unintended consequences regarding your key ordering.
  If you are not concerned about ordering, have not added data to your topic yet,
  or otherwise can deal with ordering issues via replay or in your application itself,
  then you may use this method to increase the amount of partitions for your topic.

  An AdminOperationException will be thrown if you attempt to redefine an invalid number of partitions.

  > Note: This method may be changed/depricated given the state of this API in the official Kafka branch."
  ([^ZkUtils zk-utils ^String topic partitions]
    ;;TODO: verify if blank replica doesn't cause any issues
   (redefine-partition-count! zk-utils topic partitions "" true))
  ([^ZkUtils zk-utils topic partitions replica-assignment check-broker-available?]
   (redefine-partition-count! zk-utils topic partitions replica-assignment check-broker-available? RackAwareMode$Enforced$/MODULE$))
  ([^ZkUtils zk-utils topic partitions replica-assignment check-broker-available? rack-aware-mode]
   (AdminUtils/addPartitions zk-utils topic (int partitions) replica-assignment check-broker-available? rack-aware-mode)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Replicas
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn partition-replica-assignment
  "Returns the assigned replicas for a given topic and partition.

  Returns a vector of assigned replica IDs

  Example:

  `[1001 1002]`"
  ([^ZkUtils zk-utils {:keys [topic partition]}]
   (partition-replica-assignment zk-utils topic partition))
  ([^ZkUtils zk-utils ^String topic partition]
   (->> partition
        (.getReplicasForPartition zk-utils topic)
        (codec/decode))))

(defn partition-in-sync-replicas
  "Returns the in-sync replicas for a topic partition."
  ([^ZkUtils zk-utils {:keys [topic partition]}]
   (partition-in-sync-replicas zk-utils topic partition))
  ([^ZkUtils zk-utils ^String topic partition]
   (->> partition
        (.getInSyncReplicasForPartition zk-utils topic)
        (codec/decode))))

(defn partition-leader
  "Returns the partition leader (if any) for a topic partition.

  There may be no leader if one is not elected yet, in which case nil is returned."
  ([^ZkUtils zk-utils {:keys [topic partition]}]
   (partition-leader zk-utils topic partition))
  ([^ZkUtils zk-utils ^String topic partition]
   (->> partition
        (.getLeaderForPartition zk-utils topic)
        (codec/decode))))

(defn partition-leaders-for
  "Retrives the leaders (if any) for each partition in a given list of topics.

  Each topic name will be the key in the returned map. Each topic may have multiple partitions, and each partition
  will have a list of partition leaders.

  Example Output:

  `{:taco-bars {2 [1001], 1 [0], 0 [1001]}, :fajita-facts {2 [1001], 1 [1002], 0 [1001]}}`"
  [^ZkUtils zk-utils topics]
  (->> (codec/encode topics)
       (.getPartitionAssignmentForTopics zk-utils)
       (codec/decode)))

;;perhaps a rational human can explain to me why this ALSO requires a ZkClient , when ZkUtils is constructed with one...
;;someone s-it the bed and then slept in it perhaps.
;; ... and finally someone heard you ;) zk-client is gone ;)
(defn partition-leaders-metadata
  [^ZkUtils zk-utils topic-partitions]
  "Retrieves the leaders (if any) for each topic and partition map provided."
  (let [topics-and-partitions (->> topic-partitions
                                   (codec/sequential->topic-and-partitions)
                                   (codec/encode))]
    (->> (.getPartitionLeaderAndIsrForTopics zk-utils topics-and-partitions)
         (codec/decode))))

(defn partition-leader-metadata
  "Returns the partition leader (if any), a list of in-sync-replicas, and other metadata for a topic partition."
  ([^ZkUtils zk-utils {:keys [topic partition]}]
   (partition-leader-metadata zk-utils topic partition))
  ([^ZkUtils zk-utils ^String topic partition]
   (->> partition
        (.getLeaderAndIsrForPartition zk-utils topic)
        (codec/decode))))

(defn partitions-undergoing-reassignment
  [^ZkUtils zk-utils]
  "Returns a map of topic partitions being reassigned and their associated reassignment metadata."
  (->>
    (.getPartitionsBeingReassigned zk-utils)
    (codec/decode)))

(defn partitions-undergoing-replica-election
  "Returns partitions undergoing preferred replica election."
  [^ZkUtils zk-utils]
  (->>
    (.getPartitionsUndergoingPreferredReplicaElection zk-utils)
    (codec/decode)))

(defn partition-epoch
  "Returns the topic epoch from the ISR path in Zookeeper."
  ([^ZkUtils zk-utils {:keys [topic partition]}]
   (partition-epoch zk-utils topic partition))
  ([^ZkUtils zk-utils ^String topic partition]
   (.getEpochForPartition zk-utils topic partition)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Zookeeper
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn write-topic-partition-assignment-path!
  "Given a topic and map of partition replica assignments, creates or updates the partition assignment path with a given config. "
  [^ZkUtils zk-utils ^String topic partition-replica-assignment config update?]
  (->
    (AdminUtils/createOrUpdateTopicPartitionAssignmentPathInZK zk-utils topic (codec/encode partition-replica-assignment)
                                                               (config-codec/encode config) update?)
    (codec/decode)))

(defn topic-partition-path
  "Returns the Zookeeper topic partition path."
  [^String topic partition]
  (ZkUtils/getTopicPartitionPath topic (int partition)))

(defn topic-partitions-path
  "Returns the path for partitions for a given topic in Zookeeper."
  [^String topic]
  (ZkUtils/getTopicPartitionsPath topic))

(defn topic-partition-isr-leader-path
  "Returns the topic partition leader and in-sync replica path in Zookeeper."
  [^String topic partition]
  (ZkUtils/getTopicPartitionLeaderAndIsrPath topic (int partition)))

(defn reassign-partitions-path []
  "Returns the path used to reassign partitions in Zookeeper."
  (ZkUtils/ReassignPartitionsPath))

(defn replica-leader-election-path []
  "Returns the preferred replica leader election path in Zookeeper."
  (ZkUtils/PreferredReplicaLeaderElectionPath))

(defn isr-change-notification-path []
  "Returns the ISR change notifications path in Zookeeper."
  (ZkUtils/IsrChangeNotificationPath))
