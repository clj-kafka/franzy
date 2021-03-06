(defproject clj-kafka.franzy/avro "0.0.0"
  :description "A Kafka Serializer/Deserializer supporting Avro, and an add-on for Franzy, a Clojure Kafka suite of libraries."
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.apache.kafka/kafka-clients "0.11.0.0"]
                 [com.damballa/abracad "0.4.13"]]
  :monolith/inherit true
  :middleware [leiningen.v/dependency-version-from-scm
               leiningen.v/version-from-scm
               leiningen.v/add-workspace-data]


  :plugins
  [[lein-monolith "1.0.1"]
   [com.roomkey/lein-v "6.2.0"]])
