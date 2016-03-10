(ns franzy.serialization.json.serialization-tests
  (:require [midje.sweet :refer :all]
            [franzy.serialization.json.serializers :as serializers]
            [franzy.serialization.json.deserializers :as deserializers])
  (:import (org.apache.kafka.common.serialization Deserializer Serializer)
           (java.util UUID)))

;;TODO: better check on facts
(facts
  "JSON serializers/deserializers serialize and deserialize Clojure data structures." :serializers
  (let [serializer (serializers/json-serializer)
        deserializer (deserializers/json-deserializer)
        topic "music-education"
        data {:good-bands ["Camera Obscura" "Mazzy Star" "Best Coast"]
              :music-people-play-on-guitar-but-you-dont-want-to-heard '("Freedbird" "Indigo Girls" "All Folk" "Dave Matthews")
              :good-year 1988
              :essential-album "My Maudlin Career"
              :most-overrated "Adele"
              :good-music-this-year nil}]
    (fact
      "A JSON serializer is a Kafka serializer." :serializers
      (instance? Serializer serializer) => true)
    (fact
      "A JSON deserializer is a Kafka deserializer." :serializers
      (instance? Deserializer deserializer) => true)
    (fact
      "A JSON serializer can serialize a string." :serializers
      (let [val "Slowdive is under appreciated"]
        (.serialize serializer topic val) => not-empty

        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A json serializer can serialize an number." :serializers
      (let [val 1982]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A json serializer can serialize a short." :serializers
      (let [val (short 1)]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A json serializer can serialize a long." :serializers
      (let [val (long Long/MAX_VALUE)]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A JSON serializer can serialize an integer." :serializers
      (let [val (int Integer/MAX_VALUE)]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A JSON serializer can serialize a UUID, but it will be returned as a string without a handler." :serializers
      (let [val (UUID/randomUUID)]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => (str val)))
    (fact
      "A JSON serializer can serialize a map, but its keys will be strings without a deserializer key-fn." :serializers
      (let [val {:great-song "DIIV - Follow"}]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => {"great-song" "DIIV - Follow"}))
    (fact
      "A JSON deserializer can deserialize a map, and its keys will be keywords with a key-fn." :serializers
      (let [val {:great-song "DIIV - Follow"}
            deserializer (deserializers/json-deserializer {:key-fn true})]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A JSON serializer can munge its keys with a key-fn." :serializers
      (let [val {:things-to-buy-at-midnight "48 pack of toilet paper"}
            serializer (serializers/json-serializer {:key-fn (fn [k] (-> k (name) (.toUpperCase)))})]
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => {"THINGS-TO-BUY-AT-MIDNIGHT" "48 pack of toilet paper"}))
    (fact
      "A JSON serializer can serialize a vector." :serializers
      (let [val ["Flaming Lips" "Guided by Voices" "Yo La Tengo"]]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A JSON serializer can serialize a list." :serializers
      (let [val '("Pixies" "Sonic Youth" "Pavement")]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A JSON serializer can serialize a set." :serializers
      (let [val #{"Do You Remember The First Time?" "Sorted Out for Es and Whizz" "Disco 2000"}]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => (contains val)))
    (fact
      "A JSON serializer can serialize a keywords as names." :serializers
      (let [val :hipsters-love-television-marquee-moon]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => (name val)))
    (fact
      "A JSON serializer can not serialize a function, but it's stupid to do so." :serializers
      (let [val (fn [x] (+ 1 x))]
        (.serialize serializer topic val) => (throws Exception)))
    (fact
      "A JSON serializer should be able to produce the same data in a round trip." :serializers
      (let [deserializer (deserializers/json-deserializer {:key-fn true})]
        (->> (.serialize serializer topic data)
             (.deserialize deserializer topic))) => data)
    (fact
      "A JSON serializer should handle nil data, just in case..." :serializers
      (->> (.serialize serializer topic nil) => nil))
    (fact
      "A JSON deserializer should handle nil data, just in case..." :serializers
      (->> (.deserialize deserializer topic nil) => nil))))

;;These tests are dupes, but in the past had some weird SMILE specific things, so separating the tests
(facts
  "JSON SMILE serializers/deserializers serialize and deserialize Clojure data structures." :serializers
  (let [serializer (serializers/smile-serializer)
        deserializer (deserializers/smile-deserializer)
        topic "music-education"
        data {:good-bands ["Camera Obscura" "Mazzy Star" "Best Coast"]
              :music-people-play-on-guitar-but-you-dont-want-to-heard '("Freedbird" "Indigo Girls" "All Folk" "Dave Matthews")
              :good-year 1988
              :essential-album "My Maudlin Career"
              :most-overrated "Adele"
              :good-music-this-year nil}]
    (fact
      "A JSON serializer is a Kafka serializer." :serializers
      (instance? Serializer serializer) => true)
    (fact
      "A JSON deserializer is a Kafka deserializer." :serializers
      (instance? Deserializer deserializer) => true)
    (fact
      "A JSON serializer can serialize a string." :serializers
      (let [val "Slowdive is under appreciated"]
        (.serialize serializer topic val) => not-empty

        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A json serializer can serialize an number." :serializers
      (let [val 1982]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A json serializer can serialize a short." :serializers
      (let [val (short 1)]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A json serializer can serialize a long." :serializers
      (let [val (long Long/MAX_VALUE)]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A JSON serializer can serialize an integer." :serializers
      (let [val (int Integer/MAX_VALUE)]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A JSON serializer can serialize a UUID, but it will be returned as a string without a handler." :serializers
      (let [val (UUID/randomUUID)]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => (str val)))
    (fact
      "A JSON serializer can serialize a map, but its keys will be strings without a deserializer key-fn." :serializers
      (let [val {:great-song "DIIV - Follow"}]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => {"great-song" "DIIV - Follow"}))
    (fact
      "A JSON deserializer can deserialize a map, and its keys will be keywords with a key-fn." :serializers
      (let [val {:great-song "DIIV - Follow"}
            deserializer (deserializers/smile-deserializer {:key-fn true})]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A JSON serializer can munge its keys with a key-fn." :serializers
      (let [val {:things-to-buy-at-midnight "48 pack of toilet paper"}
            serializer (serializers/smile-serializer {:key-fn (fn [k] (-> k (name) (.toUpperCase)))})]
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => {"THINGS-TO-BUY-AT-MIDNIGHT" "48 pack of toilet paper"}))
    (fact
      "A JSON serializer can serialize a vector." :serializers
      (let [val ["Flaming Lips" "Guided by Voices" "Yo La Tengo"]]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A JSON serializer can serialize a list." :serializers
      (let [val '("Pixies" "Sonic Youth" "Pavement")]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A JSON serializer can serialize a set." :serializers
      (let [val #{"Do You Remember The First Time?" "Sorted Out for Es and Whizz" "Disco 2000"}]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => (contains val)))
    (fact
      "A JSON serializer can serialize a keywords as names." :serializers
      (let [val :hipsters-love-television-marquee-moon]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => (name val)))
    (fact
      "A JSON serializer can not serialize a function, but it's stupid to do so." :serializers
      (let [val (fn [x] (+ 1 x))]
        (.serialize serializer topic val) => (throws Exception)))
    (fact
      "A JSON serializer should be able to produce the same data in a round trip." :serializers
      (let [deserializer (deserializers/smile-deserializer {:key-fn true})]
        (->> (.serialize serializer topic data)
             (.deserialize deserializer topic))) => data)
    (fact
      "A JSON serializer should handle nil data, just in case..." :serializers
      (->> (.serialize serializer topic nil) => nil))
    (fact
      "A JSON deserializer should handle nil data, just in case..." :serializers
      (->> (.deserialize deserializer topic nil) => nil))))
