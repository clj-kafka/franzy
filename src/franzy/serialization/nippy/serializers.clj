(ns franzy.serialization.nippy.serializers
  (:require [taoensso.nippy :as nippy])
  (:import (org.apache.kafka.common.serialization Serializer)))

(deftype NippySerializer [opts]
  Serializer
  (configure [_ _ _])
  (serialize [_ _ data]
    (nippy/freeze data opts))
  (close [_]))

(defn nippy-serializer
  "Nippy serializer for Apache Kafka.
  Use for serializing Kafka values.

  > Notes: You may pass any of the built-in nippy options via the opts map, using
   the 1-arity version of this function."
  (^NippySerializer [] (nippy-serializer nil))
  (^NippySerializer [opts]
   (NippySerializer. opts)))
