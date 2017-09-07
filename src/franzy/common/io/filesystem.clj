(ns franzy.common.io.filesystem
  (:require [clojure.java.io :refer [file]])
  (:import (java.io File)))

(defn make-temp
  [& parts]
  (apply file (System/getProperty "java.io.tmpdir") parts))

(defn make-temp-path
  [& parts]
  (->> parts
       ^File (apply make-temp)
       (.getPath)))

(defn make-temp-absolute-path [& parts]
  (->>
    parts
    (apply make-temp)
    (.getAbsolutePath)))
