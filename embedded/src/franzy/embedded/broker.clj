(ns franzy.embedded.broker
  "Embedded Kafka Broker concrete types."
  (:require [taoensso.timbre :as timbre]
            [franzy.embedded.protocols :refer :all]
            [franzy.embedded.server :as server])
  (:import
    (kafka.server KafkaServer KafkaServerStartable)
    (java.io Closeable)))

(deftype EmbeddedKafkaBroker [^KafkaServer server]
  KafkaBrokerLifecycle
  (startup [_]
    "Starts the Kafka Broker."
    (timbre/info "Starting Embedded Kafka Broker...")
    (.startup server))
  (shutdown [_]
    "Shuts down the Kafka Broker.

    > Note: This will signal shutdown, but does not block to gurantee the broker has actually shutdown."
    (timbre/info "Shutting down Embedded Kafka Broker...")
    (.shutdown server))
  (await-shutdown [_]
    "Blocks until shutdown is complete.

    > Note: You must not call this method until you have called shutdown. If you want to shutdown and block immediately,
      `call (close s)`"
    (timbre/info "Awaiting Embedded Kafka Broker shutdown...")
    (.awaitShutdown server))
  (attempt-shutdown [this]
    "Signals shutdown and immediately calls awaitShutdown on the Kafka Broker to block until shutdown,
    but shutdown may or may not succeed."
    (shutdown this)
    (await-shutdown this))
  KafkaServerState
  (set-state [_ state]
    "Allows setting broker state, useful for testing and emitting new states."
    (-> server
        (.brokerState)
        (.newState ^Byte state)))
  (bound-port [_ security-protocol]
    (.boundPort server security-protocol))
  ZookeeperConnected
  (zk-utils [_]
    "Returns the broker's instance of ZkUtils, which can be used for administration.

    > Note: This is here for testing/convenience purposes, for example easy use with Franzy-Admin.
    You must not alter the state of the open Zookeeper connection or it will cause problems with your broker.
    For example, do not close the instance of zk-utils, rather close the broker itself."
    (.zkUtils server))
  Closeable
  (close [this]
    "Proxies to `(attempt-shutdown c)"
    (attempt-shutdown this)))

(deftype StartableEmbeddedKafkaBroker [^KafkaServerStartable server]
  KafkaBrokerLifecycle
  (startup [_]
    (timbre/info "Starting Embedded Kafka Broker...")
    (.startup server))
  (shutdown [_]
    (timbre/info "Shutting down Embedded Kafka Broker...")
    (.shutdown server))
  (await-shutdown [_]
    (timbre/info "Awaiting Embedded Kafka Broker shutdown...")
    (.awaitShutdown server))
  (attempt-shutdown [this]
    (shutdown this)
    (await-shutdown this))
  Closeable
  (close [this]
    (attempt-shutdown this)))

(defn make-broker
  "Creates a low-level Kafka broker, useful for building more customized Zookeeper interactions."
  (^EmbeddedKafkaBroker []
   (make-broker nil nil))
  (^EmbeddedKafkaBroker
  [broker-config] (make-broker broker-config nil))
  (^EmbeddedKafkaBroker
  [broker-config thread-name-prefix]
   (-> broker-config
       (server/make-server thread-name-prefix)
       (EmbeddedKafkaBroker.))))

(defn make-startable-broker
  "Creates a Kafka broker suitable for dev and unit testing."
  (^StartableEmbeddedKafkaBroker []
   (make-startable-broker nil))
  (^StartableEmbeddedKafkaBroker
  [broker-config]
   (-> broker-config
       (server/make-startable-server)
       (StartableEmbeddedKafkaBroker.))))
