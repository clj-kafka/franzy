;;; 
;;; ######################################################################
;;; ## check, test and install
;;; 
;;;     lein monolith each do clean, check, midje, install
;;;
;;;
;;; ## generate documentation
;;; 
;;;     lein monolith each do install, codox
;;;
;;;
;;; ## release using a parameter to "lein v update"
;;;
;;;   lein release minor
;;;   lein release major
;;;
;;; ######################################################################


(defproject clj-kafka.franzy/all "0.0.0"
  :description "A set of Clojure libraries to work with Apache Kafka (producer, consumer, rebalancing, administration, and validation)."

  :vcs :git
  :deploy-repositories [["releases" :clojars]]

   :plugins [;; essential for the project structure, as we depend on inheritance of project.clj entries
   [lein-monolith "1.0.1"]
   [chrisbetz/lein-v "6.2.0"] ;; replace with appropriate version of com.roomkey/lein-v when @cch1 accepts PR roomkey/lein-v#10 and creates new version on clojars.

   ;; test and documentation
   [lein-midje "3.2"]
   [lein-codox "0.9.4"]

	;; little helpers
   [lein-cljfmt "0.5.7"]
   [lein-cprint "1.2.0"]]

  :middleware [leiningen.v/version-from-scm
               leiningen.v/add-workspace-data]

  :dependencies
  [[org.clojure/clojure "1.8.0"]]

  :codox {:metadata    {:doc/format :markdown}
          :doc-paths   ["README.md"]
          :output-path "doc/api"} :test-selectors
  {:unit        (complement :integration)
   :integration :integration}

  :profiles {:install-for-with-all-repl {:middleware ^:replace []}

             :dev              {:dependencies [[midje "1.7.0"]
                                               [com.gfredericks/debug-repl "0.0.9"]]}

             :reflection-check {:global-vars
                                {*warn-on-reflection* true
                                 *assert*             false
                                 *unchecked-math*     :warn-on-boxed}}}

:repl-options
  {:nrepl-middleware
    [com.gfredericks.debug-repl/wrap-debug-repl]}

  :monolith
  {:inherit [:test-selectors
             :env
             :plugins
             :profiles
             :middleware
             :codox
             :repl-options]

   :inherit-leaky
   [:dependencies
    :repositories
    :deploy-repositories
    :managed-dependencies]

   :project-selectors
   {:deployable :deployable
    :unstable   #(= (first (:version %)) \0)}

   :project-dirs
   [; "admin"
    "avro"
    "common"
    "core"
    ; "embedded"
    ; "examples"
    "fressian"
    "json"
    ;"mocks"
    ;"nippy"
    "transit"]}

  :release-tasks [["vcs" "assert-committed"]
                  ["v" "update"]                            ;; compute new version & tag it
                  ["vcs" "push"]
                  ["monolith" "each" "deploy"]])