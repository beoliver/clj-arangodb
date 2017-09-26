(ns clj-arangodb.velocypack.core
  (:refer-clojure :exclude [get get-in])
  (:require [clj-arangodb.velocypack.utils :as utils])
  (:import com.arangodb.velocypack.VPackSlice
           com.arangodb.velocypack.ValueType
           com.arangodb.velocypack.VPack
           com.arangodb.velocypack.VPackBuilder))

(defn get-type [slice]
  (.getType slice))

(defn get
  "Returns the `VPackSlice` mapped to key, not-found or nil if key not present."
  ([slice k] (get slice k nil))
  ([slice k not-found]
   (let [val (.get slice (utils/normalize k))]
     (if (.isNone val) not-found val))))

(defn get-in
  "Returns the `VPackSlice` in a nested `VPackSlice` structure,
  where ks is a sequence of keys. Returns nil if the key
  is not present, or the not-found value if supplied."
  ([slice ks] (get-in slice ks nil))
  ([slice ks not-found]
   (let [inner-slice (reduce (fn [slice k]
                               (.get slice (utils/normalize k))) slice ks)]
     (if (.isNone inner-slice) not-found inner-slice))))

(defn read-as [x keyword]
  (when-not (or (.isNull x) (.isNone x))
    (try
      (case keyword
        :string (.getAsString x)
        :bool (.getAsBoolean x)
        :number (.getAsNumber x)
        :date (.getAsDate x)
        :int (.getAsInt x)
        :float (.getAsFloat x)
        :long (.getAsLong x)
        :char (.getAsChar x)
        :double (.getAsDouble x)
        :byte (.getAsByte x)
        (.getAsString x))
      (catch Exception _ (.toString x)))))

;; (def schema-1 {:a :number
;;                :b {:c :bool :d :string}})

;; (defn unpack [schema slice])


;; (-> (pack {:a 209.34 :b {:c true :d "ok"}})
;;     (get-in [:b :c])
;;     (read-as :bool))

(defn new-vpack-builder []
  (new VPackBuilder))

(declare build-map)
(declare build-array)

(defn pack-one [x]
  (-> (new VPackBuilder)
      (.add ValueType/ARRAY)
      (.add x)
      .close
      .slice
      (.get 0)))

(defn pack [xs]
  (cond (map? xs)
        (.slice (build-map (-> (new VPackBuilder) (.add ValueType/OBJECT)) xs))
        ((some-fn string? number? nil?) xs) (pack-one xs)
        (utils/seqable? xs)
        (.slice (build-array (-> (new VPackBuilder) (.add ValueType/ARRAY)) xs))
        :else (pack-one xs)))

(defn build-array [builder seq]
  (-> (reduce (fn [builder elem]
                (cond (map? elem) (-> builder
                                      (.add ValueType/OBJECT)
                                      (build-map elem))
                      ;; as a string is seqable we test for it early on.
                      ;; otherwise we store a list of characters!
                      (string? elem) (.add builder elem)
                      (nil? elem) (.add builder elem)
                      ;; (number? elem) (.add builder elem)
                      (utils/seqable? elem) (-> builder
                                                (.add ValueType/ARRAY)
                                                (build-array elem))
                      :else (.add builder elem))) builder seq)
      .close))

(defn build-map
  [builder m]
  (as-> builder $
    (reduce (fn [builder [k v]]
              (cond (map? v) (-> builder
                                 (.add (utils/normalize k) ValueType/OBJECT)
                                 (build-map v))
                    (string? v) (.add builder (utils/normalize k) v)
                    (nil? v) (.add builder (utils/normalize k) nil)
                    (number? v) (.add builder (utils/normalize k) v)
                    (utils/seqable? v) (-> builder
                                           (.add (utils/normalize k) ValueType/ARRAY)
                                           (build-array (seq v)))
                    :else (.add builder (utils/normalize k) v)))
            $ m)
    (.close $)))
