(defproject clj-kafka.franzy/core "0.0.0"
  :description "Clojure Kafka client with support for Kafka producer, consumer, rebalancing, administration, and validation."
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [prismatic/schema "1.0.5"]
                 [org.apache.kafka/kafka-clients "0.10.0.1"]
                 [lbradstreet/franzy-common "0.0.2"]]
  :monolith/inherit true
  :middleware [leiningen.v/dependency-version-from-scm]

  :plugins
  [[lein-monolith "1.0.1"]
   [com.roomkey/lein-v "6.1.0-cb-9-0x521a"]
   [lein-codox "0.9.4"]]
  :codox {:metadata    {:doc/format :markdown}
          :doc-paths   ["../README.md"]
          :output-path "doc/api"}
  :profiles {:dev
             {:dependencies [[midje "1.7.0"]
                             [com.taoensso/timbre "4.3.1"]]
              :plugins      [[lein-midje "3.2"]
                             [lein-set-version "0.4.1"]
                             [lein-update-dependency "0.1.2"]
                             [lein-pprint "1.1.1"]]}
             :reflection-check {:global-vars {*warn-on-reflection* true
                                              *assert*             false
                                              *unchecked-math*     :warn-on-boxed}}})
