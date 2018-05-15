(ns franzy.embedded.component
  "Embedded Kafka Broker Component."
  (:require [taoensso.timbre :as timbre]
            [com.stuartsierra.component :as component]
            [franzy.embedded.server :as server])
  (:import
    (kafka.server KafkaServer KafkaServerStartable)))

;;see extensions.clj for full protocol impl if desired
(defrecord EmbeddedBroker [broker-config thread-name-prefix]
  component/Lifecycle
  (start [component]
    (timbre/info "Starting embedded Kafka component..." broker-config)
    (let [server (server/make-server broker-config thread-name-prefix)]
      (.startup server)
      (assoc component
        :server server)))
  (stop [{:keys [^KafkaServer server] :as component}]
    (timbre/info "Stopping embedded Kafka component..." broker-config)
    (doto server
      (.shutdown)
      (.awaitShutdown))
    (timbre/info "Stopped embedded Kafka component.")
    (assoc component
      :server nil)))

(defn make-embedded-broker
  "Creates a low-level embeddable broker."
  (^EmbeddedBroker []
   (make-embedded-broker nil nil))
  (^EmbeddedBroker
   [broker-config]
   (make-embedded-broker broker-config nil))
  (^EmbeddedBroker
   [broker-config thread-name-prefix]
   (->EmbeddedBroker broker-config thread-name-prefix)))

(defrecord EmbeddedStartableBroker [broker-config]
  component/Lifecycle
  (start [component]
    (timbre/info "Starting embedded startable Kafka component..." broker-config)
    (let [server (server/make-startable-server broker-config)]
      (.startup server)
      (assoc component
        :server server)))
  (stop [{:keys [^KafkaServerStartable server] :as component}]
    (timbre/info "Stopping embedded startable Kafka component..." broker-config)
    (doto server
      (.shutdown)
      (.awaitShutdown))
    (timbre/info "Stopped embedded startable Kafka component.")
    (assoc component
      :server nil)))

(defn make-embedded-startable-broker
  "Creates an embedded startable Kafka broker component, suitable for unit testing and dev."
  (^EmbeddedStartableBroker []
   (make-embedded-startable-broker nil))
  (^EmbeddedStartableBroker
   [broker-config]
   (->EmbeddedStartableBroker broker-config)))
