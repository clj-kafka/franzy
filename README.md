# Franzy-Transit

[Kafka](http://kafka.apache.org/documentation.html) [Transit](https://github.com/cognitect/transit-clj).

Supported formats include:

* JSON - `:json`
* JSON-verbose - `:json-verbose`
* Message Pack - `:msgpack`

Great fit with [Franzy](https://github.com/ymilky/franzy), a Clojure Kafka client, though not required.

## Why

* You want your Kafka storage format as JSON or Message Pack and enjoy some of the compression benefits of using Transit.
* You are working in Clojure/ClojureScript and you are already using [Transit](https://github.com/cognitect/transit-clj).
* You need to return data to things that can decode a binary stream of json, such as non-JVM languages or other apps.
* You want to serialize things to/from Clojure, even from other JVM languages.
* You want painless serialization of JSON without losing type information or extra munging for most use-cases.
* You want the possibility of deserializing your data back into Clojure easily.
* You are using a Kafka client such as [Franzy](https://github.com/ymilky/franzy) and need a plugable, robust serializer.
* You want to serialize Clojure data types with little effort.
* You want to compose with a message pack serializer/deserializer.
* You want seamless serialization, no embedded serialization calls at call sites or `.getBytes` ugly things floating around.

## Docs

* Read the browsable [API](http://ymilky.github.io/franzy-transit/)
* See [Franzy Source](https://github.com/ymilky/franzy) and docs for more information about serializers/deserializers.
* For more information about Transit, see the [official](https://github.com/cognitect/transit-clj) Transit Clojure project.

## Installation

Add the necessary dependency to your project:

```clojure
[ymilky/franzy-transit "0.0.1"]
```

## Serializing

First, require:

```clojure
(ns my-ns
  (:require [franzy.serialization.transit.serializers :as serializers]))
```

Then use with a producer, such as the one with [Franzy](https://github.com/ymilky/franzy).

```clojure
  (let [;;optionally specify via Kakfa Config key - value.serializer using fully qualified class name
        pc {:bootstrap.servers ["127.0.0.1"]}
        ;;Serializes producer record keys, ex: (keyword-serializer) from Franzy
        key-serializer (your-key-serializer-type)
        serializer-opts {:handlers my-handlers-map}
        value-serializer (serializers/transit-serializer :json serializer-opts)]
    (with-open [p (producer/make-producer pc key-serializer value-serializer)]
      ;;assault Kafka with data no one will ever see
      (send-async! "stolen-leftovers" 0
      {:thieves ["Brad" "Brad Again"}
       :delights `("Milk from 2 months ago" "1/2 Eaten Fried Chicken"
       "Rack of Lamb)})))
```

**You may not mix and match serializers and deserializers.**

## Deserializing

First, require:

```clojure
(ns my-ns
  (:require [franzy.serialization.transit.deserializers :as deserializers]))
```

Then use with a consumer, such as the one with [Franzy](https://github.com/ymilky/franzy).

```clojure
  (let [;;optionally specify via Kafka Config key - value.deserializer using fully qualified class name
        cc {:bootstrap.servers ["127.0.0.1:9092"]
            :group.id          "hungry-hippos"}
        ;;Deserializes your record keys, ex: (keyword-deserializer) from Franzy
        key-deserializer (your-key-deserializer-type)
        ;;ensure you're using the same format when deserializing
        ;;we could also specify an options map here too, but showing without
        value-deserializer (deserializers/transit-deserializer :json)
        topic "stolen-leftovers"
        topic-partitions [{:topic topic :partition 0}]]
    (with-open [c (consumer/make-consumer cc key-deserializer value-deserializer)]
      (assign-partitions! c topic-partitions)
      (seek-to-beginning-offset! c topic-partitions)
      (poll! c)))
```

Note that if you do not pass a format, the default will be JSON.

**You may not mix and match serializers and deserializers.**

## License

Copyright © 2016 Yossi M. (ymilky).

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.

Use at your own risk, I am not responsible or liable. Please give credit if you use pieces of this library or otherwise, it is much appreciated.
