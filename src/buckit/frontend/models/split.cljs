(ns buckit.frontend.models.split)

(def id             :id)
(def account-id     :account_id)
(def amount         :amount)
(def memo           :memo)
(def primary-split? :is_primary_split)

;; These properties are not always set on the split. But they can be used when
;; saving splits in order to save changes to the properties transactionally.
(def account        :account)

(defn split-for-account
  [splits _account-id]
  (first (filter #(= (account-id %) _account-id) splits)))

(defn splits-for-other-accounts
  [splits _account-id]
  (remove #(= (account-id %) _account-id) splits))
