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

(def ledger-header
  [:div.container-fluid
   [:div.row.buckit--ledger-header
    [:span.col-sm-2 "Date"]
    [:span.col-sm-2 "Payee"]
    [:span.col-sm-3 "Category"]
    [:span.col-sm-3 "Memo"]
    [:span.col-sm-2 "Amount"]]])

(defn ledger-row
  [account-id transaction & {:keys [is-selected?]}]
  (let [accounts (subscribe [:accounts-by-id])
        payees   (subscribe [:payees-by-id])]
    (fn
      [account-id transaction & {:keys [is-selected?]}]
      (let [splits       (:splits transaction)
            main-split   (split-for-account splits account-id)
            other-splits (splits-for-other-accounts splits account-id)]
        [:div.row
         {:on-click #(routes/go-to
                       ((if is-selected?
                          routes/account-transaction-edit-url
                          routes/account-transaction-details-url)
                          {:account-id account-id
                           :transaction-id (:id transaction)}))}
          [:span.col-sm-2 (:date transaction)]
          [:span.col-sm-2 (->> transaction :payee-id (get @payees) :name)]
          [:span.col-sm-3 (account-to-show @accounts other-splits)]
          [:span.col-sm-3]
          [:span.col-sm-2 (amount-to-show main-split)]]))))

(defn editor
  [account-id transaction]
  (let []
    (fn
      [account-id transaction]
      [:div
       [:div.row
        [:input.col-sm-2 {:type "text" :placeholder "Date"}]
        [:input.col-sm-2 {:type "text" :placeholder "Payee"}]
        [:input.col-sm-3 {:type "text" :placeholder "Category"}]
        [:input.col-sm-3 {:type "text" :placeholder "Memo"}]
        [:input.col-sm-2 {:type "text" :placeholder "Amount"}]]
       [:div.row
        [:div.col-sm-12
         [:div.btn-toolbar.pull-right
          [:button.btn.btn-default.btn-xs "Cancel"]
          [:button.btn.btn-default.btn-xs "Save"]]]]]
      )))

(defn ledger
  [account-id selected-transaction-id & {:keys [edit-selected?]}]
  (let [transactions (subscribe [:transactions])]
    (fn
      [account-id selected-transaction-id & {:keys [edit-selected?]}]
      [:div.buckit--ledger
       ledger-header
       (doall
         (for [transaction (filter #(account-in-splits? account-id %) @transactions)
               :let [transaction-id (:id transaction)
                     is-selected?   (= selected-transaction-id transaction-id)]]
           ^{:key transaction-id}
           [:div.container-fluid.buckit--ledger-row
            {:class (when is-selected? "active")}
            (if (and is-selected? edit-selected?)
              [editor account-id transaction]
              [ledger-row account-id transaction
               :is-selected? (= selected-transaction-id transaction-id)])]))])))

(defn transactions
  [account-id selected-transaction-id & {:keys [edit-selected?]}]
  [:div.buckit--transactions-view
   [ledger account-id selected-transaction-id
    :edit-selected? edit-selected?]])
