(ns franzy.clients.mocks.consumer.mock-consumer-tests
  (:require [midje.sweet :refer :all]
            [schema.core :as s]
            [franzy.clients.consumer.protocols :refer :all]
            [franzy.clients.mocks.consumer.protocols :refer :all]
            [franzy.clients.mocks.protocols :refer :all]
            [franzy.common.metadata.protocols :refer :all]
            [franzy.clients.mocks.consumer.client :as consumer]
            [franzy.clients.consumer.defaults :as defaults]
            [franzy.clients.consumer.types :as pt]
            [franzy.clients.consumer.callbacks :as callbacks])
  (:import (franzy.clients.consumer.client FranzConsumer)))

(fact
  "A mock producer can be created using a real producer with the implementations swapped with mock implementations."
  (let [c (consumer/make-mock-consumer :earliest)]
    (nil? c) => false
    (instance? FranzConsumer c) => true
    (.close c)))

(fact
  "A mock producer can accept an alternative offset strategy when constructed."
  (with-open [c (consumer/make-mock-consumer :none)]
    (nil? c) => false
    (instance? FranzConsumer c) => true))

(fact
  "A mock producer can accept producer options when constructed."
  (with-open [c (consumer/make-mock-consumer :earliest (defaults/make-default-consumer-options))]
    (nil? c) => false
    (instance? FranzConsumer c) => true))

(fact
  "A mock consumer needs to be assigned topic partitions"
  (with-open [c (consumer/make-mock-consumer :earliest)]
    (let [first-tp {:topic "heads-that-talk" :partition 0}
          second-tp {:topic "heads-that-talk" :partition 1}
          topic-partitions [first-tp second-tp]]
      (assign-partitions! c topic-partitions)
      (assigned-partitions c) => #{first-tp second-tp})))

(fact
  "A mock consumer can use subscriptions to receive an assignment."
  (with-open [c (consumer/make-mock-consumer :earliest)]
    (subscribe-to-partitions! c ["heads-that-talk"])
    (partition-subscriptions c) => #{"heads-that-talk"}))

(fact
  "A mock consumer can start from any offset specified"
  (with-open [c (consumer/make-mock-consumer :earliest)]
    (let [first-tp {:topic "heads-that-talk" :partition 0}
          second-tp {:topic "heads-that-talk" :partition 1}
          topic-partitions [first-tp second-tp]]
      (assign-partitions! c topic-partitions)
      (beginning-offsets! c {first-tp 0 second-tp 20})
      (seek-to-beginning-offset! c topic-partitions)
      (next-offset c first-tp) => 0
      (next-offset c second-tp) => 20)))

(fact
  "A mock consumer can be checked if it is closed."
  (with-open [c (consumer/make-mock-consumer :earliest)]
    (closed? c) => false))


(fact
  "A mock consumer can simulate consumer rebalance events."
  (let [first-tp {:topic "heads-that-talk" :partition 0}
        second-tp {:topic "heads-that-talk" :partition 1}
        third-tp {:topic "heads-that-talk" :partition 55}
        partition-assigned-fn (fn [topic-parts]
                                (println "assigned topic parts:" topic-parts))
        partition-revoked-fn (fn [topic-parts]
                               (println "revoked topic parts:" topic-parts))
        callbacks (callbacks/consumer-rebalance-listener partition-assigned-fn partition-revoked-fn)
        consumer-opts {:rebalance-listener-callback callbacks}]
    (with-open [c (consumer/make-mock-consumer :earliest consumer-opts)]
      (subscribe-to-partitions! c ["heads-that-talk"] consumer-opts)
      (beginning-offsets! c {first-tp 0 second-tp 20})
      (rebalance! c [third-tp])
      (beginning-offsets! c {third-tp 12})
      (poll! c consumer-opts))))

(fact
  "A mock consumer can add records to poll for."
  (let [topic "love-my-way"
        first-tp {:topic topic :partition 0}
        second-tp {:topic topic :partition 1}
        topic-partitions [first-tp second-tp]
        cr1 (pt/->ConsumerRecord topic 0 0 :boys-dont-cry "I wanna be a cowboy.")
        cr2 (pt/->ConsumerRecord topic 0 1 :boys-dont-cry "I wanna be a cowgirls.")]
    (with-open [c (consumer/make-mock-consumer :earliest nil)]
      (beginning-offsets! c {first-tp 0 second-tp 1})
      (assign-partitions! c topic-partitions)
      (add-record! c cr1)
      (add-record! c cr2)
      (into [] (poll! c)) => [cr1 cr2])))

(fact
  "A mock consumer can update topic partitions."
  (let [topic "love-my-way"
        first-tp {:topic topic :partition 0}
        second-tp {:topic topic :partition 1}
        topic-partitions [first-tp second-tp]]
    (with-open [c (consumer/make-mock-consumer :earliest nil)]
      (beginning-offsets! c {first-tp 0 second-tp 1})
      (assign-partitions! c topic-partitions)
      (update-partitions! c topic {:topic            topic :partition 0
                                   :leader           {:id 1234 :host "127.0.0.1" :port 2112}
                                   :replicas         [{:id 1235 :host "127.0.0.1" :port 2112}]
                                   :in-sync-replicas [{:id 1236 :host "127.0.0.1" :port 2112}]}))))

;;TODO: more tests
