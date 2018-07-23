(ns clj-arangodb.arangodb.admin)

(defn version
  "can be called on a `conn` or `database`, retuns a map with the keys
  `:liscense`, `:server`, `:version`"
  [conn-or-db]
  (-> (.getVersion conn-or-db)
      bean
      (dissoc :class)
      (update :license str)))

(defn users
  "returns a map with user names as keys. Vals are maps with additional info"
  [conn]
  (reduce (fn [user-map obj]
            (let [user (-> obj bean (dissoc :class))]
              (assoc user-map (:user user) user))) {} (.getUsers conn)))
