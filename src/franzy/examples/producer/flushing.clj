(ns franzy.examples.producer.flushing
  (:require [franzy.clients.producer.client :as producer]
            [franzy.clients.producer.protocols :refer :all]
            [franzy.clients.producer.defaults :as pd]
            [franzy.serialization.serializers :as serializers]
            [franzy.serialization.nippy.serializers :as nippy-serializers]
            [franzy.examples.configuration :as config]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Producer Flush
;;
;; This example demonstrates how a producer can flush its messages, overriding producer configuration.
;; You can use this to force a producer to flush its messages and return success according to your ack configuration.
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn flush-producer []
  (let [pc {:bootstrap.servers config/kafka-brokers

            ;;note: much of this behavior will be determined by your broker itself, read up on flush config keys
            ;;Notice! - we are setting linger.ms. Read more about it in the Kafka manual and how this will impact flush
            :linger.ms         30000}
        key-serializer (serializers/keyword-serializer)
        value-serializer (nippy-serializers/nippy-serializer)
        options (pd/make-default-producer-options)
        topic (:nippy-topic config/topics-to-create)
        partition 2]
    (with-open [p (producer/make-producer pc key-serializer value-serializer options)]
      ;;Here, we don't use callbacks at all. Alternatively, we could pass nil as the callback value, or if we are
      ;;feeling especially explicit, {:send-callback nil}
      (let [send-fut (send-async! p {:topic topic :partition 2 :key :fire-swamp :value [:lightning-sand :rous
                                                                                        :flame-spurt]})]
        ;;Note, we sent, but we may be lingering before we commit the data to Kafka
        ;;linger.ms wants us to wait longer but we are impatient...
        ;;And so, we command it.....
        ;;data is flushed to Kafka, rather than your dreams/pipes
        (flush! p)
        ;;it may be more helpful for you to set a callback for send as well, but keeping this example simple...
        (println "Async send results:" @send-fut)))))
