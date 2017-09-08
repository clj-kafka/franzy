(ns franzy.admin.core-test
  (:require [midje.sweet :refer :all]
            [nomad :refer [defconfig]]
            [clojure.java.io :as io]
            [franzy.admin.zookeeper.defaults :as zk-defaults]
            [franzy.admin.zookeeper.client :as client]))

;; This all needs to change, but for now ensure you have a file zookeeper.edn
;; in your classpath under config with something like this in it at a minimum:

;{:nomad/hosts
; {"giant-spoon.local"
;  {:kakfa/zookeeper-config
;   {:servers "127.0.0.1:2181"}}}}

(defconfig zk-config (io/resource "config/zookeeper.edn"))

(defn make-zk-config []
  (let [zk-conf (get-in (zk-config) [:kakfa/zookeeper-config])]
    (merge (zk-defaults/zk-client-defaults) zk-conf)))

(defn make-zk-client []
  (client/make-zk-client (make-zk-config)))

(defn make-zk-utils []
  (client/make-zk-utils (make-zk-config) false))



