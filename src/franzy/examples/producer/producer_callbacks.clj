(ns franzy.examples.producer.producer-callbacks
  (:require [franzy.clients.producer.client :as producer]
            [franzy.clients.producer.protocols :refer :all]
            [franzy.clients.producer.callbacks :as callbacks]
            [franzy.serialization.serializers :as serializers]
            [franzy.serialization.nippy.serializers :as nippy-serializers]
            [franzy.examples.configuration :as config]
            [taoensso.timbre :as timbre]
            [franzy.clients.producer.types :as pt]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Producer Send Callbacks
;;
;; This example demonstrates how a producer can receive a callback when its send completes.
;; You can use this information to determine flow, get information about the PR offset, handle exceptions, etc.
;;
;; From our house, to yours:
;;
;; 1. The send callback can be set at the call-site or via the producer options. I recommend setting it via the producer
;;    options because you don't need to worry about it ever again, and it is more efficient this way. It also discourages
;;    bad behavior like accidentally creating callbacks inside a loop. Don't do it.
;; 2. I'm going to repeat this - stop creating callbbacks in loops, create them ONCE. I've seen many examples and posts
;;    all over where people do this in various clients, especially the ofificial one. Just say no.
;;    This creates massive amounts of garbage for the GC. Allocations and GC are slow. Don't.
;; 3. Sometimes you do want a call-site send callback. In which case all the previous advice stands, however it's fine to
;;    pass the callback. No worries, just don't create it again and again.
;;    this, it's easy to check this info.
;; 4. There are multiple arities for creating callbacks. Either create 2 separate funcitons or 1 function that handles both.
;;    The reason for this is people often have different ways of handling errors in particular, and especially since the
;;    well-informed among you should probably be pushing these values to core.asyc channels or something similar.
;; 5. You can create your own custom callback and close around the values outside. Someone complained that there's no
;;    version that takes a map. Map destructuring would just slow things down even more. No. If you need more "stuff"
;;    passed in the callback, close over it. That's why the callback is reified here.
;;    A good alternative is to create your own gen-class or deftype and then you can go wild.
;; 6. This callback is a good option for logging.
;; 7. You get back a clojure map for record metadata converted nicely for you already from the Kafka types.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn callback-on-send-complete []

  (let [pc {:bootstrap.servers config/kafka-brokers}
        key-serializer (serializers/keyword-serializer)
        value-serializer (nippy-serializers/nippy-serializer)
        topic (:nippy-topic config/topics-to-create)
        first-partition 0
        second-partition 1
        ;;Here we create a custom send callback that allows us to take some action when the send completes
        ;;There are a few options to create this or roll your own, below is a very simple example:
        custom-send-callback (callbacks/send-callback (fn [record-metadata]
                                                        (timbre/info "My value (name) is:" record-metadata))
                                                      (fn [exceptional-exception]
                                                        (timbre/error "Prepare to die, because:" exceptional-exception)))
        another-send-callback (callbacks/send-callback (fn [record-metadata e]
                                                         (timbre/info "My record metadata, if I have it is:" record-metadata)
                                                         (timbre/info "If I have an error, it is:" e)))
        ;;notice, we manually set this option, which will be merged with the defaults
        options {:send-callback custom-send-callback}]
    ;;here our custom send callback gets passed when we construct the producer
    (with-open [p (producer/make-producer pc key-serializer value-serializer options)]
      (timbre/info "Sending...")
      ;;Here we send a topic, partition, key, and value like before, but we override the send callback with our own
      ;;You can do this on a per-call basis if you need, or just set the callback for all sends using this producer
      ;;when you create the producer options
      ;;Generally, you should avoid creating and recreating callbacks in a loop. Instead, close over it, create outside loop,
      ;;phone a friend, whatever works for you.
      ;; Don't do it. I do mean you.
      (let [send-fut (send-async! p topic first-partition :pitink-it "not left handed" {:send-callback another-send-callback})
            _ (timbre/info "sent...")
            ;;notice: not overrided, it will use the default - custom-send-callback - this is the usual preferred method
            send-fut-2 (send-async! p (pt/->ProducerRecord topic second-partition :drunken-spaniard {:father :dead
                                                                                                     :quest  "Read books to become good at swords."}))
            ;;Likewise we can override synchronously, however most of the time you probably don't want a send callback here
            ;;You can use this method if you want to send something synchronously, block automatically before your subsequent code,
            ;;But still fire some callback logic that does something, depsite the synchronous call.
            ;;Alternatively, you can just block when dereferecing the future from the async version. To each, their own.
            record-metadata (send-sync! p topic first-partition :crystaline-entity "billy" {:send-callback another-send-callback})]
        ;;You may notice that the error handler is not called, this is because there may have been no error.
        ;;If it has been called, probably time to get a cup of coffee and prepare to look disgruntled at your desk.
        (println "Sync send results:" record-metadata)
        (println "Async send results:" @send-fut)
        (println "Second async send results:" @send-fut-2)))))
