(ns clj-arangodb.velocypack.core
  (:refer-clojure :exclude [get get-in])
  (:require [clj-arangodb.velocypack.utils :as utils])
  (:import com.arangodb.velocypack.VPackSlice
           com.arangodb.velocypack.ValueType
           com.arangodb.velocypack.VPack
           com.arangodb.velocypack.VPackBuilder))

;;; NOTE - keys are always converted to Strings. this means that the
;;; type information is lost. Keywords `:a` `:b` etc are converted to
;;; strings by the `pack` function.
;;; (def d (new java.util.Date))
;;; (v/unpack (v/pack {:a d d true}))
;;; {"Sat Sep 30 17:12:18 CEST 2017" true, "a" #inst "2017-09-30T15:12:18.368-00:00"}

;;; keywords and strings - using :a and "a" in the same map -- not a good idea.
;;; user> (v/pack {:a "hello" "a" "world"})
;;; #object[com.arangodb.velocypack.VPackSlice 0x743c4699 "{\"a\":\"hello\",\"a\":\"world\"}"]
;;; user> (v/unpack (v/pack {:a "hello" "a" "world"}))
;;; {"a" "world"}

;;; use `unpack*` to convert all keys to keywords (included in maps inside arrays)
;;; will convert "1" to `:1`

(defn ^Boolean vpack-slice? [x]
  (= (type x) VPackSlice))

(defn ^ValueType get-type [^VPackSlice slice]
  (.getType slice))

(defn get
  "Returns the `VPackSlice` mapped to key, not-found or nil if key not present.
  keys are strings. if a keyword is (name key) will be called"
  ([slice k] (get slice k nil))
  ([slice k not-found]
   (let [val (.get slice (utils/normalize k))]
     (if (.isNone val) not-found val))))

(defn get*
  "Returns the value at key cast to its inferred type,
  not-found or nil if key not present. If the type is not one of
  None ILLEGAL NULL STRING INT BOOL DOUBLE UINT SMALLINT BINARY UTC_DATE
  then a slice will be returned. As such is is NOT consistent.
  keys are strings. if a keyword is (name key) will be called"
  ([slice k] (get* slice k nil))
  ([slice k not-found]
   (let [slice (.get slice (utils/normalize k))]
     (if (.isNone slice)
       not-found
       (case (-> slice .getType .toString)
         "None" nil
         "ILLEGAL" nil
         "NULL" nil
         "STRING" (.getAsString slice)
         "INT" (.getAsInt slice)
         "BOOL" (.getAsBoolean slice)
         "DOUBLE" (.getAsDouble slice)
         "UINT" (.getAsInt slice)
         "SMALLINT" (.getAsInt slice)
         "BINARY" (.getAsBinary slice)
         "UTC_DATE" (.getAsDate slice)
         "VPACK" slice
         "ARRAY" slice
         "OBJECT" slice
         "EXTERNAL" slice
         "MIN_KEY" slice
         "MAX_KEY" slice
         "BCD" slice
         "CUSTOM" slice)))))

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



(defn unpack [^VPackSlice slice]
  (case (-> slice .getType .toString)
    "ARRAY" (let [length (.getLength slice)]
              (for [i (range length)] (unpack (.get slice i))))
    "OBJECT" (let [length (.getLength slice)]
               (into {} (for [i (range length)]
                          [(unpack (.keyAt slice i)) (unpack (.valueAt slice i))])))
    "None" nil
    "ILLEGAL" nil
    "NULL" nil
    "STRING" (.getAsString slice)
    "INT" (.getAsInt slice)
    "BOOL" (.getAsBoolean slice)
    "DOUBLE" (.getAsDouble slice)
    "UINT" (.getAsInt slice)
    "SMALLINT" (.getAsInt slice)
    "BINARY" (.getAsBinary slice)
    "UTC_DATE" (.getAsDate slice)
    "VPACK" slice
    "EXTERNAL" slice
    "MIN_KEY" slice
    "MAX_KEY" slice
    "BCD" slice
    "CUSTOM" slice
    ))


(defn make-key [x]
  (keyword x))

(defn unpack* [^VPackSlice slice]
  (case (-> slice .getType .toString)
    "ARRAY" (let [length (.getLength slice)]
              (for [i (range length)] (unpack (.get slice i))))
    "OBJECT" (let [length (.getLength slice)]
               (into {} (for [i (range length)]
                          [(make-key (unpack (.keyAt slice i))) (unpack (.valueAt slice i))])))
    "None" nil
    "ILLEGAL" nil
    "NULL" nil
    "STRING" (.getAsString slice)
    "INT" (.getAsInt slice)
    "BOOL" (.getAsBoolean slice)
    "DOUBLE" (.getAsDouble slice)
    "UINT" (.getAsInt slice)
    "SMALLINT" (.getAsInt slice)
    "BINARY" (.getAsBinary slice)
    "UTC_DATE" (.getAsDate slice)
    "VPACK" slice
    "EXTERNAL" slice
    "MIN_KEY" slice
    "MAX_KEY" slice
    "BCD" slice
    "CUSTOM" slice
    ))
