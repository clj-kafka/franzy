(ns franzy.admin.zookeeper.types
  "Types for working with Zookeeper")

(defrecord ZkClientConfig
  [servers connection-timeout session-timeout operation-retry-timeout serializer])

(defrecord ACL
  [id permissions])

(defrecord Stat
  ;;help, I'm covered in bytes
  [czx-id mzx-id c-time m-time version a-version c-version ephemeral-owner data-length num-children pzx-id])
