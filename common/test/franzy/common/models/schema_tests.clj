(ns franzy.common.models.schema-tests
  (:require [midje.sweet :refer :all]
            [schema.core :as s]
            [franzy.common.models.schema :as fs])
  (:import (java.util.concurrent TimeUnit)
           (schema.utils NamedError)))

;;TODO - these tests could be tons better, just a placeholder/basic check for now

;;TODO: fix this schema
(facts
  "A TimeUnit enum value must pass schema validation."
  (fact
    "Time Unit enum values are acceptable values for the TimeUnitEnum schema."
    (s/check fs/TimeUnitEnum TimeUnit/MICROSECONDS) => nil
    (s/check fs/TimeUnitEnum TimeUnit/MILLISECONDS) => nil
    (s/check fs/TimeUnitEnum TimeUnit/MINUTES) => nil)
  (fact
    "Anything not a time unit is not a time unit. Obvious."
    (s/check fs/TimeUnitEnum "penguin cage match") =not=> nil
    (s/check fs/TimeUnitEnum 63) =not=> nil
    (s/check fs/TimeUnitEnum "microseconds") =not=> nil
    (s/check fs/TimeUnitEnum "MICROSECONDS") =not=> nil
    ;;will make this pass in the future
    (s/check fs/TimeUnitEnum :microseconds) =not=> nil))

;;TODO: fix this schema
(facts
  "A SecurityProtocolEnum value must pass schema validation."
  ;;TODO: these really need to be keywords probably....
  (fact
    "Plaintext values of security protocol enum are valid."
    (s/check fs/SecurityProtocolEnum "PLAINTEXT") => nil
    (s/check fs/SecurityProtocolEnum "SSL") => nil
    (s/check fs/SecurityProtocolEnum "SASL_SSL") => nil
    (s/check fs/SecurityProtocolEnum "SASL_PLAINTEXT") => nil
    (s/check fs/SecurityProtocolEnum "TRACE")
    (s/check fs/SecurityProtocolEnum :plaintext) => nil)
  (fact
    "Anything that is not an acceptable security protocol value is illegal."
    (s/check fs/SecurityProtocolEnum "plaintext") =not=> nil
    (s/check fs/SecurityProtocolEnum "repeating the same song is not wrong") =not=> nil
    (s/check fs/SecurityProtocolEnum 99999999) =not=> nil))

(facts
  "Topic partitions must pass schema validation."
  (let [valid-topic-partition {:topic "80s-super-action" :partition 87}]
    (fact
      "Topic Partition with a topic and partition is valid."
      (s/validate fs/TopicPartition valid-topic-partition) => valid-topic-partition)
    (fact
      "Topic Partition without a topic is invalid."
      (let [invalid-topic-partition {:partition 1}]
        ;;another way to check for schema errors, still trying to think of best way for accurate testing
        (class (s/check (s/named fs/TopicPartition "name") invalid-topic-partition)) => NamedError))
    (fact
      "Topic Partition with an overflowing partition value is invalid."
      (s/check fs/TopicPartition {:topic "cheese" :partition (+ Integer/MAX_VALUE 1)}) =not=> nil)
    (fact
      "Topic Partition with an underflowing partition value is invalid."
      (s/check fs/TopicPartition {:topic "cheese" :partition (- Integer/MIN_VALUE 1)}) =not=> nil)
    (fact
      "Topic Partition with a non-string topic name is invalid."
      (s/check fs/TopicPartition {:topic 123 :partition 99}) =not=> nil)
    (fact
      "Invalid Topic Partition values should not pass validation"
      ;;test with extreme paranoia, as if this fails, so do many things. Kafka is not to be trusted.
      (s/check fs/TopicPartition (assoc valid-topic-partition :topic [])) =not=> nil
      (s/check fs/TopicPartition (assoc valid-topic-partition :topic {})) =not=> nil
      (s/check fs/TopicPartition (assoc valid-topic-partition :topic '())) =not=> nil
      (s/check fs/TopicPartition (assoc valid-topic-partition :topic #{})) =not=> nil
      (s/check fs/TopicPartition (assoc valid-topic-partition :topic "")) =not=> nil
      (s/check fs/TopicPartition (assoc valid-topic-partition :topic [])) =not=> nil
      (s/check fs/TopicPartition (assoc valid-topic-partition :topic "")) =not=> nil
      (s/check fs/TopicPartition (assoc valid-topic-partition :topic nil)) =not=> nil
      (s/check fs/TopicPartition (assoc valid-topic-partition :partition Long/MAX_VALUE)) =not=> nil
      (s/check fs/TopicPartition (assoc valid-topic-partition :partition Double/NaN)) =not=> nil
      (s/check fs/TopicPartition (assoc valid-topic-partition :partition Double/POSITIVE_INFINITY)) =not=> nil
      (s/check fs/TopicPartition (assoc valid-topic-partition :partition -1)) =not=> nil
      (s/check fs/TopicPartition (assoc valid-topic-partition :dark-crystal "skeksis")) =not=> nil)))

(facts
  "Bootstrap servers must pass schema validation."
  (fact
    "Bootstrap server has a host and a port."
    (let [server-address {:host "127.0.0.1" :port 8080}]
      (s/validate fs/BootstrapServerAddress server-address) => server-address))
  (fact
    "Bootstrap server cannot have a negative port."
    (let [server-address {:host "127.0.0.1" :port -8080}]
      (s/check fs/BootstrapServerAddress server-address) =not=> nil))
  (fact
    "Bootstrap server cannot be missing a host."
    (let [server-address {:port 8080}]
      (s/check fs/BootstrapServerAddress server-address) =not=> nil)))

(facts
  "KafkaAck must pass schem validation."
  (fact
    "A Kafka ack is one of -1, 0, and 1"
    (s/check fs/KafkaAck -1) => nil
    (s/check fs/KafkaAck 0) => nil
    (s/check fs/KafkaAck 1) => nil)
  (fact
    "Anything not -1, 0, or 1 is invalid."
    (s/check fs/KafkaAck -1.0) =not=> nil
    (s/check fs/KafkaAck 0.0) =not=> nil
    (s/check fs/KafkaAck 1.0) =not=> nil
    (s/check fs/KafkaAck "juice bar") =not=> nil
    (s/check fs/KafkaAck :1) =not=> nil))

(facts
  "Nodes must pass schema validation."
  (let [valid-node {:host "127.0.0.1" :port 2181 :id 55}]
    (fact
      "Valid nodes are valid."
      (s/check fs/Node valid-node) => nil
      (s/check fs/Node (assoc valid-node :port 0)) => nil)
    (fact
      "Invalid nodes fail schema validation."
      (s/check fs/Node (assoc valid-node :host 123)) =not=> nil
      (s/check fs/Node (assoc valid-node :host nil)) =not=> nil
      (s/check fs/Node (assoc valid-node :port "turtles")) =not=> nil
      (s/check fs/Node (assoc valid-node :port nil)) =not=> nil
      (s/check fs/Node (assoc valid-node :port -123)) =not=> nil
      (s/check fs/Node (assoc valid-node :id nil)) =not=> nil
      (s/check fs/Node {}) =not=> nil
      (s/check fs/Node (assoc valid-node :careless-key 123.0)) =not=> nil
      (s/check fs/Node (dissoc valid-node :host)) =not=> nil
      (s/check fs/Node (dissoc valid-node :port)) =not=> nil
      (s/check fs/Node (dissoc valid-node :id)) =not=> nil)))

(facts
  "PartitionInfo must pass schema validation."
  (let [valid-partition-info {:topic            "vocoders"
                              :partition        23
                              :leader           {:host "127.0.0.1" :id 3432 :port 80}
                              :replicas         [{:host "127.0.0.1" :id 2322 :port 81}]
                              :in-sync-replicas [{:host "127.0.0.1" :id 3432 :port 80}]}]
    (fact
      "Valid PartitionInfo must be valid."
      (s/check fs/PartitionInfo valid-partition-info) => nil
      (s/check fs/PartitionInfo (assoc valid-partition-info :replicas [])) => nil
      (s/check fs/PartitionInfo (assoc valid-partition-info :replicas nil)) => nil
      (s/check fs/PartitionInfo (assoc valid-partition-info :in-sync-replicas [])) => nil
      (s/check fs/PartitionInfo (assoc valid-partition-info :in-sync-replicas nil)) => nil
      (s/check fs/PartitionInfo (assoc valid-partition-info :leader nil)) => nil)
    (fact
      "Invalid PartitionInfo is strictly out of bounds."
      (s/check fs/PartitionInfo (assoc valid-partition-info :topic nil)) =not=> nil
      (s/check fs/PartitionInfo (assoc valid-partition-info :partition nil)) =not=> nil
      (s/check fs/PartitionInfo (assoc valid-partition-info :renegade-key nil)) =not=> nil
      (s/check fs/PartitionInfo (assoc valid-partition-info :partition "lettuce is filler")) =not=> nil
      (s/check fs/PartitionInfo (assoc valid-partition-info :leader [])) =not=> nil
      (s/check fs/PartitionInfo (assoc valid-partition-info :leader "of men")) =not=> nil
      (s/check fs/PartitionInfo (assoc valid-partition-info :replicas {})) =not=> nil
      (s/check fs/PartitionInfo (assoc valid-partition-info :replicas "spoon")) =not=> nil
      (s/check fs/PartitionInfo (assoc valid-partition-info :in-sync-replicas {})) =not=> nil
      (s/check fs/PartitionInfo (assoc valid-partition-info :in-sync-replicas "spooning lead to..")) =not=> nil
      (s/check fs/PartitionInfo "sprinkles") =not=> nil
      (s/check fs/PartitionInfo {}) =not=> nil
      (s/check fs/PartitionInfo (dissoc valid-partition-info :topic)) =not=> nil
      (s/check fs/PartitionInfo (dissoc valid-partition-info :partition nil)) =not=> nil
      (s/check fs/PartitionInfo (dissoc valid-partition-info :leader)) =not=> nil
      (s/check fs/PartitionInfo (dissoc valid-partition-info :replicas)) =not=> nil
      (s/check fs/PartitionInfo (dissoc valid-partition-info :in-sync-replicas)) =not=> nil)))

(facts
  "KafkaMetric must pass schema validation."
  (let [valid-kafka-metric {:metric-name "power of love"
                            :value       3.47}]
    (fact
      "Valid Kafka metrics must pass schema validation."
      (s/check fs/KafkaMetric valid-kafka-metric) => nil
      (s/check fs/KafkaMetric (assoc valid-kafka-metric :value nil)) => nil)
    (fact
      "Invalid KAfka metrics must fail schema validation."
      (s/check fs/KafkaMetric {}) =not=> nil
      (s/check fs/KafkaMetric (dissoc valid-kafka-metric :metric-name)) =not=> nil

      (s/check fs/KafkaMetric (assoc valid-kafka-metric :metric-name nil)) =not=> nil
      (s/check fs/KafkaMetric (dissoc valid-kafka-metric :metric-name 42)) =not=> nil
      (s/check fs/KafkaMetric (dissoc valid-kafka-metric :value)) =not=> nil
      (s/check fs/KafkaMetric (assoc valid-kafka-metric :value "squirrel battles")) =not=> nil)))

(facts
  "KafkaMetricName must pass schema validation."
  (let [valid-metric-name {:group       "prisoners on early release"
                           :name        "All shows with 'is new' are overrated"
                           :description "hopeful to measure-up"
                           :tags        {:hopelessly-clogged "bloods"
                                         :bbq                "crips"
                                         :annoyances         "newscasters mentioning tags on live TV"}}]
    (fact
      "Valid Kafka metric names pass schema validation."
      (s/check fs/KafkaMetricName valid-metric-name) => nil
      (s/check fs/KafkaMetricName (dissoc valid-metric-name :description)) => nil
      (s/check fs/KafkaMetricName (assoc valid-metric-name :description nil)) => nil
      (s/check fs/KafkaMetricName (dissoc valid-metric-name :tags)) => nil
      (s/check fs/KafkaMetricName (assoc valid-metric-name :tags nil)) => nil)
    (fact
      "Invalid Kafka metric names must fail schema validation."
      (s/check fs/KafkaMetricName {}) =not=> nil
      (s/check fs/KafkaMetricName (assoc valid-metric-name :group nil)) =not=> nil
      (s/check fs/KafkaMetricName (assoc valid-metric-name :group 1234)) =not=> nil
      (s/check fs/KafkaMetricName (assoc valid-metric-name :name nil)) =not=> nil
      (s/check fs/KafkaMetricName (assoc valid-metric-name :name 1234)) =not=> nil
      (s/check fs/KafkaMetricName (assoc valid-metric-name :tags [])) =not=> nil
      (s/check fs/KafkaMetricName (assoc valid-metric-name :tags {"lettuce" "say no"})) =not=> nil
      (s/check fs/KafkaMetricName (assoc valid-metric-name :description 1234)) =not=> nil
      (s/check fs/KafkaMetricName (dissoc valid-metric-name :group)) =not=> nil
      (s/check fs/KafkaMetricName (dissoc valid-metric-name :name)) =not=> nil
      (s/check fs/KafkaMetricName (assoc valid-metric-name :fat-finger-key "launch codes")) =not=> nil)))
