# Franzy-Avro

[Kafka](http://kafka.apache.org/documentation.html) serializer/deserializer using [Avro](https://avro.apache.org/).

The emphasis of this serializer is on serializing EDN to Avro, though the serializer will support any provided schema format.

This serializer uses [Abracad](https://github.com/damballa/abracad) and any bugs, quirks, and associated features/limitations are thus present. As Abracada is as of this writing seeing some churn and bug fixing, there may be a few quirks.

Please review [Abracad](https://github.com/damballa/abracad) and see if these behaviors are acceptable to you first.



Great fit with [Franzy](https://github.com/ymilky/franzy), a Clojure Kafka client, though not required.

## Why

* You want your Kafka storage format as Avro.
* You are sending data directly from Kafka to Hadoop (additional support by Abracad).
* You are using Avro as a consistent format in your architecture.
* You want to serialize things to/from Clojure, even from other JVM languages.
* You want painless serialization of JSON without losing type information or extra munging for most use-cases.
* You want the possibility of deserializing your data back into Clojure with relative ease.
* You are using a Kafka client such as [Franzy](https://github.com/ymilky/franzy) and need a plugable, robust serializer.
* You want to serialize Clojure data types with little effort.
* You want to compose with a message pack serializer/deserializer.
* You want seamless serialization, no embedded serialization calls at call sites or `.getBytes` ugly things floating around.

## Docs

* Read the browsable [API](http://ymilky.github.io/franzy-avro/) and the tests.
* See [Franzy Source](https://github.com/ymilky/franzy) and docs for more information about serializers/deserializers.
* For more information about working with Clojure and Avro, see [Abracad](https://github.com/damballa/abracad).

## Installation

Add the necessary dependency to your project:

```clojure
[clj-kafka.franzy/avro "0.0.1"]
```

[![Clojars Project](https://img.shields.io/clojars/v/clj-kafka.franzy/avro.svg)](https://clojars.org/clj-kafka.franzy/avro)

## Serializing

First, require:

```clojure
(ns my-ns
  (:require [franzy.serialization.avro.serializers :as serializers]))
```

Then use with a producer, such as the one with [Franzy](https://github.com/ymilky/franzy).

```clojure
  (let [;;optionally specify via Kakfa Config key - value.serializer using fully qualified class name
        pc {:bootstrap.servers ["127.0.0.1"]}
        
        ;;ensure you define a schema, if not, a basic edn schema is the default
        ;;you must parse your schema so avro understands it
        schema (avro/parse-schema
                      {:name "example", :type "record",
                       :fields [{:name "left", :type "string"}
                                {:name "right", :type "long"}]
                       :abracad.reader "vector"})]
                               
        ;;Serializes producer record keys, ex: (keyword-serializer) from Franzy
        key-serializer (your-key-serializer-type)

        ;;avro serializer - serializes to binary
        value-serializer (serializers/avro-serializer schema)]
    (with-open [p (producer/make-producer pc key-serializer value-serializer)]
      ;;assault Kafka with data no one will ever see
      (send-async! "stolen-leftovers" 0 "lomein" "["3 days old, still stolen." 1234])))
```

**You may not mix and match serializers and deserializers.**

## Deserializing

First, require:

```clojure
(ns my-ns
  (:require [franzy.serialization.avro.deserializers :as deserializers]))
```

Then use with a consumer, such as the one with [Franzy](https://github.com/ymilky/franzy).

```clojure
  (let [;;optionally specify via Kafka Config key - value.deserializer using fully qualified class name
        cc {:bootstrap.servers ["127.0.0.1:9092"]
            :group.id          "hungry-hippos"}
        
        ;;ensure you use a consistent schema with the deserializer, i.e. same as before
        ;;basic edn schema is again the default if no schema is provided.
        schema (avro/parse-schema
                      {:name "example", :type "record",
                       :fields [{:name "left", :type "string"}
                                {:name "right", :type "long"}]
                       :abracad.reader "vector"})]
                       
        ;;Deserializes your record keys, ex: (keyword-deserializer) from Franzy
        key-deserializer (your-key-deserializer-type)
        
        ;;ensure you're using the same format when deserializing
        value-deserializer (deserializers/avro-deserializer schema)
        topic "stolen-leftovers"
        topic-partitions [{:topic topic :partition 0}]]
    (with-open [c (consumer/make-consumer cc key-deserializer value-deserializer)]
      (assign-partitions! c topic-partitions)
      (seek-to-beginning-offset! c topic-partitions)
      (poll! c)))
```

## TODO

* If there is a demand for custom bindings for schemas, to support richer types, please let me know.

**You may not mix and match serializers and deserializers.**

## License

Copyright © 2016 Yossi M. (ymilky).

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.

Use at your own risk, I am not responsible or liable. Please give credit if you use pieces of this library or otherwise, it is much appreciated.
