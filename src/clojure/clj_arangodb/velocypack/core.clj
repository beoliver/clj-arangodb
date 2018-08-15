(ns clj-arangodb.velocypack.core
  (:require [clj-arangodb.velocypack.utils :as utils])
  (:import [com.arangodb.velocypack
            ObjectIterator
            ArrayIterator
            VPackSlice
            ValueType
            VPackBuilder]))

(defprotocol VPackable
  (pack [this])
  (add-item [this builder])
  (add-entry [this k builder]))

(extend-protocol VPackable

  (Class/forName "[B")
  (pack [^bytes this] ^VPackSlice (.slice ^VPackBuilder (.add (VPackBuilder.) ^bytes this)))
  (add-item [^bytes this ^VPackBuilder builder] (.add builder ^bytes this))
  (add-entry [^bytes this ^String k ^VPackBuilder builder]
    (.add builder ^String k ^bytes this))

  nil
  ;; a hack - we pretend that we are passing an Object, but pass it nil
  (pack [this] ^VPackSlice (.slice ^VPackBuilder (.add (VPackBuilder.) ^Integer this)))
  (add-item [this ^VPackBuilder builder] (.add builder ^Integer this))
  (add-entry [this ^String k ^VPackBuilder builder]
    (.add builder ^String k ^Integer this))

  String
  (pack [this] ^VPackSlice (.slice ^VPackBuilder (.add (VPackBuilder.) ^String this)))
  (add-item [^String this ^VPackBuilder builder] (.add builder this))
  (add-entry [^String this ^String k ^VPackBuilder builder]
    (.add builder ^String k this))

  clojure.lang.Keyword
  (pack [this] ^VPackSlice (.slice ^VPackBuilder (.add (VPackBuilder.) ^String (name this))))
  (add-item [this ^VPackBuilder builder] (.add builder ^String (name this)))
  (add-entry [this ^String k ^VPackBuilder builder]
    (.add builder ^String k ^String (name this)))

  Long
  (pack [this] ^VPackSlice (.slice ^VPackBuilder (.add (VPackBuilder.) ^Long this)))
  (add-item [^Long this ^VPackBuilder builder] (.add builder this))
  (add-entry [^Long this ^String k ^VPackBuilder builder]
    (.add builder ^String k this))

  VPackSlice
  (pack [this] ^VPackSlice (.slice ^VPackBuilder (.add (VPackBuilder.) ^VPackSlice this)))
  (add-item [^VPackSlice this ^VPackBuilder builder] (.add builder this))
  (add-entry [^VPackSlice this ^String k ^VPackBuilder builder]
    (.add builder ^String k this))

  Short
  (pack [this] ^VPackSlice (.slice ^VPackBuilder (.add (VPackBuilder.) ^Short this)))
  (add-item [^Short this ^VPackBuilder builder] (.add builder this))
  (add-entry [^Short this ^String k ^VPackBuilder builder]
    (.add builder ^String k this))

  BigInteger
  (pack [this] ^VPackSlice (.slice ^VPackBuilder (.add (VPackBuilder.) ^BigInteger this)))
  (add-item [^BigInteger this ^VPackBuilder builder] (.add builder this))
  (add-entry [^BigInteger this ^String k ^VPackBuilder builder]
    (.add builder ^String k this))

  BigDecimal
  (pack [this] ^VPackSlice (.slice ^VPackBuilder (.add (VPackBuilder.) ^BigDecimal this)))
  (add-item [^BigDecimal this ^VPackBuilder builder] (.add builder this))
  (add-entry [^BigDecimal this ^String k ^VPackBuilder builder]
    (.add builder ^String k this))

  java.util.Date
  (pack [this] ^VPackSlice (.slice ^VPackBuilder (.add (VPackBuilder.) ^java.util.Date this)))
  (add-item [^java.util.Date this ^VPackBuilder builder] (.add builder this))
  (add-entry [^java.util.Date this ^String k ^VPackBuilder builder]
    (.add builder ^String k this))

  java.sql.Date
  (pack [this] ^VPackSlice (.slice ^VPackBuilder (.add (VPackBuilder.) ^java.sql.Date this)))
  (add-item [^java.sql.Date this ^VPackBuilder builder] (.add builder this))
  (add-entry [^java.sql.Date this ^String k ^VPackBuilder builder]
    (.add builder ^String k this))

  Integer
  (pack [this] ^VPackSlice (.slice ^VPackBuilder (.add (VPackBuilder.) ^Integer this)))
  (add-item [^Integer this ^VPackBuilder builder] (.add builder this))
  (add-entry [^Integer this ^String k ^VPackBuilder builder]
    (.add builder ^String k this))

  Character
  (pack [this] ^VPackSlice (.slice ^VPackBuilder (.add (VPackBuilder.) ^Character this)))
  (add-item [^Character this ^VPackBuilder builder] (.add builder this))
  (add-entry [^Character this ^String k ^VPackBuilder builder]
    (.add builder ^String k this))

  Double
  (pack [this] ^VPackSlice (.slice ^VPackBuilder (.add (VPackBuilder.) ^Double this)))
  (add-item [^Double this ^VPackBuilder builder] (.add builder this))
  (add-entry [^Double this ^String k ^VPackBuilder builder]
    (.add builder ^String k this))

  Float
  (pack [this] ^VPackSlice (.slice ^VPackBuilder (.add (VPackBuilder.) ^Float this)))
  (add-item [^Float this ^VPackBuilder builder] (.add builder this))
  (add-entry [^Float this ^String k ^VPackBuilder builder]
    (.add builder ^String k this))

  Boolean
  (pack [this] ^VPackSlice (.slice ^VPackBuilder (.add (VPackBuilder.) ^Boolean this)))
  (add-item [^Boolean this ^VPackBuilder builder] (.add builder this))
  (add-entry [^Boolean this ^String k ^VPackBuilder builder]
    (.add builder ^String k this))

  clojure.lang.Sequential
  (pack [this] ^VPackSlice
    (.slice (.close ^VPackBuilder
                    (reduce (fn [^VPackBuilder b x]
                              (add-item x b))
                            (.add (VPackBuilder.) ^ValueType ValueType/ARRAY)
                            this))))
  (add-item [this ^VPackBuilder builder]
    (.close ^VPackBuilder
            (reduce (fn [^VPackBuilder builder elem]
                      (add-item elem builder))
                    (.add builder ^ValueType ValueType/ARRAY)
                    this)))
  (add-entry [this ^String k ^VPackBuilder builder]
    (.close ^VPackBuilder
            (reduce (fn [^VPackBuilder b x]
                      (add-item x b))
                    (.add builder ^String k ^ValueType ValueType/ARRAY)
                    this)))

  clojure.lang.IPersistentSet
  (pack [this] (pack (seq this)))
  (add-item [this builder] (add-item (seq this) builder))
  (add-entry [this k builder] (add-entry (seq this) k builder))

  clojure.lang.IPersistentMap
  (pack [this] ^VPackSlice
    (.slice (.close ^VPackBuilder
                    (reduce (fn [^VPackBuilder b [k v]]
                              (add-entry v (if (instance? clojure.lang.Keyword k) (. ^clojure.lang.Named k (getName))
                                               (. k (toString))) b))
                            (.add (VPackBuilder.) ^ValueType ValueType/OBJECT)
                            this))))

  (add-item [this ^VPackBuilder builder]
    (.close ^VPackBuilder
            (reduce (fn [^VPackBuilder b [k v]]
                      (add-entry v (if (instance? clojure.lang.Keyword k) (. ^clojure.lang.Named k (getName))
                                       (. k (toString))) b))
                    (.add builder ^ValueType ValueType/OBJECT)
                    this)))

  (add-entry [this ^String k ^VPackBuilder builder]
    (.close ^VPackBuilder
            (reduce (fn [^VPackBuilder b [k v]]
                      (add-entry v (if (instance? clojure.lang.Keyword k) (. ^clojure.lang.Named k (getName))
                                       (. k (toString))) b))
                    (.add builder ^String k ^ValueType ValueType/OBJECT)
                    this))))

(defn unpack
  "Deserialize a VPackSlice.
  Takes an optional `key-fn`.
  By default integer and float keys are converted
  all others are returned as keywords"
  ([^VPackSlice slice] (unpack slice utils/str->key))
  ([^VPackSlice slice key-fn]
   (case (.toString ^ValueType (.getType slice))
     "OBJECT"
     (let [len (long (.getLength slice))]
       (loop [acc (transient {})
              i (long 0)]
         (if (== i len)
           (persistent! acc)
           (recur
            (assoc! acc
                    (key-fn (.getAsString (.keyAt slice i)))
                    (unpack (.valueAt slice i) key-fn))
            (inc i)))))
     "ARRAY"
     (loop [^ArrayIterator iter (.arrayIterator slice)
            xs (transient [])]
       (if (.hasNext iter)
         (recur iter (conj! xs (unpack (.next iter) key-fn)))
         (persistent! xs)))
     "NULL" nil
     "STRING" (.getAsString slice)
     "BOOL" (.getAsBoolean slice)
     "DOUBLE" (.getAsDouble slice)
     "UTC_DATE" (.getAsDate slice)
     "INT" (.getAsLong slice)
     "UINT" (.getAsInt slice)
     "SMALLINT" (.getAsInt slice)
     "BINARY" (.getAsBinary slice)
     "NONE" nil
     "ILLEGAL" nil
     slice)))
