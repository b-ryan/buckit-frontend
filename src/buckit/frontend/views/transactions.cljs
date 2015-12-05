(ns buckit.frontend.views.transactions
  (:require [buckit.frontend.routes :as routes]
            [re-frame.core :refer [subscribe]]
            [reagent.core :as reagent]
            [reagent-forms.core :as forms]))

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
  [accounts-by-id other-splits]
  (if (> (count other-splits) 1)
    "Splits"
    (->> other-splits first :account-id (get accounts-by-id) :name)))

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
  (let [accounts-by-id (subscribe [:accounts-by-id])
        payees-by-id   (subscribe [:payees-by-id])]
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
          [:span.col-sm-2 (->> transaction :payee-id (get @payees-by-id) :name)]
          [:span.col-sm-3 (account-to-show @accounts-by-id other-splits)]
          [:span.col-sm-3]
          [:span.col-sm-2 (amount-to-show main-split)]]))))

(defn- editor-div
  [width content]
  [:div.buckit--ledger-editor-input {:class (str "col-sm-" width)} content])

(defn- input
  [& options]
  [:input.form-control.input-sm (apply hash-map options)])

(defn date-editor-template
  []
  (editor-div 2 (input :id :transaction.date :field :text :placeholder "Date")))

(defn payee-editor-template
  [payees]
  (editor-div 2 [:select.form-control.input-sm
                 {:field :list :id :transaction.payee-id}
                 (for [payee payees]
                   ^{:key (:id payee)}
                   [:option
                    {:key (:id payee) :visible? (constantly true)}
                    (:name payee)])]))

(defn split-editor-template
  [split-path accounts]
  (list
    ^{:key :account-id}
    (editor-div 3 [:select.form-control.input-sm
                   {:field :list :id (conj split-path :account-id)}
                   (for [account accounts]
                     ^{:key (:id account)}
                     [:option
                      {:key (:id account) :visible? (constantly true)}
                      (:name account)])])

    ^{:key :memo}
    (editor-div 3 (input :id (conj split-path :memo)
                         :field :text :placeholder "Memo"))

    ^{:key :amount}
    (editor-div 2 (input :id (conj split-path :amount)
                         :field :text :placeholder "Amount"))))

(defn editor
  ; GRR... reagent-forms doesn't seem to preserve metadata. Otherwise could
  ; just use lists instead of needing to concatenate vectors. TODO fix or open
  ; a ticket with reagent-forms
  [account-id transaction]
  (let [accounts     (subscribe [:accounts])
        payees       (subscribe [:payees])
        splits       (:splits transaction)
        main-split   (split-for-account splits account-id)
        other-splits (splits-for-other-accounts splits account-id)
        form         (reagent/atom {:transaction transaction
                                    :main-split main-split
                                    ; For some reason, this doesn't work unless
                                    ; it's a vector. I would guess it's because
                                    ; (get-in (list 1) [0]) => nil
                                    :other-splits (vec other-splits)})]
    (fn
      [account-id transaction]
      [:form
       [forms/bind-fields
        (into
          [:div.row
           (date-editor-template)
           (payee-editor-template @payees)]
          (split-editor-template [:main-split] @accounts))
        form]
       (doall
         (for [i (range (count other-splits))]
           ^{:key i}
           [forms/bind-fields 
            (into
              [:div.row [:div.col-sm-4]]
              (split-editor-template [:other-splits i] @accounts))
            form]))
       [:div.row
        [:div.col-sm-12
         [:div.btn-toolbar.pull-right
          [:button.btn.btn-danger.btn-xs "Cancel"]
          [:button.btn.btn-success.btn-xs "Save"]]]]])))

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
