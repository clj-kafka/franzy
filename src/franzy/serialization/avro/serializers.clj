(ns franzy.serialization.avro.serializers
  (:require [abracad.avro :as avro]
            [abracad.avro.edn :as aedn])
  (:import (org.apache.kafka.common.serialization Serializer)))

(deftype AvroSerializer [schema]
  Serializer
  (configure [_ _ _])
  (serialize [_ _ data]
    (when data
      (avro/binary-encoded schema data)))
  (close [_]))

(defn avro-serializer
  "Avro serializer for Apache Kafka. Use for serializing Kafka keys values.

  Values will be serialized according to the provided schema.
  If no schema is provided, a default EDN schema is assumed.

  See https://avro.apache.org/
  See https://github.com/damballa/abracad"
  (^AvroSerializer [] (avro-serializer nil))
  (^AvroSerializer [schema]
   (AvroSerializer. (or schema (aedn/new-schema)))))
