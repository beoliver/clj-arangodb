(ns clj-arangodb.velocypack.core
  (:require [clj-arangodb.velocypack.utils :as utils])
  (:import [com.arangodb.velocypack
            VPackSlice
            ValueType
            VPackBuilder]))

;;; NOTE - keys are always converted to Strings.

(declare build-array)
(declare build-map)

(defn pack ^VPackSlice
  ([^Object o] (pack o utils/key->str))
  ([^Object o key-fn]
   (let [^VPackBuilder builder (new VPackBuilder)]
     (.slice
      (cond (map? o)
            ^VPackBuilder (build-map (.add builder ^ValueType ValueType/OBJECT) o key-fn)
            (sequential? o)
            ^VPackBuilder (build-array (.add builder ^ValueType ValueType/ARRAY) o key-fn)
            :else (.add builder o))))))

(defn- build-array [^VPackBuilder builder xs key-fn]
  (.close ^VPackBuilder
          (reduce (fn [^VPackBuilder b ^Object x]
                    (cond (map? x)
                          (build-map (.add ^VPackBuilder b
                                           ^ValueType ValueType/OBJECT) x key-fn)
                          (sequential? x)
                          (build-array (.add ^VPackBuilder b
                                             ^ValueType ValueType/ARRAY) x key-fn)
                          :else (.add ^VPackBuilder b
                                      ^Object x))) builder xs)))

(defn- build-map [^VPackBuilder builder o key-fn]
  (.close ^VPackBuilder
          (reduce
           (fn [^VPackBuilder b [k v]]
             (let [^String s (key-fn k)]
               (cond
                 (map? v) (build-map (.add ^VPackBuilder b
                                           s ^ValueType ValueType/OBJECT) v key-fn)
                 (sequential? v)
                 (build-array (.add ^VPackBuilder b
                                    s ^ValueType ValueType/ARRAY) v key-fn)
                 :else (.add ^VPackBuilder b s v)))) builder o)))

(defn unpack
  "Unpack a VPackSlice
  Takes an optional `key-fn`.
  By default integer and float keys are converted
  all others are returned as keywords"
  ([^VPackSlice slice] (unpack slice utils/str->key))
  ([^VPackSlice slice key-fn]
   (cond
     (.isNull slice) nil
     (.isString slice) (.getAsString slice)
     (.isInteger slice) (.getAsLong slice)
     (.isBoolean slice) (.getAsBoolean slice)
     (.isObject slice)
     (let [j (dec (.getLength slice))]
       (loop [acc (transient {})
              i 0]
         (if (> i j)
           (persistent! acc)
           (recur
            (assoc! acc
                    (key-fn (.getAsString (.keyAt slice i)))
                    (unpack (.valueAt slice i) key-fn))
            (inc i)))))
     (.isArray slice)
     (let [j (dec (.getLength slice))]
       (loop [acc (transient [])
              i 0]
         (if (> i j)
           (persistent! acc)
           (recur
            (conj! acc (unpack (.get slice i) key-fn))
            (inc i)))))
     (.isDouble slice) (.getAsDouble slice)
     (.isDate slice) (.getAsDate slice)
     (.isMaxKey slice) (.getAsLong slice)
     (.isMinKey slice) (.getAsLong slice)
     (.isBinary slice) (.getAsBinary slice)
     (.isInt slice) (.getAsInt slice)
     (.isNumber slice) (.getAsNumber slice)
     (.isSmallInt slice) (.getAsInt slice)
     (.isUInt slice) (.getAsInt slice)
     (.isCustom slice) slice
     (.isExternal slice) slice
     (.isBCD slice) slice
     (.isNone slice) nil
     (.isIllegal slice) nil
     :else slice)))
