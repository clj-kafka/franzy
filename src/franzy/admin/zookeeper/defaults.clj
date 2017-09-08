(ns franzy.admin.zookeeper.defaults
  (:require [franzy.admin.serializers :as serializers]))

(defn zk-connection-defaults []
  {:session-timeout 30000                                   ;;from ZkConnection default
   })

(defn zk-client-defaults []
  {:connection-timeout      30000
   :session-timeout         30000                           ;;from ZkConnection default
   :operation-retry-timeout (long -1)                       ;;from ZkClient default
   :serializer              (serializers/zk-string-serializer)}) ;;from the divine
