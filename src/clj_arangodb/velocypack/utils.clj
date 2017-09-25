(ns clj-arangodb.velocypack.utils)

(defn seqable? [x]
  (or (nil? x)
      (instance? clojure.lang.ISeq x)
      (instance? clojure.lang.Seqable x)
      (instance? Iterable x)
      (instance? java.util.Map x)
      (instance? CharSequence x)
      (.. x getClass isArray)))

(defn normalize [k]
  (if (keyword? k) (name k) (str k)))
