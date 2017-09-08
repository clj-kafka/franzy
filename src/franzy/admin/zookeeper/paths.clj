(ns franzy.admin.zookeeper.paths
  "A collection of Kafka-specific Zookeeper calls, often used internally.

  This namespace presents a collection of tools to help you interrogate the state of Zookeeper paths."
  (:require [franzy.admin.codec :as codec])
  (:import (kafka.utils ZkUtils)))

(defn path-exists?
  "Checks if a given path exists in Zookeeper."
  [^ZkUtils zk-utils ^String path]
  (.pathExists zk-utils path))

(defn persistent-paths
  "Returns persistent paths in Zookeeper, optionally returning secure paths."
  ([^ZkUtils zk-utils] (persistent-paths zk-utils false))
  ([^ZkUtils zk-utils ^Boolean secure-paths?]
   (->>
     (if secure-paths?
       (.securePersistentZkPaths zk-utils)
       (.persistentZkPaths zk-utils))
     (codec/decode))))

(defn path-children
  "Returns Zookeeper path children."
  ([^ZkUtils zk-utils ^String path]
   (path-children zk-utils path true))
  ([^ZkUtils zk-utils ^String path ^Boolean allow-nil-parent?]
   (->> (if allow-nil-parent?
          (.getChildrenParentMayNotExist zk-utils path)
          (.getChildren zk-utils path))
        (codec/decode))))

(defn delete-path
  "Deletes a path in zookeeper, optionally deleting recursively."
  ([^ZkUtils zk-utils ^String path]
   (delete-path zk-utils path false))
  ([^ZkUtils zk-utils ^String path ^Boolean recursive?]
   (if recursive?
     (.deletePathRecursive zk-utils path)
     (.deletePath zk-utils path))))

(defn maybe-delete-path [^String zk-url ^String zk-dir]
  "Returns a maybe delete path in Zookeeper, given a url and a directory."
  (ZkUtils/maybeDeletePath zk-url zk-dir))

(defn path-data
  "Returns data on a Zookeeper path, optionally allowing for nulls."
  ([^ZkUtils zk-utils ^String path] (path-data zk-utils path true))
  ([^ZkUtils zk-utils ^String path ^Boolean allow-nulls?]
   (->
     (if allow-nulls?
       (.readDataMaybeNull zk-utils path)
       (.readData zk-utils path))
     (codec/decode))))

;;TODO - a few more Zookeeper path functions related to ISRs, replica election, etc.
