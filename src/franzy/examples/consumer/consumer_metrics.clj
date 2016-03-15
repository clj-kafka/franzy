(ns franzy.examples.consumer.consumer-metrics
  (:require [franzy.clients.consumer.client :as consumer]
            [franzy.clients.consumer.types :as ct]
            [franzy.common.models.types :as mt]
            [franzy.clients.consumer.protocols :refer :all]
            [franzy.common.metadata.protocols :refer :all]
            [franzy.clients.consumer.defaults :as cd]
            [franzy.serialization.deserializers :as deserializers]
            [franzy.examples.configuration :as config]
            [franzy.serialization.nippy.deserializers :as nippy-deserializers]
            [taoensso.timbre :as timbre]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Consumer Metrics
;;
;; This is a simple example of how to query a consumer's metrics. You'll want to tweak the properties more in reality,
;; so read up on the metric related config settings. This example should dump out some results for you to think about
;; what you can gather and how you might want to use that information.
;;
;; Notes from the tote:
;;
;; 1.  Metrics are returned as map values, with full detail
;; 2.  Querying metrics is not necessarily cheap, so design your metrics accordingly. For one, the payload can be large
;;     depending on your settings. Luckily manipulating the result set is easy since it's just Clojure maps.
;; 3.  In the real-world, you'll want the consumer to poll in a separate thread for a bit before you query metrics.
;;     The workflow is create consumer -> consumer do work -> every once in awhile check metrics
;; 4.  Metrics can be customized on the server.
;; 5.  All values are doubles by default. Kafka sends back -Infinity and +Infinity for nulls sometimes, so we coerce
;;     these values to nil to make things on the Clojure-side easier. If you want to measure infinity, fork this.
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn measure-metrics []
  (let [cc {:bootstrap.servers config/kafka-brokers
            :client.id         "best-of-the-best-3"
            :auto.offset.reset :earliest
            :group.id          "mawages"}
        topic (:nippy-topic config/topics-to-create)
        ;;just for fun, demonstrating using a topic partition as a record
        topic-partitions [(mt/->TopicPartition topic 0)]
        key-deserializer (deserializers/keyword-deserializer)
        value-deserializer (nippy-deserializers/nippy-deserializer)]
    (with-open [c (consumer/make-consumer cc key-deserializer value-deserializer)]
      ;;Now let's say we want to know something about how consuming is going. Perhaps we are too greedy.
      ;;We can get a plethora of metrics, log them, exert back-pressure on the producer if needed, eject, etc.
      ;;All of this, by parsing this wonderful thing below. JMXers, rejoice.
      ;; If there is more of a demand, we can add more transducers, helpers, etc. for metrics
      ;;WARNING - prepare your REPL for a feast. You won't receive any real values unless you've kept the consumer consuming....
      ;;we'll do something simple here and just seek to the beginning manually and then spit out some results hopefully...
      (assign-partitions! c topic-partitions)
      (timbre/info "partitions assigned")
      (seek-to-beginning-offset! c topic-partitions)
      (timbre/info "seeked to beginning")
      (poll! c)
      (timbre/info "finished polling")
      (metrics c))))
