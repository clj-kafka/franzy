(ns franzy.common.configuration.property-configuration-tests
  (:require [midje.sweet :refer :all]
            [schema.core :as s]
            [franzy.common.schema :as fs]
            [franzy.common.configuration.codec :as codec])
  (:import (schema.utils NamedError)
           (java.util Properties)))

(facts
  "Clojure configuration maps should produce valid Kafka properties."
  (let [props (doto (Properties.)
                (.setProperty "bootstrap.servers" "192.168.1.2:9092,192.168.1.3:9092")
                (.setProperty "acks" "-1"))
        config-map {:bootstrap.servers ["192.168.1.2:9092" "192.168.1.3:9092"]
                    :acks              "-1"}]
    (fact
      "Clojure configurations produce Java properties."
      (instance? Properties (codec/encode config-map)) => true)
    (fact
      "Lists, vectors, and sets can work as delimited/multi-value property keys."
      (codec/encode ["10.0.0.2:9092" "10.0.0.3:9092"]) => "10.0.0.2:9092,10.0.0.3:9092"
      (codec/encode '("10.0.0.2:9092" "10.0.0.3:9092")) => "10.0.0.2:9092,10.0.0.3:9092"
      (codec/encode #{"10.0.0.2:9092" "10.0.0.3:9092"}) => "10.0.0.2:9092,10.0.0.3:9092")
    (fact
      "Keywords can be used as property keys."
      (codec/encode :bootstrap.servers) => "bootstrap.servers")
    (fact
      "Numeric values can be used as property values as raw Clojure types"
      ;;ex: max.bytes
      (codec/encode 1024) => "1024")
    (fact
      "Clojure maps should encode accurately to Kafka properties."
      (codec/encode config-map) => props)))
