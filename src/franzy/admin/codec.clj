(ns franzy.admin.codec
  "Codec and supporting functionality for managing Kafka property-based configuration."
  (:require [franzy.common.configuration.codec :as config-codec]
            [taoensso.timbre :as timbre])
  (:import (scala Predef Some Tuple2 Tuple3 None Option)
           (scala.collection JavaConverters JavaConversions)
           (java.util Map Set Collection List Properties)
           (clojure.lang Sequential)
           (scala.collection.mutable ArrayBuffer)
           (kafka.common TopicAndPartition ErrorMapping)
           (kafka.consumer ConsumerThreadId)
           (kafka.cluster Broker BrokerEndPoint EndPoint)
           (org.apache.kafka.common.protocol SecurityProtocol)
           (kafka.api TopicMetadata PartitionMetadata LeaderAndIsr TopicMetadataResponse)
           (kafka.controller ReassignedPartitionsContext LeaderIsrAndControllerEpoch PartitionAndReplica)
           (kafka.admin ReassignmentStatus)
           (org.apache.zookeeper.data ACL Stat)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; General Notes
;;
;; 1. This codec handles conversions between Scala, Java, and Clojure
;; 2. Given #1, this could be a lot faster, but we're going for accuracy and comprhensiveness, extensibility, and finally speed
;; 3. There's often bugs in the Scala APIs for Kafka and this codec tries to acommodate the unexpected
;; 4. If anyone wants the explicit conversions exposed as regular functions, happy to do so
;; 5. Enum handling sucks, please fix. There weren't many enums to deal with so we took the quick approach for now
;; 6. Since this codec often dispatches to itself, it makes it easy to handle deep conversions.
;; Originally, postwalk was used, but we found this too slow and cumbersome.
;; We also tried hand-rolled conversions, but it becomes tedious and makes this codec really fragile with updates to Kafka.
;; As such, you're welcome to moan and no one will listen.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(declare decode-xform)

(defprotocol FranzyScalaCodec
  (encode [v]
    "Encodes Clojure values into Kafka Scala admin client values.")
  (decode [v]
    "Decodes Kafka Scala admin client values to Clojure."))

;;TODO: find better way of dealing with encoding enums from keywords
(defn encode-security-protocol
  "Encodes a security protocol given either as a keyword, string,
  or SecurityProtocol value.

  ex: :security-protocol/plaintext, :plaintext, plaintext, SecurityProtocol/PLAINTEXT"
  ^SecurityProtocol [security-protocol]
  (if (instance? SecurityProtocol security-protocol)
    security-protocol
    (->> (name security-protocol)
         (.toUpperCase)
         (SecurityProtocol/valueOf))))

(def config-types
  {:clients "clients"
   :topics  "topics"})

;;TODO: better way/add to codec protocol - crude placeholder
(defn encode-config-type
  [config-type]
  ((keyword config-type) config-types))



;;TODO: use from main lib and find a better way
;;TODO: schema, also maybe can dispatch with schema
(defn map->topic-and-partition
  "Converts a map to a Topic and Partition.

  Note: This is different from the Java APIs TopicPartition. Reusing the Java type would make too much sense, so the opposite has happened.
  Sponge left in the patient."
  ^TopicAndPartition [^Map {:keys [topic partition] :or {partition 0}}]
  (when topic
    (TopicAndPartition. topic (int partition))))

;;TODO: maybe can dispatch with schema
(defn sequential->topic-and-partitions
  [topic-and-partitions]
  (into #{} (map map->topic-and-partition) topic-and-partitions)
  ; #{(TopicAndPartition. "test" 0)}
  )

(def error-code->keywords
  "Maps Kafka Error codes to error keywords so you better know what's going on."
  {(ErrorMapping/BrokerNotAvailableCode)              :broker-unavailable
   (ErrorMapping/ClusterAuthorizationCode)            :cluster-authorization
   (ErrorMapping/ConsumerCoordinatorNotAvailableCode) :consumer-coordinator-unavailable
   (ErrorMapping/GroupAuthorizationCode)              :group-authorization
   (ErrorMapping/InvalidFetchSizeCode)                :invalid-fetch-size
   (ErrorMapping/InvalidMessageCode)                  :invalid-message
   (ErrorMapping/InvalidTopicCode)                    :invalid-topic
   (ErrorMapping/LeaderNotAvailableCode)              :leader-unavailable
   (ErrorMapping/MessageSetSizeTooLargeCode)          :message-set-too-large
   (ErrorMapping/MessageSizeTooLargeCode)             :message-too-large
   (ErrorMapping/NoError)                             nil   ;;If anyone wants this as :no-error, change here, but easier to interrogate error results this way
   (ErrorMapping/NotCoordinatorForConsumerCode)       :not-coordinator-for-consumer
   (ErrorMapping/NotEnoughReplicasAfterAppendCode)    :not-enough-replicas-after-append
   (ErrorMapping/NotEnoughReplicasCode)               :not-enough-replicas
   (ErrorMapping/NotLeaderForPartitionCode)           :not-leader-for-partition
   (ErrorMapping/OffsetMetadataTooLargeCode)          :offset-metadata-too-large
   (ErrorMapping/OffsetOutOfRangeCode)                :offset-out-of-range
   (ErrorMapping/OffsetsLoadInProgressCode)           :offsets-load-in-progress
   (ErrorMapping/ReplicaNotAvailableCode)             :replica-unavailable
   (ErrorMapping/RequestTimedOutCode)                 :request-timed-out
   (ErrorMapping/StaleControllerEpochCode)            :stale-controller-epoch
   (ErrorMapping/StaleLeaderEpochCode)                :stale-leader-epoch
   (ErrorMapping/TopicAuthorizationCode)              :topic-authorization
   (ErrorMapping/UnknownCode)                         :unknown
   (ErrorMapping/UnknownTopicOrPartitionCode)         :unknown-topic-or-partition})

;;TODO: there's a few things here we could arguably pull out in a Zookeeper only codec and dispatch to it, but if things are nested/mixed this would cause a problem
(extend-protocol FranzyScalaCodec
  TopicAndPartition
  (encode [v] v)
  (decode [v]
    {:topic     (.topic v)
     :partition (.partition v)})

  ErrorMapping
  (encode [v] v)
  (decode [v]
    (get error-code->keywords v))

  TopicMetadata
  (encode [v] v)
  (decode [v]
    {:topic               (.topic v)
     :partitions-metadata (-> (.partitionsMetadata v)
                              (decode))
     :error               (some->> (.errorCode v)
                                   (get error-code->keywords))
     :error-code          (.errorCode v)})

  TopicMetadataResponse
  (encode [v] v)
  (decode [v]
    {:brokers         (->> (.brokers v)
                           (decode))
     :correlation-id  (.correlationId v)
     :topics-metadata (->> (.topicsMetadata v)
                           (decode))})

  PartitionMetadata
  (encode [v] v)
  (decode [v]
    {:partition-id (.partitionId v)
     :leader       (->> (.leader v)
                        (decode))
     :replicas     (->> (.replicas v)
                        (decode))
     :isr          (->> (.isr v)
                        (decode))
     :error        (some->> (.errorCode v)
                            (get error-code->keywords))
     :error-code   (.errorCode v)})

  ReassignmentStatus
  (encode [v] v)
  (decode [v] (.status v))

  ACL
  (encode [v] v)
  (decode [v]
    {:id          (.getId v)
     :permissions (.getPerms v)})

  ConsumerThreadId
  (encode [v] v)
  (decode [v]
    {:thread-id (.threadId v)
     :consumer  (.consumer v)})

  ;;TODO: rethink this probably - encoding/decoding is annoying because we're using a keyword so we can't dispatch on type easily, not to mention the usual issues with enum dispatch
  SecurityProtocol
  (encode [v] v)
  (decode [v]
    (-> (.name v)
        (.toLowerCase)
        (keyword)))

  LeaderAndIsr
  (encode [v] v)
  (decode [v]
    {:leader       (.leader v)
     :isr          (->> (.isr v)
                        (decode))
     :leader-epoch (.leaderEpoch v)
     :zk-version   (.zkVersion v)})

  Enum
  (encode [v] v)
  (decode [v]
    (.name v))

  EndPoint
  (encode [v] v)
  (decode [v]
    {:host          (.host v)
     :port          (.port v)
     :protocol-type (-> (.protocolType v)
                        (decode))})

  BrokerEndPoint
  (encode [v] v)
  (decode [v]
    {:id   (.id v)
     :host (.host v)
     :port (.port v)})

  Broker
  (encode [v] v)
  (decode [v]
    {:id        (.id v)
     :endpoints (->> (.endPoints v)
                     (decode))})

  ;;TODO: this is probably an actual object since we also will want the isrChangeListener for partition rebalance callbacks
  ;;but for data-only mapping this is all that is interesting
  ReassignedPartitionsContext
  (encode [v] v)
  (decode [v]
    {:new-replicas (decode v)})

  LeaderIsrAndControllerEpoch
  (encode [v] v)
  (decode [v]
    {:controller-epoch (.controllerEpoch v)
     :leader-and-isr   (-> (.leaderAndIsr v)
                           (decode))})

  PartitionAndReplica
  (encode [v] v)
  (decode [v]
    {:topic     (.topic v)
     :partition (.partition v)
     :replica   (.replica v)})

  Stat
  (encode [v] v)
  (decode [v]
    {:czx-id          (.getCzxid v)
     :mzx-id          (.getMzxid v)
     :c-time          (.getCtime v)
     :m-time          (.getMtime v)
     :version         (.getVersion v)
     :a-version       (.getVersion v)
     :c-version       (.getCversion v)
     :ephemeral-owner (.getEphemeralOwner v)
     :data-length     (.getDataLength v)
     :num-children    (.getNumChildren v)
     :pzx-id          (.getPzxid v)})

  Properties
  (encode [v]
    (config-codec/encode v))
  (decode [v]
    (config-codec/decode v))

  Map
  (encode [v]
    (-> (->> v
             (reduce (fn [m [k val]]
                       (assoc! m (encode k) (encode val)))
                     (transient {}))
             persistent!)
        (JavaConverters/mapAsScalaMapConverter)
        (.asScala)
        (.toMap (Predef/conforms))))
  (decode [v]
    (->> v
         (reduce (fn [m [k val]]
                   (assoc! m (as-> (decode k) dk
                                   (if (string? dk) (keyword dk) dk))
                           (decode val)))
                 (transient {}))
         persistent!))

  Set
  (encode [v]
    (-> (reduce (fn [v1 v2] (conj! v1 (encode v2))) (transient #{}) v)
        (persistent!)
        (JavaConversions/asScalaSet)))
  (decode [v]
    (into #{} decode-xform v))

  List
  (encode [v]
    (-> (reduce (fn [v1 v2] (conj! v1 (encode v2))) (transient []) v)
        (persistent!)
        (JavaConversions/asScalaBuffer)))
  (decode [v]
    (into [] decode-xform v))

  Collection
  (encode [v]
    (-> (reduce (fn [v1 v2] (conj! v1 (encode v2))) (transient []) v)
        (persistent!)
        (JavaConversions/asScalaBuffer)))
  (decode [v]
    (into [] decode-xform v))

  Some
  (encode [v] v)
  (decode [v]
    (some-> v
            (.get)
            (decode)))

  scala.collection.mutable.Map
  (encode [v] v)
  (decode [v]
    ;;bleh, inefficient but not worth the time....
    (->> (JavaConversions/mapAsJavaMap v)
         (decode)))

  scala.collection.immutable.Map
  (encode [v] v)
  (decode [v]
    ;;bleh, inefficient but not worth the time....
    (->> (JavaConversions/mapAsJavaMap v)
         (decode)))

  ArrayBuffer
  (encode [v]
    v)
  (decode [v]
    (->> (JavaConversions/bufferAsJavaList v)
         (decode)))

  scala.collection.Iterable
  (encode [v]
    v)
  (decode [v]
    (->> (JavaConversions/asJavaIterable v)
         (into [] decode-xform)))

  nil
  (encode [v] v)
  (decode [v] v)

  Tuple2
  (encode [v] v)
  (decode [v]
    ;;could use better notation or map, but this just looks like art with rainbows on
    [(decode (._1 v)) (decode (._2 v))])

  Tuple3
  (encode [v] v)
  (decode [v]
    ;;could use better notation or map, but this just looks like art with rainbows on
    [(decode (._1 v)) (decode (._2 v)) (decode (._3 v))])

  Option
  (encode [v] v)
  (decode [v]
    ;;strange bug with getOrElse on some machines NPE, I have no idea why, so this for now....
    (if (.isEmpty v)
      ;;save the extra dispatch
      nil
      (-> (.get v)
          (decode))))

  None
  (encode [v] v)
  (decode [v] nil)

  Object
  (encode [v] v)
  (decode [v] v))

(def decode-xform
  "Transducer, applied on decode of collections that may be overriden using alter-var-root for example."
  (map decode))
