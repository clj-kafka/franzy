(ns franzy.admin.zookeeper.client
  (:require [franzy.admin.zookeeper.defaults :as defaults]
            [franzy.admin.zookeeper.codec :as codec])
  (:import (kafka.utils ZkUtils)
           (org.I0Itec.zkclient ZkClient ZkConnection IZkConnection)
           (org.I0Itec.zkclient.serialize ZkSerializer)))

(defn make-zk-connection
  ^ZkConnection
  [zk-connection-config]
  (let [{:keys [servers session-timeout]} (merge (defaults/zk-connection-defaults) zk-connection-config)]
    (-> (codec/encode-server-list servers)
        (ZkConnection. session-timeout))))

(defn make-zk-client
  ^ZkClient
  ([^IZkConnection zk-connection zk-client-config]
   (let [{:keys [connection-timeout operation-retry-timeout ^ZkSerializer serializer]} (merge (defaults/zk-client-defaults) zk-client-config)]
     (ZkClient. zk-connection (int connection-timeout) serializer (long operation-retry-timeout))))
  ([zk-client-config]
   (let [{:keys [servers session-timeout connection-timeout operation-retry-timeout serializer]} (merge (defaults/zk-client-defaults) zk-client-config)]
     (-> (codec/encode-server-list servers)
         (ZkClient. session-timeout connection-timeout serializer operation-retry-timeout)))))

(defn make-zk-utils
  ^ZkUtils
  ([zk-client-config is-secure?]
   (let [zk-connection-config (select-keys zk-client-config [:servers :session-timeout])
         zk-connection (make-zk-connection zk-connection-config)
         zk-client (make-zk-client zk-connection zk-client-config)]
     (make-zk-utils zk-client zk-connection is-secure?)))
  ([^ZkClient zk-client ^ZkConnection zk-connection is-secure?]
   (ZkUtils. zk-client zk-connection is-secure?)))
