(ns franzy.admin.configuration
  (:require [franzy.admin.codec :as codec]
            [franzy.common.configuration.codec :as config-codec])
  (:import (kafka.admin AdminUtils)
           (kafka.utils ZkUtils)
           (kafka.server ConfigType)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Configuration
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def default-config-xf
  "Transducer to remove defaukt configurations."
  (remove (comp empty? second)))

(defn all-entity-configs
  "Returns all topic or client configurations from Zookeeper, as specified by the config-type.

  See (all-topics-configuration) for an example."
  ([^ZkUtils zk-utils config-type]
   (->> config-type
        (codec/encode-config-type)
        (AdminUtils/fetchAllEntityConfigs zk-utils)
        (codec/decode)))
  ([^ZkUtils zk-utils config-type ^Boolean include-defaults?]
   (if include-defaults?
     ;;avoid the extra transduction if we don't want it, otherwise we could do identity as the xf
     (all-entity-configs zk-utils config-type)
     (into {} default-config-xf (all-entity-configs zk-utils config-type)))))

(defn all-entities-with-configs
  "Returns all entity names that have a configuration.

  Example:

  `(all-entities-with-configs zk-utils :topics)`"
  [^ZkUtils zk-utils config-type]
  (->> (codec/encode-config-type config-type)
       (.getAllEntitiesWithConfig zk-utils)
       (codec/decode)))

(defn all-configs
  "Returns all configs in the cluster.

  > Note: This is a convenience method, that makes multiple queries."
  ([^ZkUtils zk-utils]
   (all-configs zk-utils false))
  ([^ZkUtils zk-utils include-defaults?]
   (let [topic-configs (all-entity-configs zk-utils :topics include-defaults?)
         client-configs (all-entity-configs zk-utils :clients include-defaults?)]
     (merge client-configs topic-configs))))

(defn entity-config
  "Returns the entity (topic or client) config (if any) from Zookeeper, given a config type and an entity."
  [^ZkUtils zk-utils config-type entity]
  (->> (AdminUtils/fetchEntityConfig zk-utils (codec/encode-config-type config-type) entity)
       (codec/decode)))

;;TODO: need to check if any internal configs get returned here, in which case we will call the all-entity-configs with the include param false
(defn all-topic-configs
  "Returns all topic configurations from Zookeeper.

  Returns a maps with topic names as keys and configuration maps as values.
  If you did not specify a configuration on topic creation or otherwise modify it later,
  an empty map is returned for the topic configuration.
  Note this does not mean there is no configuration, but rather the topic will be using Kafka defaults.

   Example:

   `{:all-my-friends {:cleanup.policy \"compact\"} :people-who-died {}}`"
  [^ZkUtils zk-utils]
  (->> (AdminUtils/fetchAllTopicConfigs zk-utils)
       (codec/decode)))

;;TODO: need to check if any internal configs get returned here, in which case we will call the all-entity-configs with the include param false
(defn all-client-configs
  "Returns all client configurations from Zookeeper"
  [^ZkUtils zk-utils]
  (all-entity-configs zk-utils :clients))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Writing Configurations
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn update-topic-config!
  "Updates an existing topic's configuration, given a topic configuration map.

  Example:

  `(update-topic-config! \"sandwiches-of-unusual-size\" {:cleanup-policy compact})`

  > Note: Config changes needs to replicate to other brokers, therefore changes may not be instantaneous.
   Do not implement behaviors that depend on this change being instant, minimally check if the target broker has
   received the change.
   A change notification will be created internally to propogate the configuration change to other brokers."
  [^ZkUtils zk-utils ^String topic config]
  (->> config
       (config-codec/encode)
       (AdminUtils/changeTopicConfig zk-utils topic)))

(defn update-client-config!
  [^ZkUtils zk-utils ^String client-id config]
  "Updates an existing client's configuration given a client configuration map.

    > Note: Config changes needs to replicate to other brokers, therefore changes may not be instantaneous.
    Do not implement behaviors that depend on this change being instant,
    minimally check if the target broker has received the change.
    A change notification will be created internally to propogate the configuration change to other brokers."
  (->> (config-codec/encode config)
       (AdminUtils/changeClientIdConfig zk-utils client-id)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Zookeeper
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn config-change-znode-data
  "Gets znode data for configuration nodes in Zookeeper."
  [config-type ^String entity-name]
  (-> (codec/encode-config-type config-type)
      (AdminUtils/getConfigChangeZnodeData entity-name)
      (codec/decode)))

(defn entity-config-root-path [config-type]
  "Returns the root path used to store entity configurations in Zookeeper for a given config type."
  (-> (codec/encode-config-type config-type)
      (ZkUtils/getEntityConfigRootPath)))

(defn entity-config-path [config-type ^String entity]
  "Returns the entity config path in Zookeeper, given a config type and the target entity."
  (-> (codec/encode-config-type config-type)
      (ZkUtils/getEntityConfigPath entity)))

(defn entity-config-changes-path []
  "Returns the entity config changes path in Zookeeper."
  (ZkUtils/EntityConfigChangesPath))
