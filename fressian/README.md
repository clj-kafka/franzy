# Franzy-Fressian

[Kafka](http://kafka.apache.org/documentation.html) serializer using [Fressian](https://github.com/Datomic/fressian).

Great fit with [Franzy](https://github.com/ymilky/franzy), a Clojure Kafka client, though not required. Feel free to use this serializer with any Kafka client, including via Java, Scala, Groovy, or any other JVM language.

## Why

* You want to compress your data when sending to and from Kafka.
* You want tight integration between [Datomic](http://www.datomic.com), perhaps in a CQRS system.
* You want de/serialization and compression, via Fressian.
* You are using a Kafka client such as [Franzy](https://github.com/ymilky/franzy) and need a pluggable, robust serializer.
* You want to serialize Clojure data types with little effort.
* You want seamless serialization, no embedded serialization calls at call sites or `.getBytes` ugly things floating around.

## Docs

* Read the browsable [API](http://ymilky.github.io/franzy-fressian/api/index.html)
* See [Franzy Source](https://github.com/ymilky/franzy) and docs for more information about serializers/deserializers.
* For more information about serializer options, compression, etc, see the official [Fressian](https://github.com/Datomic/fressian) docs.

## Installation

Add the necessary dependency to your project:

```clojure
[clj-kafka.franzy/fressian "0.0.1"]
```

[![Clojars Project](https://img.shields.io/clojars/v/clj-kafka.franzy/fressian.svg)](https://clojars.org/clj-kafka.franzy/fressian)

## Serializing

First, require:

```clojure
(ns my-ns
  (:require [franzy.serialization.fressian.serializers :as serializers]))
```

Then use with a producer, such as the one with [Franzy](https://github.com/ymilky/franzy).

```clojure
  (let [;;optionally specify via Kakfa Config key - value.serializer using fully qualified class name
        pc {:bootstrap.servers ["127.0.0.1"]}
        ;;Serializes producer record keys, ex: (keyword-serializer) from Franzy
        key-serializer (your-key-serializer-type)
        ;;Serializes producer record values using Fressian, call (serializers/fressian-serializer options) to pass Fressian options
        value-serializer (serializers/fressian-serializer)]
    (with-open [p (producer/make-producer pc key-serializer value-serializer)]
      ;;spray useless data to Kafka using Clojure types
      (send-async! "chickens-and-beyond" 12
      {:chicken-forms ["nugget" "roasted" "fried" "broiled" "boiled" "grilled" "floor dropped"]
       :tasty :sometimes
       :sale-price 9.99}))))
```

## Deserializing

First, require:

```clojure
(ns my-ns
  (:require [franzy.serialization.fressian.deserializers :as deserializers]))
```

Then use with a consumer, such as the one with [Franzy](https://github.com/ymilky/franzy).

```clojure
  (let [;;optionally specify via Kafka Config key - value.deserializer using fully qualified class name
        cc {:bootstrap.servers ["127.0.0.1:9092"]
            :group.id          "we-are-purple"}
        ;;Deserializes your record keys, ex: (keyword-deserializer) from Franzy
        key-deserializer (your-key-deserializer-type)
        ;;the value deserializer should be the same kind as the serializer, don't mix and match
        ;;call (serializers/fressian-deserializer options) to pass Fressian options
        value-deserializer (deserializers/fressian-deserializer)
        topic "chickens-and-beyond"
        topic-partitions [{:topic topic :partition 12}]]
    (with-open [c (consumer/make-consumer cc key-deserializer value-deserializer)]
      (assign-partitions! c topic-partitions)
      (seek-to-beginning-offset! c topic-partitions)
      (poll! c)))
```

## License

Copyright © 2016 Yossi M. (ymilky).

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.

Use at your own risk, I am not responsible or liable. Please give credit if you use pieces of this library or otherwise, it is much appreciated.
