(ns franzy.serialization.json.deserializers
  (:require [cheshire.core :as json])
  (:import (org.apache.kafka.common.serialization Deserializer)
           (java.io ByteArrayInputStream)))

(deftype JsonDeserializer [opts]
  Deserializer
  (configure [_ _ _])
  (deserialize [_ _ data]
    (when data
      ;;possible to move this to store in type somewhere
      ;;the parse-stream function doesn't take its options the same way as generate-stream
      ;;we could call apply on it, but want to leave room for future options without breaking signatures
      (let [{:keys [key-fn array-coerce-fn]} opts]
        (with-open [bais (ByteArrayInputStream. data)
                    reader (clojure.java.io/reader bais)]
          (json/parse-stream reader key-fn array-coerce-fn)))))
  (close [_]))

(defn json-deserializer
  "JSON deserializer for Apache Kafka.
  Use for deserializing Kafka keys and values.

  > Notes: You may pass any of the built-in Cheshire options to parse-stream via the opts map, using
   the 1-arity version of this function."
  (^JsonDeserializer [] (json-deserializer nil))
  (^JsonDeserializer [opts]
   (JsonDeserializer. opts)))

(deftype SmileDeserializer [opts]
  Deserializer
  (configure [_ _ _])
  (deserialize [_ _ data]
    (when data
      (let [{:keys [key-fn array-coerce-fn]} opts]
        (json/parse-smile data key-fn array-coerce-fn))))
  (close [_]))

(defn smile-deserializer
  "JSON deserializer for Apache Kafka using Smile binary format.
  Use for deserializing Kafka keys and values.

  http://wiki.fasterxml.com/SmileFormat

  > Notes: You may pass any of the built-in Cheshire options to parse-smile via the opts map, using
   the 1-arity version of this function."
  (^SmileDeserializer [] (smile-deserializer nil))
  (^SmileDeserializer [opts]
   (SmileDeserializer. opts)))
