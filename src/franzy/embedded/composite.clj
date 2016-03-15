(ns franzy.embedded.composite
  (:require [com.stuartsierra.component :as component]
            [franzy.embedded.protocols :refer [startup attempt-shutdown]]
            [franzy.embedded.broker :as broker]))

;;composes our deftype embedded broker
(defrecord CompositeEmbeddedBroker [embedded-broker]
  component/Lifecycle
  (start [component]
    (startup embedded-broker))
  (stop [component]
    (attempt-shutdown embedded-broker)))

(defn make-composite-embedded-broker
  "Creates a Kafka Embedded Broker that wraps another embedded broker in a component."
  ^CompositeEmbeddedBroker
  ([] (make-composite-embedded-broker nil nil))
  ([broker-config] (make-composite-embedded-broker broker-config nil))
  ([broker-config thread-name-prefix]
   (-> broker-config
       (broker/make-broker thread-name-prefix)
       (->CompositeEmbeddedBroker))))
