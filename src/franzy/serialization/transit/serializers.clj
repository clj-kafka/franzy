(ns franzy.serialization.transit.serializers
  (:require [cognitect.transit :as transit])
  (:import (org.apache.kafka.common.serialization Serializer)
           (java.io ByteArrayOutputStream)))

(deftype TransitSerializer [format opts]
  Serializer
  (configure [_ _ _])
  (serialize [_ _ data]
    (when data
      (let [baos (ByteArrayOutputStream.)]
        (-> (transit/writer baos format opts)
            (transit/write data))
        (.toByteArray baos))))
  (close [_]))

(defn transit-serializer
  "Transit serializer for Apache Kafka. Use for serializing Kafka keys values.

  You should pass a format keyword to speciy which Transit-supported format you want as your output.
  Valid formats per Transit are :json, :json-verbose, and :msgpack.

  The default format will be :json (JSON) if no format is passed.

  > Note: If you use msgpack format, is your responsibility to encode the input accordingly for Transit.
  This project does not introduce any msgpack dependencies. This can be wrapped by composing with a msgpack serialzer.

  > Notes: You may pass any of the built-in Chesire options for generate-stream via the opts map, using
   the 1-arity version of this function. The opts maps in Transit currently only supports :handlers as valid options.

   See https://github.com/cognitect/transit-clj"
  (^TransitSerializer [] (transit-serializer nil nil))
  (^TransitSerializer [format] (transit-serializer format nil))
  (^TransitSerializer [format opts]
   (TransitSerializer. (or format :json) opts)))
