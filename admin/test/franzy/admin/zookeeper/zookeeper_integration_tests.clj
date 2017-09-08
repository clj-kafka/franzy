(ns franzy.admin.zookeeper.zookeeper-integration-tests
  (:require [midje.sweet :refer :all]
            [franzy.admin.core-test :as core-test]
            [franzy.admin.zookeeper.client :as zk]
            [franzy.admin.serializers :as serializers]
            [franzy.admin.zookeeper.defaults :as zk-defaults])
  (:import (org.I0Itec.zkclient ZkClient)
           (java.io Closeable)))

(facts
  "A Zookeeper client can be constructed to connect to Zookeeper in a variety of ways."
  (fact
    "A Zookeeper client comes with a set of reasonable defaults except for a server."
    (let [default-config (zk-defaults/zk-client-defaults)]
      (:servers default-config) => nil
      (:connection-timeout default-config) =not=> nil
      (:session-timeout default-config) =not=> nil
      (:operation-retry-timeout default-config) =not=> nil))
  (let [zk-config (core-test/make-zk-config)]
    (fact
      "A Zookeeper client must be disposed of properly by calling close, or by using with-open, however it doesn't implement closeable."
      (let [zk-client (zk/make-zk-client zk-config)]
        (.close zk-client)
        (instance? Closeable zk-client) => false)))
  (fact
    "A Zookeeper client can be constructed from a map of zookeeper client options."
    (with-open [zk-client (zk/make-zk-client {:servers                 (:servers (core-test/make-zk-config))
                                              :connection-timeout      3000
                                              :session-timeout         3000
                                              :operation-retry-timeout 3000})]
      (instance? ZkClient zk-client) => true))
  (fact
    "A Zookeeper client only needs at a minimum a list of servers to connect to Zookeeper."
    (with-open [zk-client (zk/make-zk-client {:servers (:servers (core-test/make-zk-config))})]
      (instance? ZkClient zk-client) => true))
  (fact
    "A Zookeeper client only needs at a minimum one of server to connect to Zookeeper, which can be a string instead of a list."
    (let [zk-conf {:servers (:servers (core-test/make-zk-config))}]
      (with-open [zk-client (zk/make-zk-client zk-conf)]
        (instance? ZkClient zk-client) => true)
      (string? (:servers zk-conf)) => true))
  (fact
    "If for some reason, you wrote your own ZK Serializer, you can provide it, otherwise use the one provided."
    (with-open [zk-client (zk/make-zk-client {:servers     (:servers (core-test/make-zk-config))
                                              :serializers (serializers/zk-string-serializer)
                                              })]
      (instance? ZkClient zk-client) => true)))
