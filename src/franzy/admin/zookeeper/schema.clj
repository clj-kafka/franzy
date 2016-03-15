(ns franzy.admin.zookeeper.schema
  "Schemas for working with Zookeeper."
  (:require [schema.core :as s]
            [franzy.common.schema :as fs])
  (:import (org.I0Itec.zkclient.serialize ZkSerializer)))

;;TODO: more specific schema
(def ZkConnectionConfig
  "A schema for a ZkConnection configuration."
  {(s/required-key :servers)         [fs/NonEmptyStringOrList]
   (s/required-key :session.timeout) fs/SPosInt})

(def ZkClientConfig
  "A schema for a ZkClient configuration."
  {(s/required-key :servers)                 [fs/NonEmptyStringOrList]
   (s/required-key :connection-timeout)      fs/SPosInt
   (s/optional-key :session-timeout)         fs/SPosInt
   (s/optional-key :operation-retry-timeout) fs/SPosLong
   (s/optional-key :serializer)              ZkSerializer})

(def ACL
  "A schema for a Zookeeper ACL."
  {
   (s/required-key :id)          s/Any
   (s/required-key :permissions) s/Int})

(def Stat
  "A schema for a Zookeeper stat."
  {(s/optional-key :czx-id)          (s/maybe fs/CoercableLong)
   (s/optional-key :mzx-id)          (s/maybe fs/CoercableLong)
   (s/optional-key :c-time)          (s/maybe fs/CoercableLong)
   (s/optional-key :m-time)          (s/maybe fs/CoercableLong)
   (s/optional-key :version)         (s/maybe fs/CoercableInt)
   (s/optional-key :a-version)       (s/maybe fs/CoercableInt)
   (s/optional-key :c-version)       (s/maybe fs/CoercableInt)
   (s/optional-key :ephemeral-owner) (s/maybe fs/CoercableLong)
   (s/optional-key :data-length)     (s/maybe fs/CoercableInt)
   (s/optional-key :num-children)    (s/maybe fs/CoercableInt)
   (s/optional-key :pzx-id)          (s/maybe fs/CoercableLong)})

;;more......one day
