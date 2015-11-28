(ns buckit.frontend.db)

(defonce default-db
  {:url-path []
   :url-params {}
   :accounts [{:id 1 :name "Checking"}
              {:id 2 :name "Savings"}]})
