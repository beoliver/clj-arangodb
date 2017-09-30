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


(defn- ^CollectionCreateOptions collection-type
  [^CollectionCreateOptions collection-options
   {:keys [type] :as user-options}]
  (case type
    :document (.type collection-options CollectionType/DOCUMENT)
    :edge (.type collection-options CollectionType/EDGES)
    collection-options))

(defn make-options [{:keys [type] :as options}]
  (-> (new CollectionCreateOptions)
      (collection-type options)))
