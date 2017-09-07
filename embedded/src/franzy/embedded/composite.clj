(ns franzy.embedded.composite
  (:require [com.stuartsierra.component :as component]
            [franzy.embedded.protocols :refer [startup attempt-shutdown]]
            [franzy.embedded.broker :as broker]
            [taoensso.timbre :as timbre]))

;;composes our deftype embedded broker
(defrecord CompositeEmbeddedBroker [embedded-broker]
  component/Lifecycle
  (start [component]
    (timbre/info "Starting Kafka composite broker...")
    (startup embedded-broker)
    (timbre/info "Composite Kafka broker started.")
    component)
  (stop [component]
    (timbre/info "Stopping composite Kafka broker...")
    (attempt-shutdown embedded-broker)
    (timbre/info "Kafka composite broker stopped.")
    component))

(defn make-composite-embedded-broker
  "Creates a Kafka Embedded Broker that wraps another embedded broker in a component.

  Any broker that satisfied the KafkaBrokerLifecycle protocol may be used."
  (^CompositeEmbeddedBroker [] (make-composite-embedded-broker nil nil))
  (^CompositeEmbeddedBroker
  [broker-config] (make-composite-embedded-broker broker-config nil))
  (^CompositeEmbeddedBroker
  [broker-config thread-name-prefix]
   (-> broker-config
       (broker/make-broker thread-name-prefix)
       (->CompositeEmbeddedBroker))))
