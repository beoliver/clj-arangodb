(ns clj-arangodb.arangodb.collection-options
  (:import com.arangodb.model.CollectionCreateOptions
           com.arangodb.entity.CollectionType))

;; {:journalSize String
;;  :replicationFactor Int
;;  :allowUserKeys {}
;;  :waitForSync Boolean (default is false)
;;  :doCompact Boolean (default is true)
;;  :isVolatile Boolean (default is false)
;;  :shardKeys [Array String]
;;  :numberOfShards Integer
;;  :isSystem Boolean (default false - name must start with _)
;;  :type Type What type of collection to create
;;  :indexBuckets Integer ;; The default is 16 and this number has to be a power of 2 and less than or equal to 1024
;;  }

(defn- ^CollectionCreateOptions collection-type-option
  "one of `:edges` or `:document` (or CollectionType/DOCUMENT CollectionType/EDGES)"
  [^CollectionCreateOptions collection-options
   {:keys [type] :as user-options}]
  (if-not type
    collection-options
    (case type
      :document (.type collection-options CollectionType/DOCUMENT)
      :edges (.type collection-options CollectionType/EDGES)
      (.type collection-options type))))

(defn- ^CollectionCreateOptions replication-factor-option
  "a number that can be cast to int"
  [^CollectionCreateOptions collection-options
   {:keys [replicationFactor] :as user-options}]
  (if replicationFactor
    (.replicationFactor collection-options (int replicationFactor))
    collection-options))

(defn- ^CollectionCreateOptions wait-for-sync-option
  "a `bool`. Default is `false`"
  [^CollectionCreateOptions collection-options
   {:keys [waitForSync] :as user-options}]
  (if waitForSync
    (.waitForSync collection-options waitForSync)
    collection-options))

(defn- ^CollectionCreateOptions is-volatile-option
  "a `bool`. Default is `false`"
  [^CollectionCreateOptions collection-options
   {:keys [isVolatile] :as user-options}]
  (if isVolatile
    (.isVolatile collection-options isVolatile)
    collection-options))

(defn- ^CollectionCreateOptions number-of-shards-option
  "an `int`. Default is `?`"
  [^CollectionCreateOptions collection-options
   {:keys [numberOfShards] :as user-options}]
  (if numberOfShards
    (.numberOfShards collection-options (int numberOfShards))
    collection-options))

(defn create-options
  "given a map return a `CollectionCreateOptions` object
  returns nil if nil is passed"
  [{:keys [type waitForSync replicationFactor] :as options}]
  (when options
    (-> (new CollectionCreateOptions)
        (replication-factor-option options)
        (wait-for-sync-option options)
        (is-volatile-option options)
        (number-of-shards-option options)
        (collection-type-option options))))

;; (defn make-options [{:keys [type] :as options}]
;;   (-> (new CollectionCreateOptions)
;;       (collection-type options)))
