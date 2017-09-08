(defproject clj-kafka.franzy/fressian "0.0.0"
  :description "A Kafka Serializer/Deserializer supporting Fressian, and an add-on for Franzy, a Clojure Kafka client."
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.apache.kafka/kafka-clients "0.9.0.1"]
                 [org.clojure/data.fressian "0.2.1"]]
  :monolith/inherit true
  :middleware [leiningen.v/dependency-version-from-scm]

  :plugins
  [[lein-monolith "1.0.1"]
   [com.roomkey/lein-v "6.1.0-cb-9-0x521a"]])
