(ns buckit.frontend.views.transactions
  (:require [buckit.frontend.routes :as routes]
            [re-frame.core :refer [subscribe]]))

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

(defn ledger
  [account-id selected-transaction-id & {:keys [edit-transaction?]}]
  (let [accounts     (subscribe [:accounts-by-id])
        payees       (subscribe [:payees-by-id])
        transactions (subscribe [:transactions])]
    (fn
      [account-id selected-transaction-id & {:keys [edit-transaction?]}]
      (let [transactions (filter #(account-in-splits? account-id %) @transactions)]
        [:div.buckit--ledger
         [:div.container-fluid
          [:div.row.buckit--ledger-header
           [:span.col-sm-2 "Date"]
           [:span.col-sm-2 "Payee"]
           [:span.col-sm-3 "Category"]
           [:span.col-sm-3 "Memo"]
           [:span.col-sm-2 "Amount"]]]
         (doall
           (for [transaction transactions
                 :let [transaction-id (:id transaction)
                       is-selected?   (= selected-transaction-id transaction-id)
                       splits         (:splits transaction)
                       main-split     (split-for-account splits account-id)
                       other-splits   (splits-for-other-accounts splits account-id)]]
             ^{:key transaction-id}
             [:div.container-fluid.buckit--ledger-row
              {:class    (when is-selected? "active")
               :on-click #(routes/go-to
                            ((if is-selected?
                               routes/account-transaction-edit-url
                               routes/account-transaction-details-url)
                              {:account-id account-id
                               :transaction-id transaction-id}))}

              [:div.row
               [:span.col-sm-2 (:date transaction)]
               [:span.col-sm-2 (->> transaction :payee-id (get @payees) :name)]
               [:span.col-sm-3 (account-to-show @accounts other-splits)]
               [:span.col-sm-3]
               [:span.col-sm-2 (amount-to-show main-split)]]

              (when (and edit-transaction? is-selected?)
                [:div.row
                 [:span.col-sm-12 "heyo"]])]))]))))

(defn transactions
  [account-id selected-transaction-id & {:keys [edit-transaction?]}]
  [:div.buckit--transactions-view
   [ledger account-id selected-transaction-id
    :edit-transaction? edit-transaction?]])
