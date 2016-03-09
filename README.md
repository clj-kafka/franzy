# Franzy-Nippy

[Kafka](http://kafka.apache.org/documentation.html) serializer using the excellent serialization library, [Nippy](https://github.com/ptaoussanis/nippy).

Great fit with [Franzy](https://github.com/ymilky/franzy), a Clojure Kafka client, though not required. Feel free to use this serializer with any Kafka client, including via Java, Scala, Groovy, or any other JVM language.

## Why

* You want to compress your data when sending to and from Kafka.
* You want fast de/serialization, via nippy and its underlying implementation.
* You are using a Kafka client such as [Franzy](https://github.com/ymilky/franzy) and need a pluggable, robust serializer.
* You want to serialize Clojure data types with little effort.
* You want seamless serialization, no embedded serialization calls at call sites or `.getBytes` ugly things floating around.

## Docs

* Read the browsable [API](http://ymilky.github.io/franzy-nippy/api/index.html)
* See [Franzy Source](https://github.com/ymilky/franzy) and docs for more information about serializers/deserializers.
* For more information about serializer options, compression, etc, see the official [Nippy](https://github.com/ptaoussanis/nippy) repo.

## Installation

Add the necessary dependency to your project:

```clojure
[ymilky/franzy-nippy "0.0.1"]
```

## Serializing

First, require:

```clojure
(ns my-ns
  (:require [franzy.serialization.nippy.serializers :as serializers]))
```

Then use with a producer, such as the one with [Franzy](https://github.com/ymilky/franzy).

```clojure
  (let [;;optionally specify via Kakfa Config key - value.serializer using fully qualified class name
        pc {:bootstrap.servers ["127.0.0.1"]}
        ;;Serializes producer record keys, ex: (keyword-serializer) from Franzy
        key-serializer (your-key-serializer-type)
        ;;Serializes producer record values using nippy, call (serializers/nippy-serializer options) to pass nippy options
        value-serializer (serializers/nippy-serializer)]
    (with-open [p (producer/make-producer pc key-serializer value-serializer)]
      ;;spray useless data to Kafka using Clojure types
      (send-async! "aliens-wearing-curtains" 2262 
      ["s: Who? The Narn or the Centauri? k: yes" "...is dead. 
      k: We are all Kosh." "If you watch Legend of the Rangers, you will die."]))))
```

## Deserializing

First, require:

```clojure
(ns my-ns
  (:require [franzy.serialization.nippy.deserializers :as deserializers]))
```

Then use with a consumer, such as the one with [Franzy](https://github.com/ymilky/franzy).

```clojure
  (let [;;optionally specify via Kafka Config key - value.deserializer using fully qualified class name
        cc {:bootstrap.servers ["127.0.0.1:9092"]
            :group.id          "we-are-purple"}
        ;;Deserializes your record keys, ex: (keyword-deserializer) from Franzy
        key-deserializer (your-key-deserializer-type)
        ;;the value deserializer should be the same kind as the serializer, don't mix and match
        ;;call (serializers/nippy-deserializer options) to pass nippy options
        value-deserializer (deserializers/nippy-deserializer)
        topic "aliens-wearing-curtains"
        topic-partitions [{:topic topic :partition 2262}]]
    (with-open [c (consumer/make-consumer cc key-deserializer value-deserializer)]
      (assign-partitions! c topic-partitions)
      (seek-to-beginning-offset! c topic-partitions)
      (poll! c)))
```

## License

Copyright © 2016 Yossi M. (ymilky).

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.

Use at your own risk, I am not responsible or liable. Please give credit if you use pieces of this library or otherwise, it is much appreciated.
