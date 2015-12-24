(ns buckit.frontend.views.transactions
  (:require [buckit.frontend.db.query                  :as db.query]
            [buckit.frontend.i18n                      :as i18n]
            [buckit.frontend.models.account            :as models.account]
            [buckit.frontend.models.core               :as models]
            [buckit.frontend.models.split              :as models.split]
            [buckit.frontend.models.payee              :as models.payee]
            [buckit.frontend.models.transaction        :as models.transaction]
            [buckit.frontend.routes                    :as routes]
            [buckit.frontend.utils                     :as utils]
            [buckit.frontend.views.transactions.editor :as editor]
            [re-frame.core                             :refer [dispatch subscribe]]))

(defn- account-to-show
  [accounts other-splits]
  (if (> (count other-splits) 1)
    "Splits"
    (->> other-splits
         first
         models.split/account-id
         (get accounts)
         models.account/name)))

(defn- amount-glyphicon
  "Returns a left or right arrow glyphicon to show whether money left the
  account or is entering it."
  [amount]
  (let [expense? (< amount 0)]
    (str "glyphicon "
         (if expense? "glyphicon-arrow-right" "glyphicon-arrow-left")
         " "
         (if expense? "expense" "income"))))

(defn- amount-to-show
  [main-split]
  {:pre [(some? main-split)]}
  (let [amount   (:amount main-split)]
    [:span {:class (amount-glyphicon amount)
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
            main-split   (models.split/split-for-account splits account-id)
            other-splits (models.split/splits-for-other-accounts splits account-id)]
        (assert main-split)
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

(defn- toolbar
  [{:keys [account-id]}]
  [:div.buckit--transactions-toolbar
   [:button.btn.btn-default
    {:on-click #(routes/go-to (routes/account-transaction-create-url
                                {:account-id account-id}))}
    "+ Transaction"]])

(defn- ledger
  [context]
  (let [queries      (subscribe [:queries])
        transactions (subscribe [:transactions])]
    (fn
      [{:keys [account-id selected-transaction-id] :as context}]
      (let [query        [:load-account-transactions account-id]
            query-result (get @queries query)
            transactions (filter (partial models/account-in-splits? account-id)
                                 (vals @transactions))]

        (cond

          (db.query/successful? query-result)
          [:div.buckit--ledger
           ledger-header
           (doall
             (for [transaction transactions
                   :let [transaction-id (models.transaction/id transaction)
                         is-selected?   (= selected-transaction-id transaction-id)]]
               ^{:key transaction-id}
               [:div.container-fluid.buckit--ledger-row
                {:class (when is-selected? "active")}
                (if (and is-selected? (:edit-selected? context))
                  [editor/editor account-id transaction]
                  [ledger-row account-id transaction
                   :is-selected? (= selected-transaction-id transaction-id)])]))
           (when (:create-transaction? context)
             [:div.container-fluid.buckit--ledger-row.active
              [editor/editor account-id (-> (models.transaction/create account-id))]])]

          (db.query/failed? query-result)
          [:div [:p.text-danger i18n/transactions-not-loaded-error]]

          (db.query/pending? query-result)
          [:div.buckit--spinner]

          ; otherwise we haven't issued the request to load the transactions
          :else
          (dispatch query))))))

(defn transactions
  "context map:

  :account-id              ID of the account currently being worked on
  (required)

  :create-transaction?     Indicates whether the editor to create a new
  transaction should be shown.
  (optional -- default: false)

  :selected-transaction-id ID of the transaction highlighted or being edited
  (optional -- default: nil)

  :edit-selected?          Indicaes whether :selected-transaction-id is being
  edited
  (optional -- default: false)
  "
  [{:keys [account-id selected-transaction-id] :as context}]
  {:pre [(integer? account-id)
         (or (nil? selected-transaction-id) (integer? selected-transaction-id))]}
  [:div.container-fluid.buckit--transactions-view
   [:div.row [toolbar context]]
   [:div.row [ledger context]]])
