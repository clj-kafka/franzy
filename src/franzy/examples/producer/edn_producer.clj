(ns franzy.examples.producer.edn-producer
  (:require [franzy.serialization.serializers :as serializers]
            [franzy.examples.configuration :as config]
            [franzy.clients.producer.client :as producer]
            [franzy.clients.producer.protocols :refer :all])
  (:import (franzy.clients.producer.types ProducerRecord)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; EDN Producer
;;
;; This producer uses an EDN serializer. If you don't need compression or want to composite it with something else, this
;; is not the worst place to start. In practice, compression is usually desirable. We demonstrate it here because it's
;; in the core jar, and doesn't require extra dependencies.
;;
;; Things of note:
;;
;; 1. We demonstrate that a lot of our producer functions have overloads for the lazy or simpler requirements
;; 2. We produce using maps and records instead of explicit params. Explicit params give the best performance,
;;    but can be more cumbersome to use. If you're batching a lot of stuff up elsewhere, a record is a good choice.
;; 3. Once again, you can use records or maps. Both can be validated using schemas.
;; 4. We're sending real objects instead of just strings, and no calls to .getBytes or other sillyness.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn produce-edn []
  (let [;;notice vs the string producer example, we're going with much simpler defaults to demonstrate you don't need all that ceremony
        pc {:bootstrap.servers config/kafka-brokers
            ;;Best Practice: Set a client-id for better auditing, easier admin operations, etc.
            :client.id         "social-distorter"}
        ;;now we are using a keyword serializer for keys, so we can use Clojure keywords as keys if we want
        key-serializer (serializers/keyword-serializer)
        ;;now we are using the EDN serializer so we should be able to send full Clojure objects that are EDN-serializable
        value-serializer (serializers/edn-serializer)
        topic (config/topics-to-create :edn-topic)
        partition 0]
    ;;this producer is created without passing options, if you have no need....
    (with-open [p (producer/make-producer pc key-serializer value-serializer)]

      (let [first-value {:quotes  ["Where do these stairs go? They go up." "Egon, your mucus." "Bustin' makes me feel good."]
                         :tools   true
                         :talent  true
                         :members #{"Do" "Ray" "Egon" "Ernie-H"}}
            second-value {:geniuses   :real
                          :kilmered   true
                          :montages   5
                          :best-girls ["Guy in closet" "Stacy Peralta as Shuttle Pilot" "High-powered laser"]}
            ;;we can also send without options and also pass everything as 1 map if we choose - this time we send clj values
            send-fut (send-async! p {:topic topic :partition partition :key :vince-clortho
                                     :value first-value})
            ;;we can also send using a producer record if we don't like the idea of using a map - this is especially
            ;;useful if we need to construct thousands of these producer records in a batch, and maybe manipulate them a bit first
            ;;we could also construct the producer record from a map, and there's a convenience function included that will
            ;verify schema for you if you turn validations on...
            record-metadata (send-sync! p (ProducerRecord. topic partition :chaz-jankel second-value))]
        (println "Sync send results:" record-metadata)
        (println "Async send results:" @send-fut)))))

