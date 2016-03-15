(ns franzy.admin.zookeeper.codec)

(defn encode-server-list [servers]
  (if (coll? servers)
    (clojure.string/join "," servers)
    servers))
