(ns franzy.examples.producer.string-producer
  (:require [franzy.serialization.serializers :as serializers]
            [franzy.examples.configuration :as config]
            [franzy.clients.producer.client :as producer]
            [franzy.clients.producer.defaults :as pd]
            [franzy.clients.producer.protocols :refer :all]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; String Producer
;;
;; This is probably the simplest producer you can create and a good place to get started.
;;
;; Sometimes it's worthwhile to create a very simple producer if you know you're dealing with only a specific data type.
;; In this case, we use the out-of-the-box string serializer as it should be very efficient for simple strings, although
;; it lacks compression without composing it or switching it to another serializer.
;;
;; Some things of note:
;; 1. We can use multiple servers becuase we like clusters, via a Clojure collection of servers such as a vector
;; 2. Our config uses keyword keys, but otherwise matches the Kafka properties
;; 3. Our config uses concrete types for things like retries, because using strings is error-prone
;; 4. We explicitly define our serializers as objects, though we don't have to. This is, however, the preferred method.
;; 5. We close the producer when we're done. In this case, we just wrap it using with-open, but you could close it with (close p)
;; 6. Our producer implements closeable, so there will be no reflection here
;; 7. We can send both synchronously and asynchrously. If we send async, we can choose when to deref our future.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn string-producer-with-explicit-args []
  (let [;;for multiple servers, add a server per vector item
        pc {:bootstrap.servers config/kafka-brokers
            :acks              "all"
            ;;don't retry when we produce, because we have extreme confidence
            :retries           0
            ;;a batch size big enough for our "stuff"
            :batch.size        16384
            ;;don't linger much....
            :linger.ms         10
            ;;we like big buffers....
            :buffer.memory     33554432}
        ;;normally, just inject these direct and be aware some serializers may need to be closed,
        ;; adding to let here to make this clear
        ;;Serializes producer record keys
        key-serializer (serializers/string-serializer)
        ;;Serializes producer record values
        value-serializer (serializers/string-serializer)
        ;;we're being lazy, but we could set some options instead of using the defaults, or not even pass options at all
        options (pd/make-default-producer-options)
        ;;our example topic
        topic (config/topics-to-create :string-topic)
        ;;the first partition
        partition 0]
    ;;first we create a producer with our key serializer, value serializer, and options
    (with-open [p (producer/make-producer pc key-serializer value-serializer options)]
      ;;Here we explicitly send using a topic, partition, key, and value.
      ;;Additionally, for the sake of example, we pass in some options we can use to override send behavior at the call-site
      ;;Other arities exist that omit these options, in which case the default producer options are used
      (let [send-fut (send-async! p topic partition "a key to find a poet" "a gift for rhyme" options)
            ;;Notice! - We can only send string values using this serializer. You probably won't do this normally,
            ;;however if you have a special reason for only sending strings, you will see a performance benefit using this serializer.

            ;;Here we send data to the 2nd partition, partition 1
            ;;This time we send it synchronously, which will block until it completes
            record-metadata (send-sync! p topic 1 "butercup" "reeses" options)]
        (println "Sync send results:" record-metadata)
        (println "Async send results:" @send-fut)))))
