(ns user
  (:require [clj-arangodb.arangodb.core :as arango]
            [clj-arangodb.arangodb.collections :as c]
            [clj-arangodb.arangodb.databases :as d]
            [clj-arangodb.velocypack.core :as vpack])
  (:import com.arangodb.velocypack.VPackSlice))

(def conn (arango/new-arrangodb {:user "dev" :password "123"}))

(def db-name "testDB")
(def coll-name "testCollection")

;; (def db
;;   (do
;;     (arango/create-database conn db-name)
;;     (arango/get-database conn db-name)))

;; (def coll
;;   (do
;;     (d/create-collection db coll-name)
;;     (d/get-collection db coll-name)))

;; (def data1 (vpack/vpack {:name "dev" :age 32 :likes ["clojure" 200 "space"]}))
;; (def ins1 (c/insert-document coll data1))

;; (def data2
;;   (vpack/vpack {:name "dev"
;;                 :a-float 32.902
;;                 :a-map {:a "hello" :b [1 2 3] :c "world"}
;;                 :date (new java.util.Date)}))
;; (def ins2 (c/insert-document coll data2))


(defn load-simpsons []
  (for [simpson [(vpack/vpack {:name "Homer" :age 38})
                 (vpack/vpack {:name "Marge" :age 36})
                 (vpack/vpack {:name "Bart" :age 10})
                 (vpack/vpack {:name "Lisa" :age 8})
                 (vpack/vpack {:name "Maggie" :age 2})]]
    (c/insert-document coll simpson)))


(defn query1 []
  (let [bind-vars (java.util.HashMap. {"one" 1 "two" 2})
        cursor (.query db "FOR i IN [ @one, @two ] RETURN i * 2" bind-vars nil VPackSlice)]
    cursor
    )
  )

(defn iteration->seq [iteration]
  (seq
   (reify java.lang.Iterable
     (iterator [this]
       (reify java.util.Iterator
         (hasNext [this] (.hasNext iteration))
         (next [this] (.next iteration))
         (remove [this] (.remove iteration)))))))
