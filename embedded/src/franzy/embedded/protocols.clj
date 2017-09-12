(ns franzy.embedded.protocols
  (:import (org.apache.kafka.common.protocol SecurityProtocol)
           (org.apache.kafka.common.network ListenerName)))

(defprotocol KafkaBrokerLifecycle
  "Basic Kafka Broker lifecycle protocol."
  (startup [this])
  (shutdown [this])
  (await-shutdown [this])
  (attempt-shutdown [this])
  ;;via Closeable
  ;(close [this])
  )

(defprotocol ZookeeperConnected
  "Kafka protocol for accessing Zookeeper via a broker's Zookeeper connection."
  (zk-utils [this]))

(defprotocol KafkaServerState
  "Stateful Kafka Broker information."
  (set-state [this ^Byte state])
  ;;TODO: switch to clojure type
  (bound-port [this ^ListenerName listener-name]))
