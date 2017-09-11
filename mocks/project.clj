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
   [com.roomkey/lein-v "6.2.0"]
   ])
