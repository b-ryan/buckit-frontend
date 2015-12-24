(ns buckit.frontend.db-test
  (:require [buckit.frontend.db          :as buckit.db]
            [buckit.frontend.models.core :as models]
            [cljs.test                   :refer-macros [deftest is testing run-tests]]))

(def db buckit.db/initial-state)

(deftest get-resources
  (is (= {} (buckit.db/get-resources db models/accounts)))

  (let [resource {1 {:id 1}}
        db       (assoc-in db [buckit.db/resources models/accounts] resource)]
    (is (= resource (buckit.db/get-resources db models/accounts)))))

(deftest inject-resources
  (let [acc-1     {:id 1 :name "foo"}
        acc-2     {:id 2 :name "bar"}
        resources [acc-1 acc-2]
        db (buckit.db/inject-resources db models/accounts resources)]
    (is (= {1 acc-1 2 acc-2} (get-in db [buckit.db/resources models/accounts])))))

(deftest update-query
  (let [query-id        "foo"
        db-with-2222    (buckit.db/update-query db query-id (constantly 2222))
        db-incremented  (buckit.db/update-query db-with-2222 query-id + 10)]
    (is (= nil (get-in db [buckit.db/queries query-id])))
    (is (= 2222 (get-in db-with-2222 [buckit.db/queries query-id])))
    (is (= 2232 (get-in db-incremented [buckit.db/queries query-id])))))
