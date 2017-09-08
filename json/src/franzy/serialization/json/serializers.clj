(ns franzy.serialization.json.serializers
  (:require [cheshire.core :as json])
  (:import (org.apache.kafka.common.serialization Serializer)
           (java.io ByteArrayOutputStream)))

(deftype JsonSerializer [opts]
  Serializer
  (configure [_ _ _])
  (serialize [_ _ data]
    (when data
      (with-open [baos (ByteArrayOutputStream.)
                  writer (clojure.java.io/writer baos)]
        (json/generate-stream data writer opts)
        (.toByteArray baos))))
  (close [_]))

(defn json-serializer
  "JSON serializer for Apache Kafka.
  Use for serializing Kafka keys values.

  > Notes: You may pass any of the built-in Chesire options for generate-stream via the opts map, using
   the 1-arity version of this function."
  (^JsonSerializer [] (json-serializer nil))
  (^JsonSerializer [opts]
   (JsonSerializer. opts)))

(deftype SmileSerializer [opts]
  ;;smile, you could always be serializing faster
  Serializer
  (configure [_ _ _])
  (serialize [_ _ data]
    (when data
      (json/generate-smile data opts)))
  (close [_]))

(defn smile-serializer
  "JSON serializer for Apache Kafka using the Smile binary format.
  Use for serializing Kafka keys values.

  http://wiki.fasterxml.com/SmileFormat

  > Notes: You may pass any of the built-in Chesire options for generate-stream via the opts map, using
  the 1-arity version of this function."
  (^SmileSerializer [] (smile-serializer nil))
  (^SmileSerializer [opts]
   (SmileSerializer. opts)))
