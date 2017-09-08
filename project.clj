(defproject clj-kafka.franzy/all "0.0.0"
  :description "A set of Clojure libraries to work with Apache Kafka (producer, consumer, rebalancing, administration, and validation)."

  :vcs :git
  :deploy-repositories [["releases" :clojars]] :plugins
  [[lein-monolith "1.0.1"]
   [lein-cprint "1.2.0"]
   [lein-cljfmt "0.5.7"]
   [com.roomkey/lein-v "6.1.0-cb-9-0x521a"]] :middleware [leiningen.v/version-from-scm
                                                          leiningen.v/add-workspace-data]

  :dependencies
  [[org.clojure/clojure "1.8.0"]]

  :test-selectors
  {:unit        (complement :integration)
   :integration :integration}

  :profiles {:install-for-with-all-repl {:middleware ^:replace []}}

  :monolith
  {:inherit [:test-selectors
             :env
             :plugins
             :profiles
             :middleware]

   :inherit-leaky
   [:dependencies
    :repositories
    :deploy-repositories
    :managed-dependencies]

   :project-selectors
   {:deployable :deployable
    :unstable   #(= (first (:version %)) \0)}

   :project-dirs
   ["admin"
    "avro"
    "common"
    "core"
    "embedded"
    "examples"
    "fressian"
    "json"
    "mocks"
    "nippy"
    "transit"]}

  :release-tasks [["vcs" "assert-committed"]
                  ["v" "update"]                            ;; compute new version & tag it
                  ["vcs" "push"]
                  ["monolith" "each" "deploy"]])

;; release using a parameter to "lein v update"
;;
;;   lein release minor
;;   lein release major
;;