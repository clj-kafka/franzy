(ns franzy.serialization.fressian.serialization-tests
  (:require [midje.sweet :refer :all]
            [franzy.serialization.fressian.serializers :as serializers]
            [franzy.serialization.fressian.deserializers :as deserializers])
  (:import (org.apache.kafka.common.serialization Deserializer Serializer)
           (java.util UUID)))

;;TODO: better check on facts
(facts
  "Fressian serializers/deserializers serialize and deserialize Clojure data structures." :serializers
  (let [serializer (serializers/fressian-serializer)
        deserializer (deserializers/fressian-deserializer)
        topic "music-education"
        data {:good-bands                    ["New Order" "Joy Division" "The Cure" "The Smiths" "Pulp" "Jesus and Mary Chain"]
              :terrible-bands                #{"The Eagles" "Most of American Music in the 90s"}
              :things-pretending-to-be-bands `("Justin Bieber" "Kanye West" "Beonce" "Arcade Fire")
              :good-year                     1984
              :char                          (char 1)
              :essential-album               :script-of-the-bridge-by-the-chameleons
              :most-overrated                "Jennifer Lopez"
              :good-music-this-year          nil}]
    (fact
      "A Fressian serializer is a Kafka serializer." :serializers
      (instance? Serializer serializer) => true)
    (fact
      "A Fressian deserializer is a Kafka deserializer." :serializers
      (instance? Deserializer deserializer) => true)
    (fact
      "A Fressian serializer can serialize a string." :serializers
      (let [val "Slowdive is under appreciated"]
        (.serialize serializer topic val) => not-empty

        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A fressian serializer can serialize an number." :serializers
      (let [val 1982]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A fressian serializer can serialize a short." :serializers
      (let [val (short 1)]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A fressian serializer can serialize a long." :serializers
      (let [val (long Long/MAX_VALUE)]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A Fressian serializer can serialize an integer." :serializers
      (let [val (int Integer/MAX_VALUE)]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A Fressian serializer can serialize a UUID." :serializers
      (let [val (UUID/randomUUID)]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A Fressian serializer can serialize a map." :serializers
      (let [val {:great-song "porcelain raft - dragonfly"}]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A Fressian serializer can serialize a vector." :serializers
      (let [val ["DIIV" "Chapterhouse" "Moose" "Ride"]]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A Fressian serializer can serialize a list." :serializers
      (let [val '("Suede" "Blur" "Elbow" "Cast" "Doves")]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A Fressian serializer can serialize a set." :serializers
      (let [val #{"Forget That You're Young" "Black Satin" "Hallucinations" "With My Eyes Closed" "Here Comes Mary"}]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A Fressian serializer can serialize a keywords." :serializers
      (let [val :raveonettes]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A Fressian serializer can not serialize a function, but it's stupid to do so." :serializers
      (let [val (fn [x] (+ 1 x))]
        (.serialize serializer topic val) =not=> nil))
    (fact
      "A Fressian serializer should be able to produce the same data in a round trip." :serializers
      (->> (.serialize serializer topic data)
           (.deserialize deserializer topic)) => data)
    (fact
      "A Fressian serializer should handle nil data, just in case..." :serializers
      (->> (.serialize serializer topic nil) =not=> nil))
    (fact
      "A Fressian deserializer should handle nil data, just in case..." :serializers
      (->> (.deserialize deserializer topic nil) => nil))))
