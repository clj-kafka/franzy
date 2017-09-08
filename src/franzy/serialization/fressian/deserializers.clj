(ns franzy.serialization.fressian.deserializers
  (:require [clojure.data.fressian :as fressian])
  (:import (org.apache.kafka.common.serialization Deserializer)))

(deftype FressianDeserializer [opts]
  Deserializer
  (configure [_ _ _])
  (deserialize [_ _ data]
    (when data
      (with-open [bais (clojure.java.io/input-stream data)]
        (apply fressian/read bais opts))))
  (close [_]))

(defn fressian-deserializer
  "Fressian deserializer for Apache Kafka.
  Use for serializing Kafka values.

  > Notes: You may pass any of the built-in Fressian options via the opts map, using
   the 1-arity version of this function."
  (^FressianDeserializer [] (fressian-deserializer nil))
  (^FressianDeserializer [opts]
   (FressianDeserializer. opts)))
