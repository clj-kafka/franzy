(ns franzy.common.configuration.codec
  "Codec and supporting functionality for managing Kafka property-based configuration."
  (:import (java.util Properties Map)
           (clojure.lang Keyword IPersistentCollection IPersistentMap)))

(declare decode-xf)

(defprotocol KafkaConfigCodec
  "A codec with specialized handling for Kafka configurations, typically property-based.

  The following functions are currently supported to help you write simpler configurations when encoding a map to properties:

  * Keyword keys are converted to strings
  * Keyword values are converted to strings.
  * Persistent collections by default will be joined on a comma and can be used for multi-values, such as a server list.
  * Persistent maps are converted to properties. This allows both records and maps to be used as configs for Kafka.

  The reason why we are not doing something more 'Clojure' is as follows:

  * Mainline Kafka configurations are constantly changing, and adding yet another new slightly differently named/located key is cumbersome on users
  * There's not much repetition to justify nesting these keys, other than perhaps the security protocol-related config keys
  * It complicates serialization/deserialization needlessly
  * Keeping keys as keyword values allows Kafka configs to be Clojure enough
  * It is brain-dead easy to import existing Kafka configs this way, which is a real-world issue for many users"
  (encode [v])
  (decode [v]))

(defn map->properties
  "Converts a map to a Java Properties object.

  Notes:

  * Sequential collections will be joined as comma-delimited strings, ex: [1 2 3 4] -> \"1, 2, 3, 4\".
  * Anything else will be converted to a string.
  * Does not use the codec, and is here for the simple people that asked for a simple conversion function. I simply disapprove, but here it is."
  ^Properties [^Map m]
  (let [properties (Properties.)]
    (doseq [[k v] m] (.setProperty properties (name k)
                                   (if (coll? v)
                                     (clojure.string/join "," v)
                                     (str v))))
    properties))

(defn properties->map
  "Converts a Java Properties object to a map.

  Notes:

  * Converts all Java strings to keywords.
  * Doesn't handle string delimited values (by design for now) - convert to Clojure persistent vectors (yet?)
  * Does not use the codec, and is here for the simple people that asked for a simple conversion function. I simply disapprove, but here it is."
  [^Properties properties]
  (->> properties
       (reduce (fn [m [k v]] (assoc! m (keyword k) v)) (transient {}))
       (persistent!)))

(extend-protocol KafkaConfigCodec
  IPersistentMap
  (encode [m]
    (let [properties (Properties.)]
      (doseq [[k v] m]
        (.setProperty properties (encode k) (encode v)))
      properties))
  (decode [m] m)

  Keyword
  (encode [kw]
    (name kw))
  (decode [kw] kw)

  IPersistentCollection
  (encode [coll]
    (clojure.string/join "," coll))
  (decode [coll]
    (into [] decode-xf coll))

  Properties
  (encode [properties] properties)
  (decode [properties]
    (->> properties
         (reduce (fn [m [k v]] (assoc! m (keyword k) (encode v))) (transient {}))
         (persistent!)))

  ;;TODO: handling delimited strings on decode? Could be an issue with false positives, not to mention lots of string checks, however this codec shouldn't be called much anyway and will be for small maps

  nil
  (encode [v] v)
  (decode [v] v)

  Object
  (encode [v] (str v))
  (decode [v] (str v)))

(def decode-xf
  "Transducer, applied on decode of collections that may be overriden using alter-var-root for example."
  (map decode))
