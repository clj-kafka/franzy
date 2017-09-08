(ns franzy.admin.topic-tests
  (:require [midje.sweet :refer :all]
            [franzy.admin.core-test :as core-test]
            [franzy.admin.topics :refer :all])
  (:import (java.util UUID)
           (clojure.lang IPersistentMap IPersistentVector)))

;;TODO: use midje before/after
(with-open [zk-utils (core-test/make-zk-utils)]
  ;;Note: we only use UUIDs for testing to prevent weird collisions if a teardown failed

  ;;TODO: use before/around from midje
  (facts
    "Topics can be administered."
    (fact
      "All topics visible to the current connection to our Zookeeper cluster can be queried and returned
      as a Clojure collection."
      (let [topics (all-topics zk-utils)]
        ;;ex ["auto-tune-albums" "game-of-thrones-speculation" "boring-analytics"]
        (coll? topics) => true))
    (let [topic (str (UUID/randomUUID))
          topics [topic]
          partition-count 3]
      (fact
        "Topics can be checked if they exist before creating or using them."
        ;;ex: true ....shocking
        (topic-exists? zk-utils topic) => false)
      (fact
        "Topics can be created by providing a topic and partition count."
        (create-topic! zk-utils topic partition-count)
        (topic-exists? zk-utils topic) => true)
      (fact
        "Topic metadata can be queried and returned as Clojure data structures."
        (let [metadata (topics-metadata zk-utils topics)]
          ;;ex:
          ;[{:topic "nerds-have-revenge",
          ;  :partitions-metadata
          ;         [{:partition-id 0,
          ;           :leader {:id 1001, :host "127.0.0.1", :port 9092},
          ;           :replicas [{:id 1001, :host "127.0.0.1", :port 9092}],
          ;           :isr [{:id 1001, :host "127.0.0.1", :port 9092}],
          ;           :error nil,
          ;           :error-code 0}],
          ;  :error nil,
          ;; ;; Will lookup errors for you if there are any and put them in :error
          ;  :error-code 0}]
          (nil? metadata) => false
          (coll? metadata) => true
          ;;TODO: more tests....
          ))
      (fact
        "Replica assignments for multiple topics can be checked and will return a map with topic partitions as keys and
        replica vectors as values."
        ;;ex:
        ;; {{:topic "d2d40104-050a-41c8-955b-423f47207cb7", :partition 2} [1001],
        ;; {:topic "d2d40104-050a-41c8-955b-423f47207cb7", :partition 0} [1001],
        ;; {:topic "d2d40104-050a-41c8-955b-423f47207cb7", :partition 1} [1002]}
        (let [ra (topic-replica-assignments zk-utils topics)]
          (nil? ra) => false
          (instance? IPersistentMap ra) => true
          (count (keys ra)) => partition-count
          (instance? IPersistentVector (get ra {:topic topic :partition 0})) => true
          (-> (get ra {:topic topic :partition 0})
              (first)
              (number?)) => true))
      (fact
        "Topics can be deleted by providing a topic name. Note that the topics are marked for deletion, and may
        return true that they still exist."
        ;;calling topic-exists? again may or may not return true after delete
        (topic-exists? zk-utils topic) => true
        (delete-topic! zk-utils topic))
      ;;TODO: better test, but would need to depend on franzy to fire one up
      (fact
        "Topics can be queried by consumer group and a Clojure collection of topics will be returned."
        (let [group-name "we-are-purple"
              topic-result (topics-by-consumer-group zk-utils group-name)]
          ;;the consumer group must have been active for there to be any results
          ;;ex: ["mass-driver-users" "bruce-willis-look-alikes" "things-that-are-kosh"]
          (coll? topic-result) => true)))
    (fact
      "Topics can also be created with a custom replication factor"
      (let [topic (str (UUID/randomUUID))
            replication-factor 1]
        (create-topic! zk-utils topic 1 replication-factor)
        (topic-exists? zk-utils topic) => true
        (delete-topic! zk-utils topic)))
    (fact
      "Topics can also be created with a custom topic configuration"
      (let [topic (str (UUID/randomUUID))
            replication-factor 1
            topic-config {::cleanup.policy :delete}]
        (create-topic! zk-utils topic 1 replication-factor topic-config)
        (topic-exists? zk-utils topic) => true
        (delete-topic! zk-utils topic))))

  (facts
    "It is possible to query Zookeeper to get more information about Kafka topics or if you want to use other Zookeeper tools to manage it."
    (fact
      "It is possible to query a topic path in Zookeeper."
      (let [path (topic-path "monkey-banana-raffle")]
        ;;ex: /brokers/topics/monkey-banana-raffle
        (string? path) => true))
    (fact
      "It is possible to query all the topics marked for deletion by querying Zookeeper starting at its topics deletion path."
      ;;ex: /admin/delete_topics
      (let [delete-path (delete-topics-path)]
        (string? delete-path) => true))
    (fact
      "It is possible to query the delete path to a specific topic."
      (let [delete-path (delete-topic-path "lets-pretend-we-are-bunny-rabbits")]
        ;;ex: /admin/delete_topics/lets-pretend-we-are-bunny-rabbits
        (string? delete-path) => true))))
