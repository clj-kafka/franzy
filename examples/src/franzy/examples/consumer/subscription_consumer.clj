(ns franzy.examples.consumer.subscription-consumer
  (:require [franzy.clients.consumer.client :as consumer]
            [franzy.clients.consumer.protocols :refer :all]
            [franzy.clients.consumer.defaults :as cd]
            [franzy.serialization.nippy.deserializers :as nippy-deserializers]
            [franzy.serialization.deserializers :as deserializers]
            [franzy.examples.configuration :as config]
            [franzy.common.models.types :as mt]
            [franzy.clients.consumer.callbacks :as callbacks]
            [taoensso.timbre :as timbre]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Subscription Consumer
;;
;; This examples demonstrates some of the functions involved with a consumer that is automatically assigned partitions
;; by Kafka
;;
;; Pay attention:
;;
;; 1.  We're explicitly not assigning a partition. In fact, it won't work if we try to assign and subscribe. The
;;     two methodologies are mutually exclusive.
;; 2.  We can seek to offsets, but we cannot seek before assignment. Assignment may not happen right away, so it is not
;;     something we can write linear code against. If for some reason we need to seek, we can do so by using a consumer
;;     rebalance listener.
;; 3.  We demonstrate a very simple consumer rebalance listener. When partitions are assigned or revoked, this callback
;;     is triggered. Similar advice regarding callbacks holds for consumers as it did for producers. Namely, do not create
;;     callback in loops. Do not constantly allocate new callbacks. See producer callbacks for more info.
;; 4.  We can query which topics and partitions we are subscribed to. Moreover, we can combine this with partitions-for
;;     to get some critical state info regarding a consumers and replica interactions.
;; 5.  We assign a specific consumer group id. If you want pub-sub-like semantics, each consumer should have its own group id.
;  6.  We can subscribe to multiple topics and multiple partitions of each topic at once.
;; 7.  We can clear and unsubscribe as needed. Meaning in the real-world, we may want to shift subscriptions for the same
;;     consumer.
;; 8.  Given some of the earlier points, it is an anti-pattern especially for active consumers to be constructed/re-allocated
;;     every time we want to poll/consume. Instead, setup a polling throlling and control it as needed as shown in the
;;     state management examples.
;; 9.  Read up and pay attention to which config keys are useful for a subscribing consumer. Below we're using auto-commit
;      to commit offsets to Kafka and then comitting them every 1000 milliseconds. Internally, our offsets are stored in
;;     Kafka itself, not Zookeeper. This should be much faster and if Kafka moves off Zookeeper one day, you'll be alright.
;;     There are some drawbacks, but read up more. Generally, this is very fast and efficient. You can reply the log
;;     of comitted offsets itself as it's nothing but another Kafka topic and set of partitions.
;; 10. You may not see any data and the poll may block because the offset you're jumping to has no more data in that partition
;;     to be consumed. Either reset to the earliest offset, change consumer groups, or even better, just produce more data.

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn subscribing-consumer []
  (let [cc {:bootstrap.servers       config/kafka-brokers
            :group.id                "submissive-blonde-aussies"
            :auto.offset.reset       :earliest
            ;;here we turn on committing offsets to Kafka itself, every 1000 ms
            :enable.auto.commit      true
            :auto.commit.interval.ms 1000}
        key-deserializer (deserializers/keyword-deserializer)
        value-deserializer (nippy-deserializers/nippy-deserializer)
        topic (:nippy-topic config/topics-to-create)
        ;;demonstrating using the topic partition record for kicks....
        ;;if you have a lot of partitions, it's better to allocate these than maps
        topic-partitions [(mt/->TopicPartition topic 0) (mt/->TopicPartition topic 1) (mt/->TopicPartition topic 2)]
        ;;Here we are demonstrating the use of a consumer rebalance listener. Normally you'd use this with a manual consumer to deal with offset management.
        ;;As more consumers join the consumer group, this callback should get fired among other reasons.
        ;;To implement a manual consumer without this function is folly, unless you care about losing data, and probably your job.
        ;;One could argue though that most data is not as valuable as we are told. I heard this in a dream once or in intro to Philosophy.
        rebalance-listener (callbacks/consumer-rebalance-listener (fn [topic-partitions]
                                                                    (timbre/info "topic partitions assigned:" topic-partitions))
                                                                  (fn [topic-partitions]
                                                                    (timbre/info "topic partitions revoked:" topic-partitions)))
        ;;We create custom producer options and set out listener callback like so.
        ;;Now we can avoid passing this callback every call that requires it, if we so desire
        ;;Avoiding the extra cost of creating and garbage collecting a listener is a best practice
        options (cd/make-default-consumer-options {:rebalance-listener-callback rebalance-listener})]
    (with-open [c (consumer/make-consumer cc key-deserializer value-deserializer options)]
      ;;Note! - The subscription will read your comitted offsets to position the consumer accordingly
      ;;If you see no data, try changing the consumer group temporarily
      ;;If still no, have a look inside Kafka itself, perhaps with franzy-admin!
      ;;Alternatively, you can setup another threat that will produce to your topic while you consume, and all should be well
      (subscribe-to-partitions! c [topic])
      ;;Let's see what we subscribed to, we don't need Cumberbatch to investigate here...
      (println "Partitions subscribed to:" (partition-subscriptions c))

      (let [cr (poll! c)
            filter-xf (filter (fn [cr] (= (:key cr) :vizzini)))
            value-xf (map (fn [cr] (:value cr)))
            inconceivable-transduction (comp filter-xf value-xf)]

        (println "Record count:" (record-count cr))
        (println "Records by topic:" (records-by-topic cr topic))
        (println "Records from a topic that doesn't exist:" (records-by-topic cr "no-one-of-consequence-999"))
        (println "Records by topic partition:" (records-by-topic-partition cr (first topic-partitions)))
        ;;;The source data is a list, so no worries here....
        (println "Records by a topic partition that doesn't exist:" (records-by-topic-partition cr "no-one-of-consequence" 99))
        (println "Topic Partitions in the result set:" (record-partitions cr))
        (clojure.pprint/pprint (into [] inconceivable-transduction cr))
        (println "Put all the records into a vector (calls IReduceInit):" (into [] cr))
        ;;wow, that was tiring, maybe now we don't want to listen anymore to this topic and take a break, maybe subscribe
        ;;to something else next poll....
        (clear-subscriptions! c)
        (println "After clearing subscriptions, a stunning development! We are now subscribed to the following partitions:"
                 (partition-subscriptions c))))))
