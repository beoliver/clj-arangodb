(ns clj-arangodb.arangodb.test-data
  (:require [clj-arangodb.arangodb.databases :as d]
            [clj-arangodb.arangodb.adapter :as adapter]
            [clj-arangodb.arangodb.aql :as aql]
            [clj-arangodb.arangodb.helper :as h]))

(def ^:const game-of-thrones-db-label "gameOfThronesDB")

(def ^:private characters
  (adapter/double-quote-strings
   [{:name "Robert" :surname "Baratheon" :alive false :traits ["A" "H" "C"]}
    {:name "Jaime" :surname "Lannister" :alive true :age 36 :traits ["A" "F" "B"]}
    {:name "Ned" :surname "Stark" :alive true :age 41 :traits ["A" "H" "C" "N" "P"]}
    {:name "Catelyn" :surname "Stark" :alive false :age 40 :traits ["D" "H" "C"]}
    {:name "Cersei" :surname "Lannister" :alive true :age 36 :traits ["H" "E" "F"]}
    {:name "Daenerys" :surname "Targaryen" :alive true :age 16 :traits ["D" "H" "C"]}
    {:name "Jorah" :surname "Mormont" :alive false :traits ["A" "B" "C" "F"]}
    {:name "Petyr" :surname "Baelish" :alive false :traits ["E" "G" "F"]}
    {:name "Viserys" :surname "Targaryen" :alive false :traits ["O" "L" "N"]}
    {:name "Jon" :surname "Snow" :alive true :age 16 :traits ["A" "B" "C" "F"]}
    {:name "Sansa" :surname "Stark" :alive true :age 13 :traits ["D" "I" "J"]}
    {:name "Arya" :surname "Stark" :alive true :age 11 :traits ["C" "K" "L"]}
    {:name "Robb" :surname "Stark" :alive false :traits ["A" "B" "C" "K"]}
    {:name "Theon" :surname "Greyjoy" :alive true :age 16 :traits ["E" "R" "K"]}
    {:name "Bran" :surname "Stark" :alive true :age 10 :traits ["L" "J"]}
    {:name "Joffrey" :surname "Baratheon" :alive false :age 19 :traits ["I" "L" "O"]}
    {:name "Sandor" :surname "Clegane" :alive true :traits ["A" "P" "K" "F"]}
    {:name "Tyrion" :surname "Lannister" :alive true :age 32 :traits ["F" "K" "M" "N"]}
    {:name "Khal" :surname "Drogo" :alive false :traits ["A" "C" "O" "P"]}
    {:name "Tywin" :surname "Lannister" :alive false :traits ["O" "M" "H" "F"]}
    {:name "Davos" :surname "Seaworth" :alive true :age 49 :traits ["C" "K" "P" "F"]}
    {:name "Samwell" :surname "Tarly" :alive true :age 17 :traits ["C" "L" "I"]}
    {:name "Stannis" :surname "Baratheon" :alive false :traits ["H" "O" "P" "M"]}
    {:name "Melisandre" :alive true :traits ["G" "E" "H"]}
    {:name "Margaery" :surname "Tyrell" :alive false :traits ["M" "D" "B"]}
    {:name "Jeor" :surname "Mormont" :alive false :traits ["C" "H" "M" "P"]}
    {:name "Bronn" :alive true :traits ["K" "E" "C"]}
    {:name "Varys" :alive true :traits ["M" "F" "N" "E"]}
    {:name "Shae" :alive false :traits ["M" "D" "G"]}
    {:name "Talisa" :surname "Maegyr" :alive false :traits ["D" "C" "B"]}
    {:name "Gendry" :alive false :traits ["K" "C" "A"]}
    {:name "Ygritte" :alive false :traits ["A" "P" "K"]}
    {:name "Tormund" :surname "Giantsbane" :alive true :traits ["C" "P" "A" "I"]}
    {:name "Gilly" :alive true :traits ["L" "J"]}
    {:name "Brienne" :surname "Tarth" :alive true :age 32 :traits ["P" "C" "A" "K"]}
    {:name "Ramsay" :surname "Bolton" :alive true :traits ["E" "O" "G" "A"]}
    {:name "Ellaria" :surname "Sand" :alive true :traits ["P" "O" "A" "E"]}
    {:name "Daario" :surname "Naharis" :alive true :traits ["K" "P" "A"]}
    {:name "Missandei" :alive true :traits ["D" "L" "C" "M"]}
    {:name "Tommen" :surname "Baratheon" :alive true :traits ["I" "L" "B"]}
    {:name "Jaqen" :surname "H'ghar" :alive true :traits ["H" "F" "K"]}
    {:name "Roose" :surname "Bolton" :alive true :traits ["H" "E" "F" "A"]}
    {:name "The High Sparrow" :alive true :traits ["H" "M" "F" "O"]}]))

(def ^:private traits
  (adapter/double-quote-strings
   [{:_key "A" :en "strong" :de "stark"}
    {:_key "B" :en "polite" :de "freundlich"}
    {:_key "C" :en "loyal" :de "loyal"}
    {:_key "D" :en "beautiful" :de "schön"}
    {:_key "E" :en "sneaky" :de "hinterlistig"}
    {:_key "F" :en "experienced" :de "erfahren"}
    {:_key "G" :en "corrupt" :de "korrupt"}
    {:_key "H" :en "powerful" :de "einflussreich"}
    {:_key "I" :en "naive" :de "naiv"}
    {:_key "J" :en "unmarried" :de "unverheiratet"}
    {:_key "K" :en "skillful" :de "geschickt"}
    {:_key "L" :en "young" :de "jung"}
    {:_key "M" :en "smart" :de "klug"}
    {:_key "N" :en "rational" :de "rational"}
    {:_key "O" :en "ruthless" :de "skrupellos"}
    {:_key "P" :en "brave" :de "mutig"}
    {:_key "Q" :en "mighty" :de "mächtig"}
    {:_key "R" :en "weak" :de "schwach" }]))

(def ^:private child-of
  (adapter/double-quote-strings
   [{:parent {:name "Ned" :surname "Stark"}
     :child {:name "Robb" :surname "Stark"}}
    {:parent {:name "Ned" :surname "Stark"}
     :child {:name "Sansa" :surname "Stark"}}
    {:parent {:name "Ned" :surname "Stark"}
     :child {:name "Arya" :surname "Stark"}}
    {:parent {:name "Ned" :surname "Stark"}
     :child {:name "Bran" :surname "Stark"}}
    {:parent {:name "Catelyn" :surname "Stark"}
     :child {:name "Robb" :surname "Stark"}}
    {:parent {:name "Catelyn" :surname "Stark"}
     :child {:name "Sansa" :surname "Stark"}}
    {:parent {:name "Catelyn" :surname "Stark"}
     :child {:name "Arya" :surname "Stark"}}
    {:parent {:name "Catelyn" :surname "Stark"}
     :child {:name "Bran" :surname "Stark" }}
    {:parent {:name "Ned" :surname "Stark"}
     :child {:name "Jon" :surname "Snow" }}
    {:parent {:name "Tywin" :surname "Lannister"}
     :child {:name "Jaime" :surname "Lannister"}}
    {:parent {:name "Tywin" :surname "Lannister"}
     :child {:name "Cersei" :surname "Lannister"}}
    {:parent {:name "Tywin" :surname "Lannister"}
     :child {:name "Tyrion" :surname "Lannister"}}
    {:parent {:name "Cersei" :surname "Lannister"}
     :child {:name "Joffrey" :surname "Baratheon"}}
    {:parent {:name "Jaime" :surname "Lannister"}
     :child {:name "Joffrey" :surname "Baratheon"}}]))

(defn- drop-db [label]
  (h/with-db
    [db label]
    (d/drop db)))

(defn init-game-of-thrones-db []
  (drop-db game-of-thrones-db-label)
  (h/with-db
    [db game-of-thrones-db-label]
    (d/create-collection db "Characters")
    (d/create-collection db "Traits")
    (d/create-collection db "ChildOf" {:type com.arangodb.entity.CollectionType/EDGES})
    (->> [:LET ["data" characters]
          [:FOR ["c" "data"]
           [:INSERT "c" "Characters"]]]
         (d/query db))
    (->> [:LET ["data" traits]
          [:FOR ["t" "data"]
           [:INSERT "t" "Traits"]]]
         (d/query db))
    (->> [:LET ["data" child-of]
          [:FOR ["rel" "data"]
           [:LET ["parentId" [:FIRST
                              [:FOR ["c" "Characters"]
                               [:FILTER [:EQ "c.name" "rel.parent.name"]]
                               [:FILTER [:EQ "c.surname" "rel.parent.surname"]]
                               [:LIMIT 1]
                               [:RETURN "c._id"]]]
                  "childId" [:FIRST
                             [:FOR ["c" "Characters"]
                              [:FILTER [:EQ "c.name" "rel.child.name"]]
                              [:FILTER [:EQ "c.surname" "rel.child.surname"]]
                              [:LIMIT 1]
                              [:RETURN "c._id"]]]]
            [:FILTER [:AND [:NE "parentId" nil] [:NE "childId" nil]]]
            [:INSERT {:_from "childId" :_to "parentId"} "ChildOf"]
            [:RETURN "NEW"]]]]
         (d/query db))))
