(ns franzy.serialization.transit.deserializers
  (:require [cognitect.transit :as transit])
  (:import (org.apache.kafka.common.serialization Deserializer)
           (java.io ByteArrayInputStream)))

;(set! *warn-on-reflection* true)

(deftype TransitDeserializer [format opts]
  Deserializer
  (configure [_ _ _])
  (deserialize [_ _ data]
    (when data
      (-> (transit/reader (ByteArrayInputStream. data) format opts)
          (transit/read))))
  (close [_]))

(defn transit-deserializer
  "Transit deserializer for Apache Kafka.
  Use for deserializing Kafka keys and values.

  You should pass a format keyword to speciy which Transit-supported format you used when serializing.
  Valid formats per Transit are :json, :json-verbose, and :msgpack.
  The default format will be :json (JSON) if no format is passed.

  > Note: If you use msgpack format, is your responsibility to decode the returned msgpack accordingly.
  This project does not introduce any msgpack dependencies. This can be wrapped by composing with a msgpack deserialzer.

  > Note: You may pass any of the built-in Transit options to parse-stream via the opts map.
  :handlers and :default-handlers are supported options by Transit.

   See https://github.com/cognitect/transit-clj"
  (^TransitDeserializer [] (transit-deserializer nil nil))
  (^TransitDeserializer [format] (transit-deserializer format nil))
  (^TransitDeserializer [format opts]
   (TransitDeserializer. (or format :json) opts)))

