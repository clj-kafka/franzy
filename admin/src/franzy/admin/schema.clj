(ns franzy.admin.schema
  (:require [schema.core :as s]
            [franzy.common.schema :as fs]
            [franzy.common.models.schema :as fms]))

;;TODO: implement any schema not in Franzy-Common used for admin

;;TODO: all these schemas need heavy tweaking and stricter definitions, but this is a start

(def ReplicaAssignments
  [fs/SPosInt])

(def Endpoint
  "Schema for a Kafka endpoint."
  {(s/required-key :host)          s/Str
   (s/required-key :port)          s/Int
   (s/required-key :listener-name)  s/Any ; <-- TODO: Be more specific here.
   (s/required-key :security-protocol) fms/SecurityProtocolEnum})

(def Broker
  {(s/required-key :id)        s/Int
   (s/required-key :endpoints) [Endpoint]})

(def BrokerEndPoint
  "Schema for a Kafka broker endpoint."
  {(s/required-key :id)   s/Int
   (s/required-key :host) s/Str
   (s/required-key :port) s/Int})

(def PartitionMetadata
  "Schema for Kafka partition metadata."
  {(s/required-key :partition-id) fs/SPosInt
   (s/required-key :leader)       (s/maybe BrokerEndPoint)
   (s/required-key :replicas)     (s/maybe [BrokerEndPoint])
   (s/required-key :isr)          (s/maybe [BrokerEndPoint])
   (s/optional-key :error-code)   (s/maybe s/Int)
   (s/optional-key :error)        (s/maybe s/Keyword)})

(def TopicMetadata
  "Schema for Kafka topic metadata."
  {(s/required-key :topic)      fs/NonEmptyString
   (s/required-key :partitions) [PartitionMetadata]
   (s/optional-key :error-code) (s/maybe s/Int)
   (s/optional-key :error)      (s/maybe s/Keyword)})

(def LeaderAndIsr
  "Schema for kafka leader and in-sync replica."
  {(s/required-key :leader)       (s/maybe s/Int)
   (s/required-key :isr)          (s/maybe [s/Int])
   (s/optional-key :leader-epoch) (s/maybe s/Int)
   (s/optional-key :zk-version)   s/Int})

(def ConsumerThreadId
  "Schema for a Kafka consumer thread id."
  {(s/required-key :thread-id) s/Int
   (s/required-key :consumer)  s/Str})

(def ReassignedPartitionsContext
  {(s/required-key :new-replicas) [s/Int]})

(def LeaderIsrAndControllerEpoch
  {(s/required-key :controller-epoch) s/Int
   (s/required-key :leader-and-isr)   LeaderAndIsr})

(def PartitionAndReplica
  {(s/required-key :topic)     fs/NonEmptyString
   (s/required-key :partition) fs/SPosInt
   (s/required-key :replica)   s/Int})


