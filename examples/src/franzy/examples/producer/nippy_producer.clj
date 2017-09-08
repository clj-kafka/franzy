(ns franzy.examples.producer.nippy-producer
  (:require [franzy.serialization.serializers :as serializers]
            [franzy.examples.configuration :as config]
            [franzy.clients.producer.client :as producer]
            [franzy.clients.producer.protocols :refer :all]
            [franzy.clients.producer.types :as pt]
            [franzy.serialization.nippy.serializers :as nippy-serializers]
            [franzy.serialization.serializers :as serializers]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Nippy Producer
;;
;;
;; Things to bore your colleagues:
;;
;; 1. We're using nippy to serialize, which is a pretty great choice for most people
;; 2. Real clojure objects, no .getBytes or inline serialization
;; 3. We referenced franzy-nippy to use this serializer since it's not in the core
;; 4. When you produce, you generally want to do so in your own thread and loop. Do not create/re-create the producer
;;    all the time unless your producer is idle for long periods. If you're constantly producing, recreating the producer
;;    in a loop/every call to produce is an anti-pattern.
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn produce-nippy []
  (let [;;notice vs the string producer example, we're going with much simpler defaults to demonstrate you don't need all that ceremony
        pc {:bootstrap.servers config/kafka-brokers
            ;;Best Practice: Set a client-id for better auditing, easier admin operations, etc.
            :client.id         "hungry-eels"}
        ;;now we are using a keyword serializer for keys, so we can use Clojure keywords as keys if we want
        key-serializer (serializers/keyword-serializer)
        ;;now we are using the Nippy serializer
        value-serializer (nippy-serializers/nippy-serializer)
        topic (config/topics-to-create :nippy-topic)
        first-partition 0
        second-partition 1
        third-partition 2]
    ;;this producer is created without passing options, if you have no need....
    (with-open [p (producer/make-producer pc key-serializer value-serializer)]

      (let [producer-record-map {:topic     topic
                                 :partition first-partition
                                 :key       :six-fingered-man
                                 :value     {:inventions ["The machine" "Goofy hallyway running"]
                                             :six-finger true
                                             :hand       :left
                                             :hobbies    #{"Sword butt hitting" "Being fresh with princes"}}}
            ;;notice we use the factory function for our record this time and refer....whichever you prefer sir/madame
            producer-record (pt/->ProducerRecord topic second-partition :vizzini {:quotes                                ["the battle of wits has begun!" "finish him, your way!"]
                                                                                  :bald                                  :in-dreams
                                                                                  :does-not-mean-what-he-thinks-it-means true})
            ;;sending the producer record explicitly
            send-fut (send-async! p producer-record)
            ;;sending the producer record map explicitly
            record-metadata (send-sync! p producer-record-map)]
        (println "Sync send results:" record-metadata)
        (println "Async send results:" @send-fut)
        ;;and one last record returned directly into your repl, this time using explicit params and passing a nil options for fun
        (send-sync! p topic third-partition :roaring-rodent {:tag-line                "Hello friends, I am of unusual size"
                                                             :quotes                  ["roar" "moan" "grrrr" "aaaaar" "errrr"]
                                                             :bite                    true
                                                             :wesley-believes-exists? false} nil)))))


