(ns buckit.frontend.models.split)

(def id             :id)
(def account-id     :account_id)
(def amount         :amount)
(def memo           :memo)
(def primary-split? :is_primary_split)

(defn split-for-account
  [splits _account-id]
  (first (filter #(= (account-id %) _account-id) splits)))

(defn splits-for-other-accounts
  [splits _account-id]
  (remove #(= (account-id %) _account-id) splits))
