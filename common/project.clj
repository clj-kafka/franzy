(defproject clj-kafka.franzy/common "0.0.0"
  :description "Common resources for Kafka libraries, such as schemas, utility functions, and configuration."
  :monolith/inherit true

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [prismatic/schema "1.1.0"]]
  :plugins [[lein-monolith "1.0.1"]
            [com.roomkey/lein-v "6.1.0-cb-9-0x521a"]
            ]
  :profiles {:dev              {:dependencies [[midje "1.7.0"]
                                               [jarohen/nomad "0.7.2"]]
                                :plugins      [[lein-midje "3.2"]
                                               [lein-set-version "0.4.1"]
                                               [lein-update-dependency "0.1.2"]
                                               [lein-pprint "1.1.1"]]}
             :reflection-check {:global-vars
                                {*warn-on-reflection* true
                                 *assert*             false
                                 *unchecked-math*     :warn-on-boxed}}})
