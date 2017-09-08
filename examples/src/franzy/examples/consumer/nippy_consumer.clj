(ns franzy.examples.consumer.nippy-consumer
  (:require [franzy.clients.consumer.client :as consumer]
            [franzy.clients.consumer.protocols :refer :all]
            [franzy.clients.consumer.defaults :as cd]
            [franzy.serialization.nippy.deserializers :as nippy-deserializers]
            [franzy.serialization.deserializers :as deserializers]
            [franzy.examples.configuration :as config]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Nippy Consumer
;;
;; This example demonstrates how you can mess around with consumer records and uses a nippy deserializer.
;; NOTE: Please ensure you've populated some data in your nippy topic, otherwise you'll see nothing!
;; This should be obvious, but a poor soul already asked me this.
;;
;; Thought for food:
;;
;; 1.  Here we demonstrate a manual consumer. Only the beginning steps would change with a subscription-based consumer.
;; 2.  We poll to get results, obviously
;; 3.  Depending on our settings, we might get huge amounts of results with poll, be careful if you've populated a lot of
;;     data. You may select to tweak settings or this script in that case.
;; 4.  Consumer records is a real object, not just a map.
;; 5.  We can make consumer records behave like a clojure collection, or we can call some functions on its protocol
;; 6.  The default results are a lazy sequence, however as noted in the source, some of the functions are eager because
;;     they are also eager in Kafka.
;; 7.  We can query the results both by topic and topic partition
;; 8.  I prefer to just take the consumer records and transduce them myself. We demonstrate some really naive but easy
;;     to understand examples. In the real-world, you'd right cleaner, better performing transducers.
;; 9.  You may put the consumer records directly on to a core.async channel using one of the built-in methods since we
;;     hand you back a sequence and you can transduce it. No need for special async libs, though one might be coming for
;;     other reasons.
;; 10. Your ouput may vary depending on what's already in your topic.
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn consume-nippy []
  (let [cc {:bootstrap.servers config/kafka-brokers
            :group.id          "hungry-eels"
            :auto.offset.reset :earliest}
        key-deserializer (deserializers/keyword-deserializer)
        value-deserializer (nippy-deserializers/nippy-deserializer)
        options (cd/make-default-consumer-options)
        topic (:nippy-topic config/topics-to-create)
        ;;notice, we are setting the topics and partitions to what we produced to earlier...
        topic-partitions [{:topic topic :partition 0}]]
    (with-open [c (consumer/make-consumer cc key-deserializer value-deserializer options)]
      (assign-partitions! c topic-partitions)
      (seek-to-beginning-offset! c topic-partitions)
      (let [;;notice, here we are binding the consumer records so we can work with them in a more sane manner....
            cr (poll! c)
            ;;a transducer that looks for consumer records by a certain key
            filter-xf (filter (fn [cr] (= (:key cr) :six-fingered-man)))
            ;;a transducer that creates sequences only with consumer record value, written the long-way for clarity
            value-xf (map (fn [cr] (:value cr)))
            ;;a tranducer combining our misintentions
            inconceivable-transduction (comp filter-xf value-xf)]

        ;;count the records
        (println "Record count:" (record-count cr))
        ;;get only data by topic
        (println "Records by topic:" (records-by-topic cr topic))
        ;;this won't return anything, but demonstrates that the filter does indeed do something
        (println "Records from a topic that doesn't exist:" (records-by-topic cr "no-one-of-consequence"))
        ;;we can also query by topic partition
        (println "Records by topic partition:" (records-by-topic-partition cr topic 0))
        ;;The source data is a list, so no worries here....
        (println "Records by a topic partition that doesn't exist:" (records-by-topic-partition cr "no-one-of-consequence" 99))
        ;;we can see what topic partitions are in the result set - maybe we don't care about certain partitions
        (println "Topic Partitions in the result set:" (record-partitions cr))
        ;;the standard Clojure collection functions work of course since we implement Seq and IReduceInit
        (println "The first record:" (first cr))
        (println "The rest of the records:" (rest cr))
        (println "Take 5 records:" (take 5 cr))
        (println "A vector of inconceivable things, through the magic of transduction:")
        (clojure.pprint/pprint (into [] inconceivable-transduction cr))
        (println "Now just the values of all distinct records:")
        ;;a truly slow example for fun
        (clojure.pprint/pprint (into [] (comp (take 10) value-xf (distinct)) cr))
        (println "Put all the records into a vector (calls IReduceInit):" (into [] cr))
        (println "If you prefer inspecting a map:" (into [] (map #(into {} %)) cr))))))
