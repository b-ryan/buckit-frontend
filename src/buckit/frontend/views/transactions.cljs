(ns buckit.frontend.views.transactions
  (:require [re-frame.core :refer [subscribe]]))

; TODO this should be moved
(defn- account-in-splits?
  "Returns true if any of the splits for the given transaction have the account
  ID."
  [account-id transaction]
  {:pre [(integer? account-id)]}
  (let [splits (:splits transaction)
        account-ids (map :account-id splits)]
    (some #{account-id} account-ids)))

(defn transactions
  []
  (let [accounts (subscribe [:accounts])
        url-params (subscribe [:url-params])
        transactions (subscribe [:transactions])]
    (fn
      []
      (let [account-id (:account-id @url-params)
            transactions (filter #(account-in-splits? account-id %) @transactions)]
        [:pre (.stringify js/JSON (clj->js transactions) nil "\t")]))))
