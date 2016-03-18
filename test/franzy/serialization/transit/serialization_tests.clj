(ns franzy.serialization.transit.serialization-tests
  (:require [midje.sweet :refer :all]
            [franzy.serialization.transit.serializers :as serializers]
            [franzy.serialization.transit.deserializers :as deserializers])
  (:import (org.apache.kafka.common.serialization Deserializer Serializer)
           (java.util UUID)))

;;TODO: better check on facts
(facts
  "Transit serializers/deserializers serialize and deserialize Clojure data structures." :serializers
  (let [serializer (serializers/transit-serializer :json)
        deserializer (deserializers/transit-deserializer :json)
        topic "music-education"
        data {:good-bands                                             ["Slowdive" "Cocteau Twins" "Velvet Underground"]
              :music-people-play-on-guitar-but-you-dont-want-to-heard '("Freedbird" "Indigo Girls" "All Folk" "Dave Matthews")
              :good-year                                              1986
              :essential-album                                        "Meat is Murder"
              :most-overrated                                         "Lady Gaga"
              :good-music-this-year                                   nil}]
    (fact
      "A Transit serializer is a Kafka serializer." :serializers
      (instance? Serializer serializer) => true)
    (fact
      "A Transit deserializer is a Kafka deserializer." :serializers
      (instance? Deserializer deserializer) => true)
    (fact
      "A Transit serializer and deserializer must be passed a valid supported format." :serializers
      (with-open [invalid-serializer  (serializers/transit-serializer :cookies)
                  invalid-deserializer (deserializers/transit-deserializer :sprinkles)
                  json-serializer (serializers/transit-serializer :json)
                  json-deserializer (deserializers/transit-deserializer :json)
                  verbose-serializer (serializers/transit-serializer :json-verbose)
                  verbose-deserializer (deserializers/transit-deserializer :json-verbose)
                  msgpack-serializer (serializers/transit-serializer :msgpack)
                  msgpack-deserializer (deserializers/transit-deserializer :msgpack)]
        (.serialize invalid-serializer "cheese" "chocolate") => (throws Exception)
        (.deserialize invalid-deserializer "ween" (.getBytes "pushing little daisies")) => (throws Exception)
        (->> (.serialize json-serializer "music-thieves" "backing tracks")
             (.deserialize json-deserializer "music-thieves")) =not=> (throws Exception)
        (->> (.serialize verbose-serializer "music-thieves" "backing tracks")
             (.deserialize verbose-deserializer "music-thieves")) =not=> (throws Exception)
        (->> (.serialize msgpack-serializer "music-thieves" "backing tracks")
             (.deserialize msgpack-deserializer "music-thieves")) =not=> (throws Exception)))
    (fact
      "A Transit serializer can serialize a string." :serializers
      (let [val "Slowdive is under appreciated"
            output (.serialize serializer topic val)]
         (nil? output) => false
         (> (alength output) 0) => true
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A Transit serializer can serialize an number." :serializers
      (let [val 1982]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A Transit serializer can serialize a short." :serializers
      (let [val (short 1)]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A Transit serializer can serialize a long." :serializers
      (let [val (long Long/MAX_VALUE)]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A Transit serializer can serialize an integer." :serializers
      (let [val (int Integer/MAX_VALUE)]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A Transit serializer can serialize a UUID." :serializers
      (let [val (UUID/randomUUID)]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A Transit serializer can serialize a map." :serializers
      (let [val {:great-song "DIIV - Follow"}]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A Transit serializer can serialize a vector." :serializers
      (let [val ["Flaming Lips" "Guided by Voices" "Yo La Tengo"]]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A Transit serializer can serialize a list." :serializers
      (let [val '("Pixies" "Sonic Youth" "Pavement")]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A Transit serializer can serialize a set." :serializers
      (let [val #{"Do You Remember The First Time?" "Sorted Out for Es and Whizz" "Disco 2000"}]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => (contains val)))
    (fact
      "A Transit serializer can serialize a keywords." :serializers
      (let [val :hipsters-love-television-marquee-moon]
        (.serialize serializer topic val) =not=> nil
        (->> (.serialize serializer topic val)
             (.deserialize deserializer topic)) => val))
    (fact
      "A Transit serializer can not serialize a function, but it's stupid to do so." :serializers
      (let [val (fn [x] (+ 1 x))]
        (.serialize serializer topic val) => (throws Exception)))
    (fact
      "A Transit serializer using json verbose should be able to produce the same data in a round trip." :serializers
      (let [deserializer (deserializers/transit-deserializer :json-verbose)]
        (->> (.serialize serializer topic data)
             (.deserialize deserializer topic))) => data)
    (fact
      "A Transit serializer should handle nil data, just in case..." :serializers
      (->> (.serialize serializer topic nil) => nil))
    (fact
      "A Transit deserializer should handle nil data, just in case..." :serializers
      (->> (.deserialize deserializer topic nil) => nil))))
