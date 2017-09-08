(ns franzy.clients.mocks.protocols)

(defprotocol KafkaExceptionWriter
  (write-exception! [this e]))
