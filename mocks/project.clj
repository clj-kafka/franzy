(defproject clj-kafka.franzy/mocks "0.0.0"
  :description "Mock producer and consumer for Kafka 0.9+ for testing, dev, etc."
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.apache.kafka/kafka-clients "0.9.0.1"]
                 [clj-kafka.franzy/common nil]
                 [clj-kafka.franzy/core nil]]
  :monolith/inherit true
  :middleware [leiningen.v/dependency-version-from-scm]

  :plugins
  [[lein-monolith "1.0.1"]
   [com.roomkey/lein-v "6.1.0-cb-9-0x521a"]]
  :profiles {:dev              {:dependencies [[midje "1.7.0"]]
                                :plugins      [[lein-midje "3.2"]
                                               [lein-set-version "0.4.1"]
                                               [lein-update-dependency "0.1.2"]
                                               [lein-pprint "1.1.1"]]}
             :reflection-check {:global-vars
                                {*warn-on-reflection* true
                                 *assert*             false
                                 *unchecked-math*     :warn-on-boxed}}})
