# Franzy-JSON

[Kafka](http://kafka.apache.org/documentation.html) JSON serializer using the most excellent [Cheshire](https://github.com/dakrone/cheshire) library.

Optional SMILE encoding support, data munging, and more courtesy of [Cheshire](https://github.com/dakrone/cheshire).

Great fit with [Franzy](https://github.com/ymilky/franzy), a Clojure Kafka client, though not required. Feel free to use this serializer with any Kafka client, including via Java, Scala, Groovy, or any other JVM language.

## Why

* You want your Kafka storage format as JSON, perhaps using the (SMILE)[http://wiki.fasterxml.com/SmileFormat] binary data format.
* You need to return data to things that can decode a binary stream of json, such as non-JVM languages or other apps.
* You want to serialize things to/from Clojure, even from other JVM languages.
* You want the possibility of deserializing your data back into Clojure, and control munging to/from JSON where possible.
* You are using a Kafka client such as [Franzy](https://github.com/ymilky/franzy) and need a plugable, robust serializer.
* You want to serialize Clojure data types with little effort.
* You want seamless serialization, no embedded serialization calls at call sites or `.getBytes` ugly things floating around.

## Docs

* Read the browsable [API](http://ymilky.github.io/franzy-json/api/index.html)
* See [Franzy Source](https://github.com/ymilky/franzy) and docs for more information about serializers/deserializers.
* For more information about serializer options, compression, etc, see the official [Chesire](https://github.com/dakrone/cheshire) docs.

## Installation

Add the necessary dependency to your project:

```clojure
[clj-kafka.franzy/json "0.0.1"]
```

## Serializing

First, require:

```clojure
(ns my-ns
  (:require [franzy.serialization.json.serializers :as serializers]))
```

Then use with a producer, such as the one with [Franzy](https://github.com/ymilky/franzy).

```clojure
  (let [;;optionally specify via Kakfa Config key - value.serializer using fully qualified class name
        pc {:bootstrap.servers ["127.0.0.1"]}
        ;;Serializes producer record keys, ex: (keyword-serializer) from Franzy
        key-serializer (your-key-serializer-type)
        ;;Serializes producer record values using Json
        ;;Call (serializers/json-serializer) without options too!
        ;;Likewise, you can call (serializers/smile-serializer options)
        ;;for a smile-based serializer, because...teeth.
        serializer-opts {:key-fn true} ;;serialize keywords, if you like....
        value-serializer (serializers/json-serializer serializer-opts)]
    (with-open [p (producer/make-producer pc key-serializer value-serializer)]
      ;;spray useless data to Kafka using Clojure types
      (send-async! "front-end-developerisms" 0
      {:quotes ["Efficient JSON database" "What is Big O?" "Bro...Bro..."]
       :hobbies "writing js on the server, without a linter"
       :wise false}))))
```

Note that you may choose to serialize using Smile, but only do so if you also will deserialize using smile.

You can use `smile-serializer` for this purpose.

**You may not mix and match serializers and deserializers.**

## Deserializing

First, require:

```clojure
(ns my-ns
  (:require [franzy.serialization.json.deserializers :as deserializers]))
```

Then use with a consumer, such as the one with [Franzy](https://github.com/ymilky/franzy).

```clojure
  (let [;;optionally specify via Kafka Config key - value.deserializer using fully qualified class name
        cc {:bootstrap.servers ["127.0.0.1:9092"]
            :group.id          "broheems"}
        ;;Deserializes your record keys, ex: (keyword-deserializer) from Franzy
        key-deserializer (your-key-deserializer-type)
        ;;the value deserializer should be the same kind as the serializer, don't mix and match
        ;;call (serializers/json-deserialize) if you don't need options...
        serializer-opts {:key-fn true}
        value-deserializer (deserializers/json-deserializer serializer-opts)
        topic "front-end-developerisms"
        topic-partitions [{:topic topic :partition 0}]]
    (with-open [c (consumer/make-consumer cc key-deserializer value-deserializer)]
      (assign-partitions! c topic-partitions)
      (seek-to-beginning-offset! c topic-partitions)
      (poll! c)))
```

Note that you may choose to deserialize using Smile, but only do so if you also serialized using Smile.

You can use `smile-deserializer` for this purpose.

**You may not mix and match serializers and deserializers.**

## License

Copyright © 2016 Yossi M. (ymilky).

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.

Use at your own risk, I am not responsible or liable. Please give credit if you use pieces of this library or otherwise, it is much appreciated.
