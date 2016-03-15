(ns franzy.embedded.component
  "Embedded Kafka Broker Component."
  (:require [taoensso.timbre :as timbre]
            [com.stuartsierra.component :as component]
            [franzy.embedded.server :as server])
  (:import
    (kafka.server KafkaServer)))

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
    (timbre/info "Stopping embedded Kafka component...")
    (doto server
      (.shutdown)
      (.awaitShutdown))
    (assoc component
      :server nil)))

(defn ^EmbeddedBroker make-embedded-broker
  ([] (make-embedded-broker nil nil))
  ([broker-config]
   (make-embedded-broker broker-config nil))
  ([broker-config thread-name-prefix]
   (->EmbeddedBroker broker-config thread-name-prefix)))
