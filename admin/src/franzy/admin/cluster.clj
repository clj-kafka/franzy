(ns franzy.admin.cluster
  (:require [franzy.admin.codec :as codec]
            [franzy.common.configuration.codec :as config-codec])
  (:import (kafka.utils ZkUtils)
           (kafka.admin AdminUtils)
           (kafka.common BrokerEndPointNotAvailableException)
           (clojure.lang IPersistentMap)))
;
;(defprotocol BrokerWriter
;  ;;TODO: find if underlying data structure for this
;  (register-broker [this id host port endpoints jmx-port]))
;
;(defprotocol ClusterMetadataProvider
;  (cluster-info [this]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Reading Brokers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn all-brokers
  "Returns a list of all the broker info for each broker in the cluster."
  [^ZkUtils zk-utils]
  (->> (.getAllBrokersInCluster zk-utils)
       (codec/decode)))

(defn broker-ids
  "Gets a sorted list of broker IDs."
  [^ZkUtils zk-utils]
  (->>
    (.getSortedBrokerList zk-utils)
    (codec/decode)))

;;TODO: catch exception and return nil?
(defn broker-metadata
  "Returns broker info from Zookeeper, given a broker id.

  If the broker dies before the Zookeeper query finishes, an exception will be thrown."
  [^ZkUtils zk-utils broker-id]
  (->> (int broker-id)
       (.getBrokerInfo zk-utils)
       (codec/decode)))

(defn broker-endpoints-for-channel
  "Returns all broker info in the cluster matching a keyword representing the given protocol type.

  Valid values are:
  :ssl
  :plaintext
  :sasl_plaintext
  :sasl_ssl
  :trace"
  [^ZkUtils zk-utils protocol-type]
  ;Scala throws a BrokerEndPointNotAvailableException, but not sure why we care if we're querying.
  ;There is one exception, which is when a broker dies but we know it should be on a particular channel, in which case returning nil might be less informative.
  ;Might want to not swallow this, but seems like throwing an exception here makes this api less useful
  (try
    (->> protocol-type
         (codec/encode-security-protocol)
         (.getAllBrokerEndPointsForChannel zk-utils)
         (codec/decode))
    (catch BrokerEndPointNotAvailableException e)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Write Brokers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;TODO - this needs an explicit encoding as the nested maps cause issues unless using records

;(defn register-broker!
;  "Registers a broker in the cluster.
;
;  Example:
;
;  ```
;     (let [id 1005
;         host \"127.0.0.1\"
;         port 9092
;         ;;Security Protocol/Endpoint
;         endpoints {:plaintext {:host          \"127.0.0.1\"
;                                :port          9092
;                                :protocol-type :plaintext}}
;         jmx-port -1]
;         (register-broker! zk-utils id host port endpoints jmx-port)"
;  ([^ZkUtils zk-utils {:keys [id host port endpoints jmx-port]
;                       :or {jmx-port -1}}]
;    (register-broker! zk-utils id host port endpoints jmx-port))
;  ([^ZkUtils zk-utils id ^String host ^String port endpoints jmx-port]
;     (.registerBrokerInZk zk-utils id host port (codec/encode endpoints) jmx-port)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Replicas
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn manual-replication-assignment
  "Creates a manual replication assignment map, given a string of replica assignments, a set of broker ids, a start
  partition id, and an optional check if a broker is available."
  ([^String replica-assignments broker-ids start-partition-id]
   (manual-replication-assignment replica-assignments broker-ids start-partition-id true))
  ([^String replica-assignments broker-ids start-partition-id check-broker-available?]
   (AdminUtils/getManualReplicaAssignment replica-assignments (set broker-ids) (int start-partition-id) check-broker-available?)))

(defn assign-replicas-to-brokers
  ([broker-ids partitions replication-factor]
   (assign-replicas-to-brokers broker-ids partitions replication-factor -1 -1))
  ([broker-ids partitions replication-factor fixed-start-index start-partition-id]
   (-> broker-ids
       (codec/encode)
       (AdminUtils/assignReplicasToBrokers (int partitions) (int replication-factor) (int fixed-start-index) (int start-partition-id))
       (codec/decode))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Zookeeper
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn delete-broker-topic-path!
  "Deletes the broker topic path in Zookeeper."
  [^ZkUtils zk-utils broker-id ^String topic]
  (.deletePartition zk-utils (int broker-id) topic))

(defn broker-ids-path []
  "Returns the path in Zookeeper used for broker ids."
  (ZkUtils/BrokerIdsPath))

(defn broker-sequence-id-path []
  "Returns the broker sequence id path in Zookeeper."
  (ZkUtils/BrokerSequenceIdPath))

(defn broker-topics-path []
  "Returns the broker topics path in Zookeeper."
  (ZkUtils/BrokerTopicsPath))
