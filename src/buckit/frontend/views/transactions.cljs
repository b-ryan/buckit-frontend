(ns buckit.frontend.views.transactions
  (:require [re-frame.core :refer [subscribe]]))

; TODO this should be moved
(defn- account-in-splits?
  "Returns true if any of the splits for the given transaction have the account
  ID."
  [account-id transaction]
  {:pre [(integer? account-id)]}
  (let [splits      (:splits transaction)
        account-ids (map :account-id splits)]
    (some #{account-id} account-ids)))

(defn- split-for-account
  [splits account-id]
  (first (filter #(= (:account-id %) account-id) splits)))

(defn- splits-for-other-accounts
  [splits account-id]
  (remove #(= (:account-id %) account-id) splits))

(defn- account-to-show
  [accounts other-splits]
  (if (> (count other-splits) 1)
    "Splits"
    (->> other-splits first :account-id (get accounts) :name)))

(defn- amount-to-show
  [main-split]
  ; FIXME this is not accessible! Perhaps use negative/positive, separate
  ; columns, or some other means.
  (let [amount   (:amount main-split)
        expense? (< amount 0)]
    [:span {:class (str "glyphicon "
                        (if expense? "glyphicon-arrow-right" "glyphicon-arrow-left")
                        " "
                        (if expense? "expense" "income"))
            :aria-hidden true}
     ; FIXME other currencies?
     (str " $" (js/Math.abs amount))]))

(def ledger-header
  [:tr
   [:th "ID"]
   [:th "Date"]
   [:th "Payee"]
   [:th "Category"]
   [:th "Amount"]])

(defn transactions
  []
  (let [accounts     (subscribe [:accounts-by-id])
        payees       (subscribe [:payees-by-id])
        transactions (subscribe [:transactions])
        url-params   (subscribe [:url-params])]
    (fn
      []
      (let [account-id (:account-id @url-params)
            transactions (filter #(account-in-splits? account-id %) @transactions)]
        [:table.table.buckit--ledger
         [:thead ledger-header]
         [:tbody
          (doall
            (for [transaction transactions
                  :let [splits       (:splits transaction)
                        main-split   (split-for-account splits account-id)
                        other-splits (splits-for-other-accounts splits account-id)]]
              ^{:key (:id transaction)}
              [:tr
               [:td (:id transaction)]
               [:td (:date transaction)]
               [:td (->> transaction :payee-id (get @payees) :name)]
               [:td (account-to-show @accounts other-splits)]
               [:td (amount-to-show main-split)]]))]]))))
