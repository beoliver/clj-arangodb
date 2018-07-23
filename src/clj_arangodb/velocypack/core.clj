(ns clj-arangodb.velocypack.core
  ""
  (:require [clj-arangodb.velocypack.utils :as utils])
  (:import com.arangodb.velocypack.VPackSlice
           com.arangodb.velocypack.ValueType
           com.arangodb.velocypack.VPackBuilder))

;;; NOTE - keys are always converted to Strings.

(declare build-array)
(declare build-map)

(defn pack
  "Pack a VPackSlice

  Takes an optional `key-fn`.
  By default keywords are converted into their `name` strings"
  ([o] (pack o utils/key->str))
  ([o key-fn]
   (as-> (new VPackBuilder) $
     (cond (map? o) (-> $
                        (.add ValueType/OBJECT)
                        (build-map o key-fn)
                        .slice)
           (sequential? o) (-> $
                               (.add ValueType/ARRAY)
                               (build-array o key-fn)
                               .slice)
           :else (-> $
                     (.add ValueType/ARRAY)
                     (.add o)
                     .close
                     .slice
                     (.get 0))))))

(defn- build-array [builder o key-fn]
  (-> (reduce (fn [b x]
                (cond (map? x) (build-map (.add b ValueType/OBJECT) x key-fn)
                      (sequential? x) (build-array (.add b ValueType/ARRAY) x key-fn)
                      :else (.add b x))) builder o)
      .close))

(defn- build-map [builder o key-fn]
  (-> (reduce
       (fn [b [k v]]
         (let [k (key-fn k)]
           (cond
             (map? v) (build-map (.add b k ValueType/OBJECT) v key-fn)
             (sequential? v) (build-array (.add b k ValueType/ARRAY) v key-fn)
             :else (.add b k v)))) builder o)
      .close))

(defn unpack
  "Unpack a VPackSlice

  Takes an optional `key-fn`.
  By default integer and float keys are converted
  all others are returned as keywords"
  ([^VPackSlice slice] (unpack slice utils/str->key))
  ([^VPackSlice slice key-fn]
   (cond
     (.isNull slice)    nil
     (.isString slice)  (.getAsString slice)
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
     (.isType slice) slice
     (.isBCD slice) slice
     (.isNone slice) nil
     (.isIllegal slice) nil
     :else slice)))
