(ns franzy.serialization.avro.deserializers
  (:require [abracad.avro :as avro]
            [abracad.avro.edn :as aedn])
  (:import (org.apache.kafka.common.serialization Deserializer)))

(deftype AvroDeserializer [schema]
  Deserializer
  (configure [_ _ _])
  (deserialize [_ _ data]
    (when data
      (avro/decode schema data)))
  (close [_]))

(defn avro-deserializer
  "Avro deserializer for Apache Kafka.
  Use for deserializing Kafka keys and values.

  If no schema is provided, a default EDN schema is assumed.

   See https://avro.apache.org/
   See https://github.com/damballa/abracad"
  (^AvroDeserializer [] (avro-deserializer nil))
  (^AvroDeserializer [schema]
   (AvroDeserializer. (or schema (aedn/new-schema)))))

