(ns franzy.embedded.extensions
  (:require [franzy.embedded.protocols :refer :all])
  (:import (kafka.server KafkaServer)
           (franzy.embedded.component EmbeddedBroker)))

;;refer if you want the full set of functions

(extend-type EmbeddedBroker
  KafkaServerState
  (set-state [{:keys [^KafkaServer server]} ^Byte state]
    (some-> server
            (.brokerState)
            (.newState state)))
  (bound-port [{:keys [^KafkaServer server]} security-protocol]
    (some-> server
            (.boundPort security-protocol)))
  ZookeeperConnected
  (zk-utils [{:keys [^KafkaServer server]}]
    (some-> server
            (.zkUtils)))
  ;;NOTE: given the Lifecycle conflicts between the broker and component, I do not suggest implementing the
  ;;broker's Lifecycle protocol as shown below:
  ;;At best, it should do something similar to the component extensions out there by also modifying system and tying into the component lifecylc better.
  ;;In a word, you have been warned, but leaving this here if anyone wants to take this up

  ;KafkaBrokerLifecycle
  ;(startup [{:keys [^KafkaServer server]}]
  ;  (timbre/info "Kafka server is starting...")
  ;  (some-> server
  ;          (.startup)))
  ;(shutdown [{:keys [^KafkaServer server]}]
  ;  (timbre/info "Kafka server is shutting down...")
  ;  (some-> server
  ;          (.shutdown)))
  ;(await-shutdown [{:keys [^KafkaServer server]}]
  ;  (timbre/info "Awaiting server shutdown...")
  ;  (some-> {:keys [^KafkaServer server]}
  ;          server
  ;          (.awaitShutdown)))
  ;(attempt-shutdown [component]
  ;  (shutdown component)
  ;  (await-shutdown component))
  )
