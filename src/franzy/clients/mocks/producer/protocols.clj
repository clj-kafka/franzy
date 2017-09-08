(ns franzy.clients.mocks.producer.protocols)

(defprotocol KafkaMockProducerLifecycle
  (complete-next! [this]))

(defprotocol KafkaHistorian
  (clear-history! [this])
  (history [this]))
