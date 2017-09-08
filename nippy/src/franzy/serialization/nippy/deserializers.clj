(ns franzy.serialization.nippy.deserializers
  (:require [taoensso.nippy :as nippy])
  (:import (org.apache.kafka.common.serialization Deserializer)))

(deftype NippyDeserializer [opts]
  Deserializer
  (configure [_ _ _])
  (deserialize [_ _ data]
    (nippy/thaw data opts))
  (close [_]))

(defn nippy-deserializer
  "Nippy deserializer for Apache Kafka.
  Use for serializing Kafka values.

  > Notes: You may pass any of the built-in nippy options via the opts map, using
   the 1-arity version of this function."
  (^NippyDeserializer [] (nippy-deserializer nil))
  (^NippyDeserializer [opts]
   (NippyDeserializer. opts)))
