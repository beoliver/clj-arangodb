(ns clj-arangodb.arangodb.helper
  (:require  [clj-arangodb.arangodb.core :as a]
             [clj-arangodb.arangodb.databases :as d]
             [clj-arangodb.arangodb.collections :as c]))

(defmacro with-conn
  [[conn spec] & body]
  `(let [conn# (a/connect ~spec)
         ~conn conn#]
     ~@body
     (a/shutdown conn#)))

(defmacro with-test-conn
  [& body]
  `(let [conn# (a/connect {:user "test"})]
     ~@body
     (a/shutdown conn#)))

(defmacro with-db
  [[db label] & body]
  `(let [conn# (a/connect {:user "test"})
         ~db (a/db conn# ~label)]
     (when-not (d/exists? ~db)
       (a/create-database conn# ~label))
     ~@body
     (a/shutdown conn#)))

(defmacro with-temp-db
  [[db label] & body]
  `(let [conn# (a/connect {:user "test"})
         ~db (a/db conn# ~label)]
     (try (d/drop ~db)
          (catch Exception _#))
     (when-not (d/exists? ~db)
       (a/create-database conn# ~label))
     ~@body
     (try (d/drop ~db)
          (catch Exception _#))
     (a/shutdown conn#)))

(defmacro with-db-and-coll
  [[db db-label
    coll coll-label] & body]
  `(let [conn# (a/connect {:user "test"})
         ~db (a/db conn# ~db-label)
         ~coll (d/collection ~db ~coll-label)]
     (when-not (d/exists? ~db)
       (a/create-database conn# ~db-label))
     (when-not (c/exists? ~coll)
       (d/create-collection ~db ~coll-label))
     ~@body
     (try (d/drop ~db)
          (catch Exception _#))
     (a/shutdown conn#)))

(defmacro with-coll
  [[coll coll-label] & body]
  `(let [conn# (a/connect {:user "test"})
         db-label# (str (gensym))
         db# (a/db conn# db-label#)
         coll# (d/collection db# ~coll-label)
         ~coll coll#]
     (when-not (d/exists? db#)
       (a/create-database conn# db-label#))
     (when-not (c/exists? coll#)
       (d/create-collection db# ~coll-label))
     ~@body
     (try (d/drop db#)
          (catch Exception _#))
     (a/shutdown conn#)))
