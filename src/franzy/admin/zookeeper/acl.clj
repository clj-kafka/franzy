(ns franzy.admin.zookeeper.acl
  (:require [franzy.admin.codec :as codec])
  (:import (kafka.utils ZkUtils)))

(defn default-access-control-list
  [^ZkUtils zk-utils]
  (->>
    (.DefaultAcls zk-utils)
    (codec/decode)))
