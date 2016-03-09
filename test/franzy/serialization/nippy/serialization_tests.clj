(ns franzy.serialization.nippy.serialization-tests
  (:require [midje.sweet :refer :all]
            [franzy.serialization.nippy.serializers :as serializers]
            [franzy.serialization.nippy.deserializers :as deserializers])
  (:import (org.apache.kafka.common.serialization Deserializer Serializer)
           (java.util UUID)))

(facts
  "Nippy serializers/deserializers serialize and deserialize Clojure data structures." :serializers
  (let [serializer (serializers/nippy-serializer)
        deserializer (deserializers/nippy-deserializer)
        topic "music-education"
        data {:good-bands ["New Order" "Joy Division" "The Cure" "The Smiths" "Pulp" "Jesus and Mary Chain"]
              :terrible-bands #{"The Eagles" "Most of American Music in the 90s"}
              :things-pretending-to-be-bands `("Justin Bieber" "Kanye West" "Beonce" "Arcade Fire")
              :good-year 1984
              :essential-album :script-of-the-bridge-by-the-chameleons
              :most-overrated "Jennifer Lopez"
              :good-music-this-year nil}]
    (fact
      "A nippy serializer is a Kafka serializer."
      (instance? Serializer serializer) => true)
    (fact
      "A nippy deserializer is a Kafka deserializer."
      (instance? Deserializer deserializer) => true)
    (fact
      "A nippy serializer can serialize a string."
      (let [val "Slowdive is under appreciated"]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A nippy serializer can serialize an number."
      (let [val 1982]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A nippy serializer can serialize a short."
      (let [val (short 1)]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A nippy serializer can serialize a long."
      (let [val (long Long/MAX_VALUE)]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A nippy serializer can serialize an integer."
      (let [val (int Integer/MAX_VALUE)]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A nippy serializer can serialize a UUID."
      (let [val (UUID/randomUUID)]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A nippy serializer can serialize a map."
      (let [val {:great-song "porcelain raft - dragonfly"}]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A nippy serializer can serialize a vector."
      (let [val ["DIIV" "Chapterhouse" "Moose" "Ride"]]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A nippy serializer can serialize a list."
      (let [val '("Suede" "Blur" "Elbow" "Cast" "Doves")]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A nippy serializer can serialize a set."
      (let [val #{"Forget That You're Young" "Black Satin" "Hallucinations" "With My Eyes Closed" "Here Comes Mary"}]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A nippy serializer can serialize a keywords."
      (let [val :raveonettes]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A nippy serializer can serialize a function, but it's stupid to do so."
      (let [val (fn [x] (+ 1 x))]
        (.serialize serializer topic val) =not=> nil))
    (fact
      "A nippy serializer should be able to produce the same data in a round trip." :serializers
      (->> (.serialize serializer topic data)
           (.deserialize deserializer topic)) => data)
    (fact
      "A nippy serializer should handle nil data, just in case..."
      (->> (.serialize serializer topic nil) =not=> nil))
    (fact
      "A nippy deerializer should handle nil data, just in case..."
      (->> (.deserialize deserializer topic nil) =not=> nil))))
