(ns clj-arangodb.velocypack.core
  ""
  (:refer-clojure :exclude [get get-in])
  (:require [clj-arangodb.velocypack.utils :as utils])
  (:import com.arangodb.velocypack.VPackSlice
           com.arangodb.velocypack.ValueType
           com.arangodb.velocypack.VPack
           com.arangodb.velocypack.VPackBuilder))


(declare pack)
(declare unpack)
(declare get)
(declare get-in)

;;; NOTE - keys are always converted to Strings.

(defn vpack-slice? [x]
  (= (type x) VPackSlice))

(defn ^ValueType get-type [^VPackSlice slice]
  (.getType slice))

(defn get-slice
  "Returns the `VPackSlice` mapped to key, not-found or nil if key not present.
  keys are strings. if a keyword is (name key) will be called"
  ([slice k] (get-slice slice k nil))
  ([slice k not-found]
   (let [val (.get slice (utils/normalize k))]
     (if (.isNone val) not-found val))))

(defn get
  "Returns the value at key cast to its inferred type,
  not-found or nil if key not present. If the type is not one of
  None ILLEGAL NULL STRING INT BOOL DOUBLE UINT SMALLINT BINARY UTC_DATE
  then a slice will be returned. As such is is NOT consistent.
  keys are strings. if a keyword is (name key) will be called"
  ([slice k] (get slice k nil))
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
         "ARRAY" (unpack slice keyword)
         "OBJECT" (unpack slice keyword)
         "EXTERNAL" slice
         "MIN_KEY" slice
         "MAX_KEY" slice
         "BCD" slice
         "CUSTOM" slice)))))

(defn get-in
  "Returns nil if the key is not present, or the not-found value if supplied.
  else retuns a clojure object (or vpack slice if it can not find the type)"
  ([slice ks] (get-in slice ks nil))
  ([slice ks not-found]
   (let [inner-slice (reduce (fn [slice k]
                               (.get slice (utils/normalize k))) slice ks)]
     (if (.isNone inner-slice) not-found (unpack inner-slice keyword)))))



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
        (seqable? xs)
        (.slice (build-array (-> (new VPackBuilder) (.add ValueType/ARRAY)) xs))
        :else (pack-one xs)))

(defn build-array [builder seq]
  (-> (reduce (fn [builder elem]
                (cond (map? elem) (build-map (.add builder ValueType/OBJECT) elem)
                      ;; as a string is seqable we test for it early on.
                      ;; otherwise we store a list of characters!
                      (string? elem) (.add builder elem)
                      (nil? elem) (.add builder elem)
                      (seqable? elem) (build-array (.add builder ValueType/ARRAY) elem)
                      :else (.add builder elem)))
              builder seq)
      .close))

(defn build-map
  [builder data]
  (-> (reduce
       (fn [builder [k v]]
         (as-> (utils/normalize k) $
           (cond (string? v)  (.add builder $ v)
                 (nil? v)     (.add builder $ v)
                 (number? v)  (.add builder $ v)
                 (map? v)     (build-map (.add builder $ ValueType/OBJECT) v)
                 (seqable? v) (build-array (.add builder $ ValueType/ARRAY) (seq v))
                 :else        (.add builder $ v)))) builder data)
      .close))


(defn unpack
  ([^VPackSlice slice] (unpack slice identity))
  ([^VPackSlice slice keyword-fn]
   (case (-> slice .getType .toString)
     "STRING" (.getAsString slice)
     "OBJECT" (into {} (for [i (range (.getLength slice))]
                         [(keyword-fn (unpack (.keyAt slice i) keyword-fn))
                          (unpack (.valueAt slice i) keyword-fn)]))
     "ARRAY" (into [] (for [i (range (.getLength slice))]
                        (unpack (.get slice i) keyword?)))
     "INT" (.getAsInt slice)
     "BOOL" (.getAsBoolean slice)
     "DOUBLE" (.getAsDouble slice)
     "UINT" (.getAsInt slice)
     "SMALLINT" (.getAsInt slice)
     "None" nil
     "ILLEGAL" nil
     "NULL" nil
     "BINARY" (.getAsBinary slice)
     "UTC_DATE" (.getAsDate slice)
     "VPACK" slice
     "EXTERNAL" slice
     "MIN_KEY" slice
     "MAX_KEY" slice
     "BCD" slice
     "CUSTOM" slice
     ))
  )
