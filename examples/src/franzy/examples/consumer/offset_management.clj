(ns franzy.examples.consumer.offset-management
  (:require [franzy.serialization.deserializers :as deserializers]
            [franzy.clients.consumer.protocols :refer :all]
            [franzy.clients.consumer.callbacks :refer :all]
            [franzy.clients.consumer.defaults :as cd]
            [franzy.clients.consumer.client :as consumer]
            [franzy.serialization.nippy.deserializers :as nippy-deserializers]
            [franzy.examples.configuration :as config]
            [taoensso.timbre :as timbre]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Offset Management
;;
;; This example demonstrates how you can add metadata and manipulate offsets in Kafka...
;;
;; Guru violent meditation:
;;
;; 1. Offsets can be committed synchronously or asynchronously
;; 2. We can add metadata to offsets
;; 3. Async offsets have some special rules vs. sync offsets, shown below
;; 4. Polling and seeking is related to async commits
;; 5. We can create a callback and get notified when an async offset commits. Usually we want to do this, unless we
;;    enjoy the uncertainty that comes with commitment.
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn manage-offsets []
  (let [cc {:bootstrap.servers config/kafka-brokers
            :client.id         "remember-the-tracy-ullman-show"
            :group.id          "guilder"
            :auto.offset.reset :earliest}
        key-deserializer (deserializers/keyword-deserializer)
        value-deserializer (nippy-deserializers/nippy-deserializer)
        options (cd/make-default-consumer-options)
        topic (:nippy-topic config/topics-to-create)
        first-topic-partition {:topic topic :partition 0}
        second-topic-partition {:topic topic :partition 1}
        topic-partitions [first-topic-partition second-topic-partition]]
    (with-open [c (consumer/make-consumer cc key-deserializer value-deserializer options)]
      (timbre/info "assigning partitions...")
      ;;first we'll make sure we can assign some partitions. We could also subscribe instead, but for examples, this is easier.
      (assign-partitions! c topic-partitions)
      (timbre/info "assigned partitions, now seeking...")
      (seek-to-beginning-offset! c topic-partitions)
      (timbre/info "finished seeking, getting next offset...")
      ;;let's peek at what the next offset is.....it should be 0 if we're at the beginning and we didn't compact or delete yet
      (println "Next offset:" (next-offset c first-topic-partition))
      ;;now maybe we want to save some metadata about the beginning offset....
      ;;Notice, we're sending a map with the keys a topic partition map as the key, and the value as an offset metadata map
      (commit-offsets-sync! c {first-topic-partition {:offset 0, :metadata "In the beginning.....that was a long time ago."}})
      ;;Now let's have a peek at what we committed. If you've done this before, there might be other data obviously
      (println "Committed offsets so far:" (committed-offsets c first-topic-partition))
      ;;Now let's commit the next offset (there should be one if you produced data already), but this time async
      (commit-offsets-async! c {first-topic-partition {:offset 1 :metadata "Those who count from one, are but two."}})
      ;;Another peek at the results, but this might surprise you if your thinking cap is at the cleaners
      (println "Committed offsets after first async call:" (committed-offsets c first-topic-partition))
      ;;The problem here is you passed the offsets as the options map! Don't do it.
      ;; OK, if not then what about other arities?
      (commit-offsets-async! c {first-topic-partition {:offset 1 :metadata "Those who count from one, are but two."}} nil)
      (println "Committed offsets after proper async call:" (committed-offsets c first-topic-partition))
      ;;Nope, still no new data, but what about doing it sync
      (commit-offsets-sync! c {first-topic-partition {:offset 1 :metadata "Those who count from one, are but two."}})
      (println "Committed offsets after 2nd sync call:" (committed-offsets c first-topic-partition))
      ;;OK, great, doing it sync worked, but why?
      ;;Let's create some callbacks so we have a better idea what is going on
      ;;We could use these to do all kinds of fun stuff, like store this metadata in our own shiny database
      (let [occ (offset-commit-callback (fn [offset-metadata]
                                          (println "By the wind shalt be, commit succeeded:" offset-metadata))
                                        (fn [e]
                                          (println "Offsets failed to commit, just like you:" e)))]
        ;;notice the different arity and the fact we pass our callback.
        ;; We could have also just set this in the consumer options, in which case, there would be no need to use this arity
        ;; Unless the callback changed per-call, in which case, someone somewhere has read your code, then engaged the grumble-drive.
        (commit-offsets-async! c {first-topic-partition {:offset 2 :metadata "A Nancy to a Tanya"}} {:offset-commit-callback occ})
        (println "Committed offsets after async callback version:" (committed-offsets c first-topic-partition))
        ;;ok, why are there still no offsets?
        ;;let's try to follow the Franzy documentation! READ IT!
        ;;first, let's poll from offset 2, so we'll need to seek to it
        (seek-to-offset! c first-topic-partition 2)
        ;;and to poll, results are not important as long as we got at least 1 - you did populate the data, didn't you?
        (poll! c)
        (commit-offsets-async! c {first-topic-partition {:offset 2 :metadata "A Nancy to a Tanya"}} {:offset-commit-callback occ})
        (println "Committed offsets after listening to the doc about polling with async commits:" (committed-offsets c first-topic-partition))
        ;;all is well, that was certainly traumatic.....
        ))))
