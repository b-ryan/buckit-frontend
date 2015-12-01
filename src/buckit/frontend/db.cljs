(ns buckit.frontend.db
  (:require [buckit.frontend.accounts :as accounts]))

;TODO prismatic schema to define db?

(defonce seed-db
  {:url-path []
   :url-params {}

   :accounts [{:id 1 :parent-id nil :name "Checking" :type accounts/asset}
              {:id 2 :parent-id nil :name "Savings" :type accounts/asset}
              {:id 3 :parent-id nil :name "Groceries" :type accounts/expense}]

   :payees [{:id 1 :name "Grocery Store"}]

   :transactions [
                  {:id 1
                   :date "2015-11-28"
                   :payee-id 1
                   :splits [{:id 1
                             :transaction-id 1
                             :account-id 1
                             :amount -20.52
                             :reconciled-status "not_reconciled"}
                            {:id 2
                             :transaction-id 1
                             :account-id 3
                             :amount 20.52
                             :reconciled-status "not_reconciled"}]}
                  {:id 2
                   :date "2015-11-29"
                   :payee-id 1
                   :splits [{:id 3
                             :transaction-id 2
                             :account-id 1
                             :amount -16.18
                             :reconciled-status "not_reconciled"}
                            {:id 4
                             :transaction-id 2
                             :account-id 2
                             :amount -2
                             :reconciled-status "not_reconciled"}
                            {:id 5
                             :transaction-id 2
                             :account-id 3
                             :amount 18.18
                             :reconciled-status "not_reconciled"}]}
                  {:id 3
                   :date "2015-11-30"
                   :payee-id 1
                   :splits [{:id 6
                             :transaction-id 3
                             :account-id 1
                             :amount 14.44
                             :reconciled-status "not_reconciled"}
                            {:id 7
                             :transaction-id 3
                             :account-id 3
                             :amount -14.44
                             :reconciled-status "not_reconciled"}]}
                  ]})
