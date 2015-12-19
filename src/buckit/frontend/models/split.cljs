(ns buckit.frontend.models.split)

(def id :id)
(def account-id :account_id)
(def amount :amount)
(def memo :memo)

(defn create
  "Creates a new split. If an account ID is given, it will be used. Otherwise
  the account ID will be nil. The amount will be 0."
  [& [_account-id]]
  {id         nil
   account-id _account-id
   amount     0})

(defn split-for-account
  [splits _account-id]
  (first (filter #(= (account-id %) _account-id) splits)))

(defn splits-for-other-accounts
  [splits _account-id]
  (remove #(= (account-id %) _account-id) splits))
