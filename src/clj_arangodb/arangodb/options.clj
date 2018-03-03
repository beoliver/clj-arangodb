(ns clj-arangodb.arangodb.options
  (:import
   com.arangodb.entity.CollectionType
   com.arangodb.model.CollectionCreateOptions))

;;; this namespace provides a number of functions that transform
;;; clojure maps into "options" found in com.arangodb.model

(def ^:constant empty-key-options
  {:allowUserKeys false
   :type nil ;; KeyType
   :increment (int 1)
   :offset (int 1)})

(def ^:constant default-collection-options
  {:name "exampleCollection"
   :journalSize (long 1048576) ;The maximal size of a journal or datafile in bytes
   :replicationFactor (int 1)
   :keyOptions empty-key-options
   :waitForSync false
   :doCompact true
   :isVolatile false
   :shardKeys ["_key"] ;This option is meaningless in a single server setup
   :numberOfShards (int 1) ;In a single server setup, this option is meaningless
   :isSystem false
   :type CollectionType/DOCUMENT
   :indexBuckets (int 16) ;a power of 2 and less than or equal to 1024
   }
  )
