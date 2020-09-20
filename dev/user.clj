(ns user
  (:require [clojure.reflect :as r]
            [clj-arangodb.arangodb.core :as a]
            [clj-arangodb.arangodb.databases :as d]
            [clj-arangodb.arangodb.collections :as c]
            [clj-arangodb.arangodb.graph :as g]
            [clj-arangodb.velocypack.core :as v]
            [clj-arangodb.arangodb.adapter :as adapter]
            [clj-arangodb.arangodb.test-data :as td]
            [clj-arangodb.arangodb.helper :as h]
            )
  (:import [com.arangodb Protocol]
           [com.arangodb.entity BaseDocument]
           [com.arangodb.velocypack VPackSlice]))
