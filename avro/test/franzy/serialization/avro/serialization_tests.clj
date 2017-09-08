(ns franzy.serialization.avro.serialization-tests
  (:require [midje.sweet :refer :all]
            [franzy.serialization.avro.serializers :as serializers]
            [franzy.serialization.avro.deserializers :as deserializers]
            [abracad.avro :as avro]))

(fact
  "Avro can serialize EDN"
  (let [serializer (serializers/avro-serializer)
        deserializer (deserializers/avro-deserializer)
        data {:chicken "sometimes" :good-with-rice true :orders 57}]
    (->> data
         (.serialize serializer "leftovers")
         (.deserialize deserializer "leftovers")) => data))

(fact
  "Avro can serialize a custom schema."
  (let [schema (avro/parse-schema
              {:name "example", :type "record",
               :fields [{:name "left", :type "string"}
                        {:name "right", :type "long"}]
               :abracad.reader "vector"})
        serializer (serializers/avro-serializer schema)
        deserializer (deserializers/avro-deserializer schema)
        data  ["foo" 31337]]
    (->> data
         (.serialize serializer "leftovers")
         (.deserialize deserializer "leftovers")) => data))