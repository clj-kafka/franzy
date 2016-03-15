(ns franzy.examples.bootstrap
  (:require [franzy.examples.configuration :as config]
            [franzy.admin.topics :as topics]
            [taoensso.timbre :as timbre]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Bootstrap Script
;;
;; We need a few things for all these examples to work/make sense. Namely, some topics that we can feel free to do some
;; fun stuff with and not wreck your existing data.
;;
;; 1. Go to configuration.clj and set values that make sense for you, i.e. servers, and if you want, topic names
;; 2. Run (start) in your REPL to get started by creating some topics in Kafka.
;;
;;
;;
;;
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;;we need zk-utils to create some things in Zookeeper
(defn bootstrap-topics []
  (with-open [zk-utils (config/make-zk-utils)]
    (let [topics (config/topics-list)]
      (doseq [topic topics]
        (when-not (topics/topic-exists? zk-utils topic)
          ;;create some topics, if they don't exist
          (timbre/info "Creating topic named" topic ":::::::::::::::::")
          (topics/create-topic! zk-utils topic config/default-partition-count)))
      (timbre/info "Returning a list of topics to you from your cluster...")
      (topics/all-topics zk-utils))))

(defn verify-topics []
  (with-open [zk-utils (config/make-zk-utils)]
    (let [topics (config/topics-list)]
      (doseq [topic topics]
        (when-not (topics/topic-exists? zk-utils topic)
          ;;oops, something isn't going to work maybe ....
          (timbre/error "Could not find topic:" topic))))
    (timbre/info "Verification complete. Fix any errors...if you can. Get a snack, then do some examples with your cookie hands.")
    (timbre/info "Returning a list of topics to you from your cluster...")
    (topics/all-topics zk-utils)))

;;if you want to "cleanse" your Kafka, this could help, but deletes might not be instant - this marks them for deletion
;;if they appear in the returned list, don't be alarmed
(defn cleanup-topics []
  (with-open [zk-utils (config/make-zk-utils)]
    (let [topics (config/topics-list)]
      (doseq [topic topics]
        (when (topics/topic-exists? zk-utils topic)
          ;;delete any of our example topics if they exist
          ;;note, it's not recommended to re-run things again and again with the same topics, so prefer to change them
          (topics/delete-topic! zk-utils topic)
          (timbre/info "Returning a list of topics to you from your cluster...")
          (topics/all-topics zk-utils))))))
