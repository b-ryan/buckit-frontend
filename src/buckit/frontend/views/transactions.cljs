(ns buckit.frontend.views.transactions
  (:require [buckit.frontend.keyboard :as keyboard]
            [buckit.frontend.models.account :as models.account]
            [buckit.frontend.models.split :as models.split]
            [buckit.frontend.models.transaction :as models.payee]
            [buckit.frontend.models.transaction :as models.transaction]
            [buckit.frontend.routes :as routes]
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as reagent]
            [reagent-forms.core :as forms]))

; TODO this should be moved
(defn- account-in-splits?
  "Returns true if any of the splits for the given transaction have the account
  ID."
  [account-id transaction]
  {:pre [(integer? account-id)]}
  (let [splits      (:splits transaction)
        account-ids (map models.split/account-id splits)]
    (some #{account-id} account-ids)))

(defn- split-for-account
  [splits account-id]
  (first (filter #(= (models.split/account-id %) account-id) splits)))

(defn- splits-for-other-accounts
  [splits account-id]
  (remove #(= (models.split/account-id %) account-id) splits))

(defn- account-to-show
  [accounts other-splits]
  (if (> (count other-splits) 1)
    "Splits"
    (->> other-splits
         first
         models.split/account-id
         (get accounts)
         models.account/name)))

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

(defn- ledger-row
  [account-id transaction & {:keys [is-selected?]}]
  (let [accounts (subscribe [:accounts])
        payees   (subscribe [:payees])]
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
                           :transaction-id (models.transaction/id transaction)}))}
          [:span.col-sm-2 (:date transaction)]
          [:span.col-sm-2 (->> transaction
                               models.transaction/payee-id
                               (get @payees)
                               models.payee/name)]
          [:span.col-sm-3 (account-to-show @accounts other-splits)]
          [:span.col-sm-3]
          [:span.col-sm-2 (amount-to-show main-split)]]))))

(defn- editor-div
  [width content]
  [:div.buckit--ledger-editor-input {:class (str "col-sm-" width)} content])

(defn- input
  [& options]
  [:input.form-control.input-sm (apply hash-map options)])

(def initial-focus-wrapper
  ; TODO move
  (with-meta identity
    {:component-did-mount #(.focus (reagent/dom-node %))}))

(defn- date-editor-template
  []
  (editor-div 2 [initial-focus-wrapper
                 (input :id [:transaction models.transaction/date]
                        :field :text :placeholder "Date")]))

(defn- payee-editor-template
  [payees]
  (editor-div 2 [:select.form-control.input-sm
                 {:field :list :id [:transaction models.transaction/payee-id]}
                 (for [[payee-id payee] payees]
                   ^{:key payee-id}
                   [:option
                    {:key payee-id :visible? (constantly true)}
                    (models.payee/name payee)])]))

(defn- split-editor-template
  [split-path accounts]
  (list
    (with-meta
      (editor-div 3 [:select.form-control.input-sm
                     {:field :list :id (conj split-path models.split/account-id)}
                     (for [[account-id account] accounts]
                       ^{:key account-id}
                       [:option
                        {:key account-id :visible? (constantly true)}
                        (models.account/name account)])])
      {:key :account-id})

    (with-meta
      (editor-div 3 (input :id (conj split-path models.split/memo)
                           :field :numeric :placeholder "Memo"))
      {:key :memo})

    (with-meta
      (editor-div 2 (input :id (conj split-path models.split/amount)
                           :field :numeric :placeholder "Amount"))
      {:key :amount})))

(defn- editor
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
                                    :other-splits (vec other-splits)})
        cancel       #(routes/go-to
                        (routes/account-transaction-details-url
                          {:account-id account-id
                           :transaction-id (models.transaction/id transaction)}))
        save         #(let [result      @form
                            splits      (into [(:main-split result)]
                                              (:other-splits result))
                            transaction (-> result
                                            :transaction
                                            (assoc :splits splits))]
                        (let [d (dispatch [:update-transaction transaction])]
                          (js/console.log (clj->js d))
                          d)
                        )]
    (fn
      [account-id transaction]
      [forms/bind-fields
       [:form
        {:on-key-down #(when (= (.-which %) keyboard/escape) (cancel))}
        [:div.row
         (date-editor-template)
         (payee-editor-template @payees)
         (split-editor-template [:main-split] @accounts)]
        (doall
          (for [i (range (count other-splits))]
            ^{:key i}
            [:div.row
             [:div.col-sm-4]
             (split-editor-template [:other-splits i] @accounts)]))
        [:div.row
         [:div.col-sm-12
          [:div.btn-toolbar.pull-right
           [:input.btn.btn-danger.btn-xs {:type "button"
                                          :on-click cancel
                                          :value "Cancel"}]
           [:input.btn.btn-success.btn-xs {:type "submit"
                                           :on-click save
                                           :value "Save"}]]]]]
       form])))

(defn- ledger
  [account-id selected-transaction-id & {:keys [edit-selected?]}]
  (let [transactions (subscribe [:transactions])]
    (fn
      [account-id selected-transaction-id & {:keys [edit-selected?]}]
      [:div.buckit--ledger
       ledger-header
       (doall
         (for [transaction (filter #(account-in-splits? account-id %)
                                   (vals @transactions))
               :let [transaction-id (models.transaction/id transaction)
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
