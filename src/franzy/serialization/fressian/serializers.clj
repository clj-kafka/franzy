(ns franzy.serialization.fressian.serializers
  (:require [clojure.data.fressian :as fressian])
  (:import (org.apache.kafka.common.serialization Serializer)
           (org.fressian Writer)
           (org.fressian.impl BytesOutputStream)))

(deftype FressianSerializer [opts]
  Serializer
  (configure [_ _ _])
  (serialize [_ _ data]
    ;;adapted from the mouth of the dragon
    ;;we do this to skip any intermediate streams, since Kafka can't make use of ByteStreams without some encoder work
    (with-open [bos (BytesOutputStream.)]
      (let [{:keys [footer?]} (when opts (apply hash-map opts))
            writer ^Writer (apply fressian/create-writer bos opts)]
        (.writeObject writer data)
        (when footer?
          (.writeFooter writer))
        (.internalBuffer bos))))
  (close [_]))

(defn fressian-serializer
  "Fressian serializer for Apache Kafka.
  Use for serializing Kafka values.

  > Notes: You may pass any of the built-in Fressian options via the opts map, using
   the 1-arity version of this function."
  (^FressianSerializer [] (fressian-serializer nil))
  (^FressianSerializer [opts]
   (FressianSerializer. opts)))
