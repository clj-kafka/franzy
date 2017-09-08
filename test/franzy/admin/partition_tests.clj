(ns franzy.admin.partition-tests
  (:require [midje.sweet :refer :all]
            [franzy.admin.schema :as fas]
            [franzy.admin.topics :as topics]
            [franzy.admin.partitions :refer :all]
            [franzy.admin.core-test :as core-test]
            [schema.core :as s]
            [franzy.common.models.schema :as fs])
  (:import (java.util UUID)))

;;TODO: use midje before/after
(with-open [zk-utils (core-test/make-zk-utils)]
  (let [topic (str (UUID/randomUUID))
        topic-to-repartition (str (UUID/randomUUID))
        partitions 3
        topics [topic topic-to-repartition]]
    (topics/create-topic! zk-utils topic partitions)
    (topics/create-topic! zk-utils topic-to-repartition partitions)
    (facts
      "Topic partitions can be administered."
      (fact
        "All partitions for a topic can be listed."
        ;;ex: {:great-successes [0 1 2]}
        (let [parts (partitions-for zk-utils topics)
              part-coll (get parts (keyword topic))]
          (coll? parts) => true
          (empty? parts) => false
          (map? parts) => true
          (sequential? part-coll)
          (nil? part-coll) => false
          (empty? part-coll) => false
          part-coll => (has every? integer?)))
      (fact
        "All topic partitions can be listed."
        ;;ex:
        ;; [{:topic "brogrammers", :partition 0}
        ;;{:topic "brogrammers", :partition 1}
        ;;{:topic "cheetah-shirts", :partition 0}]
        (let [parts (all-topic-partitions zk-utils)]
          (empty? parts) => false
          (coll? parts) => true
          (sequential? parts) => true
          (s/check [fs/TopicPartition] parts) => nil))
      (fact
        "The partition count for a topic can be increased."
        (redefine-partition-count! zk-utils topic-to-repartition (inc partitions))
        (let [parts (partitions-for zk-utils [topic-to-repartition])
              part-coll (get parts (keyword topic-to-repartition))]
          (count part-coll) => (inc partitions)))
      (fact
        "The partition count for a topic cannot be decreased"
        (redefine-partition-count! zk-utils topic-to-repartition (dec partitions)) => (throws Exception))
      (fact
        "The replica assignment for a partition can be queried."
        ;;ex: [1001 1002 1003]
        (let [replica-assignment (partition-replica-assignment zk-utils topic 0)]
          (coll? replica-assignment) => true
          (sequential? replica-assignment) => true
          (s/check fas/ReplicaAssignments replica-assignment) => nil))
      (fact
        "The in-sync replicas (ISR) for a topic partition can be queried."
        (let [replica-assignment (partition-in-sync-replicas zk-utils topic 0)]
          (coll? replica-assignment) => true
          (sequential? replica-assignment) => true
          (s/check fas/ReplicaAssignments replica-assignment) => nil))
      (fact
        "The partition leader can be queried. There will be a single leader, or none if not elected yet."
        (let [leader (partition-leader zk-utils topic 0)]
          (when leader
            (coll? leader) => false
            (integer? leader) => true)))
      (fact
        "The partition leaders for a topic can be queried. There may or may not be a leader per partition."
        ;;ex: {:salads-can-be-fruit {2 [1001], 1 [0], 0 [1001]}, :chickens-tonight {2 [1001], 1 [1002], 0 [1001]}}
        (let [leaders (partition-leaders-for zk-utils topics)]
          ;;TODO: more tests...
          (when (seq leaders)
            (coll? leaders) => true
            (map? leaders) => true)))
      (fact
        "This looks ridiculous, but the partition leaders metadata for a set of topic partitions can be returned."
        ;;ex:
        ;{{:topic "team-bbq", :partition 1}
        ; {:controller-epoch 1,
        ;  :leader-and-isr
        ;                    {:leader 1001, :isr [1001], :leader-epoch 0, :zk-version 0}},
        ; {:topic "strange-itches", :partition 0}
        ; {:controller-epoch 1,
        ;  :leader-and-isr
        ;                    {:leader 1002, :isr [1002], :leader-epoch 0, :zk-version 0}}}

        (with-open [zk-client (core-test/make-zk-client)]
          (let [topic-partitions [{:topic topic :partition 0} {:topic topic :partition 1} {:topic topic-to-repartition :partition 0}]
                metadata (partition-leaders-metadata zk-utils zk-client topic-partitions)
                topic-part-keys (keys metadata)]
            (nil? metadata) => false
            (empty? metadata) => false
            (map? metadata) => true
            (empty? topic-part-keys) => false
            (s/validate [fs/TopicPartition] topic-part-keys) => topic-part-keys)))
      (fact
        "Partition leader metadata can be queried by topic partition."
        ;;ex {:leader 1001, :isr [1001], :leader-epoch 0, :zk-version 0}
        (let [metadata (partition-leader-metadata zk-utils topic 0)]
          (nil? metadata) => false?
          (map? metadata) => true
          (empty? metadata) => false
          (s/validate fas/LeaderAndIsr metadata) => metadata))
      ;;TODO: more tests...a lot more
      (fact
        "Partitions undergoing reassignment can be queried."
        (let [parts (partitions-undergoing-reassignment zk-utils)]
          (when (seq parts)
            (coll? parts) => true
            (map? parts) => true)))
      (fact
        "Partitions undergoing replica election can be queried."
        (let [parts (partitions-undergoing-replica-election zk-utils)]
          (when (seq parts)
            (coll? parts) => true)))
      (fact
        "The epoch for a partition can be queried."
        (let [epoch (partition-epoch zk-utils topic 0)]
          (integer? epoch) => true)))

    (facts
      "It is possible to query Zookeeper to get more information about Kafka partitions or if you want to use other Zookeeper tools to manage it. "
      (fact
        "It is possible to query the Zookeeper topic partition path."
        ;;ex: /brokers/topics/middle-management-does-nothing/partitions/23
        (let [path (topic-partition-path topic 0)]
          (string? path) => true))
      (fact
        "Returns the path for partitions for a given topic in Zookeeper."
        ;;ex: /brokers/topics/open-office-plans-are-for-schmucks/partitions
        (let [path (topic-partitions-path topic)]
          (string? path) => true))
      (fact
        "It is possible to create the topic partition leader and in-sync replica path in Zookeeper."
        ;;ex: /brokers/topics/horrible-office-partition/partitions/88/state
        (let [path (topic-partition-isr-leader-path topic 0)]
          (string? path) => true))
      (fact
        "It is possible to get the path used to reassign partitions in Zookeeper."
        ;;ex: /admin/reassign_partitions
        (let [path (reassign-partitions-path)]
          (string? path) => true))
      (fact
        "It is possible to get the preferred replica leader election path in Zookeeper."
        ;;ex: /admin/preferred_replica_election
        (let [path (replica-leader-election-path)]
          (string? path) => true))
      (fact
        "It is possible to get the isr change notification path in Zookeeper."
        ;;ex: /isr_change_notification
        (let [path (isr-change-notification-path)]
          (string? path) => true)))))

