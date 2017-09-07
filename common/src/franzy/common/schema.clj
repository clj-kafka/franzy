(ns franzy.common.schema
  "Basic schemas for validating Kafka configs, types, functions, etc."
  (:require [schema.core :as s]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Basic validations - many of these can probably be replaced by things inside Schema...ongoing struggle
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def Function
  "Schema for a target that must be a function."
  (s/pred fn? 'fn?))

(def AnyButNil
  "Schema for a value that can be anything but nil."
  (s/pred some? 'not-nil?))

(def NonEmptyString
  "Schema for a string that cannot be blank."
  (s/constrained s/Str (complement clojure.string/blank?) 'blank?))

(defn gt-eq-zero? [x]
  (>= x 0))

(def GreaterThanOrEqualToZero
  "Schema for a number greater than or equal to zero."
  (s/pred gt-eq-zero? 'GreaterThanOrEqualToZero?))

(def Positive
  "Schema for a positive Number."
  (s/pred pos? 'pos?))

;;Following "Java" schemas below are for general interop.
;; Clojure will coerce to the right type, warn, throw, etc., however when dealing with interop, we don't want any fun surprises.
;; Moreover, we also would like a friendly message why, rather than from the compiler or further down in a stack trace.
;; Likewise, we want to ensure when a schema is checked that we know a value is out of range, especially for a particular config-related value.
(def CoercableInt
  "Schema for a value that is coercable without overflow to a Integer."
  (s/constrained s/Int #(<= Integer/MIN_VALUE % Integer/MAX_VALUE) 'coercable-int?))

(def CoercableLong
  "Schema for a value that is coercable without overflow to a Long."
  (s/constrained s/Int #(<= Long/MIN_VALUE % Long/MAX_VALUE) 'coercable-long?))

(def CoercableShort
  "Schema for a value that is coercable without overflow to a Short."
  (s/constrained s/Int #(<= Short/MIN_VALUE % Short/MAX_VALUE) 'coercable-short?))

(def CoercableDouble
  "Schema for a value that is coercable without overflow to a Double."
  s/Num)

(def NegativeDefaultNumber
  "Schema for defaults that assume -1 as a null/not set. This schema is mainly for composition with other Schemas."
  (s/pred (partial <= -1) 'snegativedefaultnumber?))

(def PosInt
  "Schema for positive integers."
  (s/constrained CoercableInt pos?))

(def SPosInt
  "Schema for positive, zero inclusive integers."
  (s/constrained CoercableInt gt-eq-zero?))

(def SPosIntWithDefault
  "Schema for positive, zero inclusive integers that can also have -1 as a default value."
  (s/either SPosInt NegativeDefaultNumber))

(def PosLong
  "Schema for positive longs."
  (s/constrained CoercableLong pos?))

(def SPosLong
  "Schema for positive, zero inclusive longs."
  (s/constrained CoercableLong gt-eq-zero?))

(def SPosLongWithDefault
  "Schema for positive, zero inclusive longs that can also have -1 as a default value."
  (s/either SPosLong NegativeDefaultNumber))

(def PosShort
  "Schema for positive shorts."
  (s/constrained CoercableShort pos?))

(def SPosShort
  "Schema for positive, zero inclusive shorts."
  (s/constrained CoercableLong gt-eq-zero?))

(def SPosShortWithDefault
  "Schema for positive, zero inclusive shorts that can have also have -1 as a default value."
  (s/either SPosShort NegativeDefaultNumber))

(def PosDouble
  "Schema for positive doubles."
  (s/constrained CoercableDouble pos?))

(def SPosDouble
  "Schema for positive, zero inclusive doubles."
  (s/constrained CoercableDouble gt-eq-zero?))

(def SPosDoubleWithDefault
  "Schema for positive, zero inclusive doubles that can have also have -1 as a default value."
  (s/either SPosDouble NegativeDefaultNumber))

(def NamespacedKeyword
  "Schema for keywords that must be namespaced, i.e. :my-namespace/my-keyword"
  (s/pred (fn [kw]
            (and (keyword? kw)
                 (namespace kw)))
          'keyword-namespaced?))

(def NotEmpty
  "Schema for collections that must not be empty."
  (s/pred (complement empty?) 'not-empty?))

(def StringOrList
  "Schema for a value that can be a string or a collection."
  (s/pred (fn [x] (cond
                    (string? x)
                    true
                    (sequential? x)
                    true)) 'string-or-list?))

(def NonEmptyStringOrList
  "Schema for a value that can be a string or a collection."
  (s/pred (fn [x] (cond
                    (string? x)
                    (if (clojure.string/blank? x) false true)
                    (sequential? x)
                    (if (empty? x) false true))) 'non-empty-string-or-list?))

(def StringOrStringList
  "Schema for a value that can be a string or string collection."
  (s/pred (fn [x] (cond
                    (string? x)
                    true
                    (sequential? x)
                    (every? string? x)))
          'string-or-string-list?))

(def NonEmptyStringOrStringList
  "Schema for a value that can be a non-empty string or string collection."
  (s/pred (fn [x] (cond
                    (string? x)
                    (if (clojure.string/blank? x) false true)
                    (sequential? x)
                    (if (empty? x) false (every? #(and (string? %) (not (clojure.string/blank? %))) x))))
          'non-empty-string-or-string-list?))
