(ns franzy.admin.protocols
  "Experimental set of protocols for implementors that want a deftype, reified, record, component, etc.")

(defprotocol TopicReader
  (topic-exists? [this topic])
  (all-topics [this])
  (topics-by-consumer-group [this group])
  (topics-metadata [this topics])
  ;(interal-topics [this])
  )

(defprotocol TopicWriter
  (create-topic!
    [this topic partitions]
    [this topic partitions replication-factor]
    [this topic partitions replication-factor topic-config])
  (delete-topic!
    [this topic]))

(defprotocol TopicPartitionReader
  (partitions-for [this topics])
  (all-topic-partitions [this]))

(defprotocol TopicPartitionWriter
  (redefine-partition-count!
    [this topic partitions]
    [this topic partitions replica-assignment check-broker-available?]))

(defprotocol TopicReplicaMetadataProvider
  (topic-replica-assignment
    [this topic]))

(defprotocol PartitionReplicaMetadataProvider
  (partition-replica-assignment
    [this topic-partition]
    [this topic partition])
  (topic-partition-in-sync-replicas
    [this topic-partition]
    [this topic partition])
  (partition-leader
    [this topic-partition]
    [this topic partition])
  (partition-leader-metadata
    [this topic-partition]
    [this topic partition])
  (partition-leaders-for [this topics])
  (partition-leaders-metadata [this topic-partitions])
  (partitions-undergoing-reassignment [this])
  (partitions-undergoing-replica-election [this])
  (partition-epoch
    [this topic-partition]
    [this topic partition]))

(defprotocol EntityConfigurationReader
  (all-entity-configs
    [this config-type]
    ;;all-non-default-configs
    [this config-type include-defaults?])
  (entity-config [this config-type entity]))


(defprotocol TopicConfigurationReader
  (all-topic-configs [this]))


(defprotocol TopicConfigurationWriter
  (update-topic-config! [this topic config]))


(defprotocol ClientConfigurationReader
  (all-client-configs [this]))


(defprotocol ClientConfigurationWriter
  (update-client-config! [this]))


(defprotocol ConsumerGroupReader
  (consumer-groups [this])
  (consumer-groups-by-topic [this topic])
  (consumer-groups-per-topic [this topic exclude-internal-topics?])
  (consumer-group-active? [this group])
  (consumers-in-group [this group]))

(defprotocol ConsumerGroupWriter
  (delete-consumer-group! [this group])
  (delete-consumer-groups-for-topic!
    [this topic]
    [this topic group]))


(defprotocol BrokerMetadataReader
  (all-brokers [this])
  (broker-ids [this])
  (broker-metadata [this broker-id])
  (broker-endpoints-for-channel [this protocol-type]))

(defprotocol BrokerWriter
  ;;TODO: find if underlying data structure for this
  (register-broker!
    [this broker-info]
    [this id host port endpoints jmx-port]))

(defprotocol BrokerCoordinator
  (delete-broker-topic-path! [broker-id topic]))

(defprotocol ClusterMetadataProvider
  (cluster-info [this]))

(defprotocol KafkaZookeeperMetadataProvider
  (path-exists? [this path])
  (path-children [this path])
  (path-metadata
    [this path]
    [this path allow-null?])
  (persistent-paths
    [this]
    [this secure-paths?]))

(defprotocol KafkaZookeeperWriter
  (delete-path
    [this path]
    [this path recursive?]))
