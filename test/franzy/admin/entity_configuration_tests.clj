(ns franzy.admin.entity-configuration-tests
  (:require [midje.sweet :refer :all]
            [schema.core :as s]
            [franzy.admin.core-test :as core-test]
            [franzy.admin.schema :as fas]
            [franzy.admin.configuration :refer :all]
            [franzy.admin.topics :as topics]
            [franzy.common.models.schema :as fs])
  (:import (java.util UUID)))

;;TODO: midje before/after teardown
(with-open [zk-utils (core-test/make-zk-utils)]
  (let [topic (str (UUID/randomUUID))
        topic-to-config (str (UUID/randomUUID))
        partitions 3
        topics [topic topic-to-config]]
    (topics/create-topic! zk-utils topic partitions)
    (topics/create-topic! zk-utils topic-to-config partitions)

    (fact
      "All entity configurations by entity type can be queried."
      ;;ex: `{:all-my-friends {:cleanup.policy "compact"} :people-who-died {}}`"
      (let [configs (all-entity-configs zk-utils :topics)]
        (nil? configs) => false
        (coll? configs) => true
        (map? configs) => true
        (keys configs) => (has every? keyword?)))

    (fact
      "All topic configurations can be queried."
      ;;ex:  {:toasts-of-the-time {:cleanup.policy "delete"} :coworker-lunches-to-steal {}}
      (let [configs (all-topic-configs zk-utils)]
        (nil? configs) => false
        (coll? configs) => true
        (map? configs) => true
        (keys configs) => (has every? keyword?)))

    (fact
      "All configurations in the cluster can be queried."
      ;;ex: {:detention-level {:cleanup.policy "compact"} :love-and-rockets {}}
      (let [configs (all-configs zk-utils)]
        (nil? configs) => false
        (coll? configs) => true?
        (map? configs) => true
        (keys configs) => (has every? keyword?)))

    (fact
      "Returned configuration maps can be transduced to filter default configurations and used with other API calls."
      ;ex: {:adventures-of-beans {:segment.bytes "104857600"} :blonde-adonis {:cleanup.policy "delete"}}
      ;;No empty maps....
      (let [configs (all-configs zk-utils)]
        (nil? configs) => false?
        (coll? configs) => true?
        (map? configs) => true
        (into {} default-config-xf configs) =not=> (contains empty?)))

    (fact
      "All configurations in the cluster can be queried with a flag to include/exclude default entity configurations."
      ;; No configurations with empty maps will be returned, meaning only non-default are included (false - include-default?)
      ;;ex:  {:__consumer_offsets {:segment.bytes "104857600", :cleanup.policy "compact", :compression.type "uncompressed"}}
      (let [configs (all-entity-configs zk-utils :topics false)]
        (nil? configs) => false
        (coll? configs) => true?
        (map? configs) => true
        (keys configs) => (has every? keyword?)
        (:__consumer_offsets configs) =not=> {}
        configs =not=> (contains empty?)))

    (fact
      "The configuration for a specific entity, such as a topic can be queried."
      ;;ex: {:segment.bytes "104857600", :cleanup.policy "compact", :compression.type "uncompressed"}
      (let [config (entity-config zk-utils :topics topic)]
        (nil? config) => false
        (coll? config) => true
        (map? config) => true
        ;;the default...
        config => {}))

    (fact
      "All names of entities with configurations can be retrieved."
      ;;ex: ["love-in-the-time-of-mobile-homes" "the-hook"]
      (let [configs (all-entities-with-configs zk-utils :topics)]
        (nil? configs) => false
        (coll? configs) => true
        (sequential? configs) => true
        configs => (has every? string?) => true))

    (fact
      "All client configurations can be retrieved."
      (let [configs (all-client-configs zk-utils)]
        ;;if no clients are connected, you might not get any results :(
        (nil? configs) => false
        (coll? configs) => true
        (map? configs) => true))

    (fact
      "Configuration values can be updated."
      ;;ex: `{:all-my-friends {:cleanup.policy "compact"} :people-who-died {}}`"
      (update-topic-config! zk-utils topic-to-config {:cleanup.policy :compact})
      (let [config (entity-config zk-utils :topics topic-to-config)]
        (nil? config) => false
        (coll? config) => true
        (map? config) => true
        (keys config) => (has every? keyword?)
        ;;converting back to keywords isn't implemented yet...
        config => {:cleanup.policy "compact"}))

    (fact
      "Znode data for configuration nodes in Zookeeper can be retrieved."
      ;;ex {:version 1, :entity_type "topics", :entity_name "overflowing-cereal-bowls"}
      (let [node-data (config-change-znode-data :topics topic-to-config)]
        (nil? node-data) => false?
        (coll? node-data) => true?
        (map? node-data) => true
        ;;TODO: schema + conversions of key names to more clojureish if desired...
        (keys node-data) => (just [:version :entity_type :entity_name])))
    (fact
      "The root path used to store entity configurations in Zookeeper for a given config type can be retrieved."
      ;;ex: /config/topics
      (let [path (entity-config-root-path :topics)]
        (string? path) => true))

    (fact
      "The entity config path in Zookeeper, given a config type and the target entity can be retrieved."
      ;;ex: /config/clients/client-of-clients
      (let [path (entity-config-path :clients topic-to-config)]
        (string? path) => true))

    (fact
      "The entity config changes path in Zookeeper."
      ;ex: /config/changes
      (let [path (entity-config-changes-path)]
        (string? path) => true))))
