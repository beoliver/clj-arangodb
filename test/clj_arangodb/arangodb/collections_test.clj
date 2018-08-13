(ns clj-arangodb.arangodb.collections-test
  (:require [clojure.set :as set]
            [clj-arangodb.arangodb.databases :as d]
            [clj-arangodb.arangodb.aql :as aql]
            [clj-arangodb.arangodb.adapter :as adapter]
            [clj-arangodb.arangodb.cursor :as cursor]
            [clj-arangodb.arangodb.test.helper :as h]
            [clj-arangodb.arangodb.test.test-data :as td]
            [clojure.test :refer :all]))

(deftest collection-size-test
  (h/with-db [db td/game-of-thrones-db-label]
    (let [query [:RETURN [:LENGTH "Characters"]]]
      (is (= (first (d/query db query Integer))
             43)))))

(deftest neds-children-test
  (h/with-db [db td/game-of-thrones-db-label]
    (let [children #{"Robb" "Jon" "Bran" "Arya" "Sansa"}
          query [:FOR ["c" "Characters"]
                 [:FILTER [:EQ "c.name" "\"Ned\""]]
                 [:FOR ["v" {:start "c"
                             :type :inbound
                             :depth [1 1]
                             :collections ["ChildOf"]}]
                  [:RETURN "v.name"]]]]
      (is (= (set (map adapter/deserialize-doc (d/query db query)))
             children)))))

(deftest group-test-1
  (h/with-db [db td/game-of-thrones-db-label]
    (is (= (->> [:FOR ["c" "Characters"]
                 [:COLLECT ["surname" "c.surname"]]
                 [:RETURN "surname"]]
                (d/query db)
                ((comp set (partial map adapter/deserialize-doc))))
           (->> [:FOR ["c" "Characters"]
                 [:RETURN-DISTINCT "c.surname"]]
                (d/query db)
                ((comp set (partial map adapter/deserialize-doc))))))))

(deftest group-test-2
  (h/with-db [db td/game-of-thrones-db-label]
    (let [result (->> [:FOR ["c" "Characters"]
                       [:COLLECT
                        ["surname" "c.surname"] {:into ["members" "c.name"]}]
                       [:FILTER [:NE "surname" nil]]
                       [:RETURN ["surname" "members"]]]
                      (d/query db)
                      (map adapter/deserialize-doc)
                      (into {}))]
      (is (= (count (get result "Lannister")) 4))
      (is (= (count (get result "Stark")) 6)))))

(deftest brans-parents-test
  (h/with-db [db td/game-of-thrones-db-label]
    (let [parents #{"Ned" "Catelyn"}
          query [:FOR ["c" "Characters"]
                 [:FILTER [:EQ "c.name" "\"Bran\""]]
                 [:LIMIT 1] ;; does not change anything
                 [:FOR ["v" {:start "c"
                             :type :outbound
                             :depth [1 1]
                             :collections ["ChildOf"]}]
                  [:RETURN "v.name"]]]]
      (is (= (set (map adapter/deserialize-doc (d/query db query)))
             parents)))))

(deftest tywins-grandchildren-test
  (h/with-db [db td/game-of-thrones-db-label]
    (let [grandchildren #{"Joffrey"}
          query [:FOR ["c" "Characters"]
                 [:FILTER [:EQ "c.name" "\"Tywin\""]]
                 [:FOR ["v" {:start "c"
                             :type :inbound
                             :depth [2 2]
                             :collections ["ChildOf"]}]
                  [:RETURN-DISTINCT "v.name"]]]]
      (is (= (set (map adapter/deserialize-doc (d/query db query)))
             grandchildren)))))

(deftest joffrey-parents-and-grandparents-test
  (h/with-db [db td/game-of-thrones-db-label]
    (let [grandparents #{"Tywin"}
          parents #{"Cersei" "Jaime"}
          query [:FOR ["c" "Characters"]
                 [:FILTER [:EQ "c.name" "\"Joffrey\""]]
                 [:FOR ["v" {:start "c"
                             :type :outbound
                             :depth [1 2]
                             :collections ["ChildOf"]}]
                  [:RETURN-DISTINCT "v.name"]]]]
      (is (= (set (map adapter/deserialize-doc (d/query db query)))
             (set/union parents grandparents))))))
