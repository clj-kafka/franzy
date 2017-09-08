(ns franzy.admin.topics
  (:require [franzy.admin.codec :as codec]
            [franzy.common.configuration.codec :as config-codec])
  (:import (kafka.utils ZkUtils)
           (kafka.admin AdminUtils)
           (scala.collection Set)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Reading Topics
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn topic-exists? [^ZkUtils zk-utils ^String topic]
  "Checks to see if a particular topic exists, by topic name."
  (AdminUtils/topicExists zk-utils topic))

(defn all-topics
  "Returns a lazy sequence of all Kafka topics visible in the current connection."
  [^ZkUtils zk-utils]
  (->> (.getAllTopics zk-utils)
       (codec/decode)))

(defn topics-by-consumer-group
  "Returns all the topics for a particular consumer group name."
  [^ZkUtils zk-utils ^String group]
  (->> (.getTopicsByConsumerGroup zk-utils group)
       (codec/decode)))

(defn topics-metadata
  [^ZkUtils zk-utils topics]
  "Returns a list of topic metadata from Zookeeper, given one or more topics as a set or string."
  (some->
    (cond
      (set? topics)
      topics
      ;;because people don't list....maybe remove
      (sequential? topics)
      (into #{} topics)
      (string? topics)
      #{topics}
      :else (throw (ex-info "Topics must be a set, sequential collection, or a string." {:topics topics})))
    ^Set (codec/encode)
    (AdminUtils/fetchTopicMetadataFromZk zk-utils)
    (codec/decode)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Writing to Topics
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn create-topic!
  "Creates a topic with a given name, number of partitions, replication factor, and optional topic configuration.

  Topic configuration should be specified by a Clojure map, example: {\"cleanup.policy\" \"compact\"},
  which will be converted to Java Properties, or alternatively via Java Properties directly.

  If no replication factor is provided, a replication factor of 1 is assumed.

  If the topic already exists in Zookeeper, a TopicExistsException will be thrown,
  If the topic otherwise collides with an existing topic, an InvalidTopicException will be thrown.

  Note: It may take some time for a topic and its partitions to propogate and the client isn't notified and doesn't block waiting for this automatically.
  Be careful if auditing the topic and its partitions before this happens.
  Inconsistent ZK state may interfere with queries that pull from related Zookeeper nodes. Typically You will receive a NoNodeException in these cases.
  If your application requires constant polling of admin state, either try to block until the operation completes and/or check the associated Zookeeper nodes manually using a Zookeeper client."

  ([^ZkUtils zk-utils ^String topic partitions]
   (create-topic! zk-utils topic partitions 1 nil))
  ([^ZkUtils zk-utils ^String topic partitions replication-factor]
   (create-topic! zk-utils topic partitions replication-factor nil))
  ([^ZkUtils zk-utils ^String topic partitions replication-factor topic-config]
   (->> (or topic-config {})
        (config-codec/encode)
        (AdminUtils/createTopic zk-utils topic (int partitions) (int (or replication-factor 1))))))

(defn delete-topic!
  "Marks a topic for deletion.
  After a topic has been marked for deletion, it will no longer accept requests to produce or consume.

  Note that this does not mean the topic will be deleted immediately. If for example you check if the topic exists, it may still return true.

  If the topic has already been marked for deletion in Zookeeper, a TopicAlreadyMarkedForDeletionException will be thrown.
  If another error occurs, an AdminOperationException will be thrown."
  [^ZkUtils zk-utils ^String topic]
  (AdminUtils/deleteTopic zk-utils topic))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Replicas
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn topic-replica-assignments
  "Returns the assigned replicas for a given list of topics.

  Returns a vector of maps where the key is the topic partition and the values are a vector of assigned replica IDs.

  Example:

  `{{:topic \"cheeses\", :partition 0} [1001], {:topic \"gigantic-cheeses\", :partition 0} [1001]}`"
  [^ZkUtils zk-utils topics]
  (->>
    (codec/encode topics)
    (.getReplicaAssignmentForTopics zk-utils)
    (codec/decode)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Zookeeper
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn delete-topics-path []
  "Returns the Zookeeper path used to delete topics."
  (ZkUtils/DeleteTopicsPath))

(defn delete-topic-path [^String topic]
  "Returns the Zookeeper path used to delete a topic."
  (ZkUtils/getDeleteTopicPath topic))

(defn topic-path [^String topic]
  "Returns the path in Zookeeper to a given topic."
  (ZkUtils/getTopicPath topic))
