(ns buckit.frontend.views.transactions.ledger
  (:require [buckit.frontend.db.query                   :as db.query]
            [buckit.frontend.i18n                       :as i18n]
            [buckit.frontend.models.account             :as models.account]
            [buckit.frontend.models.core                :as models]
            [buckit.frontend.models.split               :as models.split]
            [buckit.frontend.models.payee               :as models.payee]
            [buckit.frontend.models.transaction         :as models.transaction]
            [buckit.frontend.routes                     :as routes]
            [buckit.frontend.utils                      :as utils]
            [buckit.frontend.views.transactions.context :as ctx]
            [buckit.frontend.views.transactions.editor  :as editor]
            [buckit.frontend.views.transactions.events  :as events]
            [re-frame.core                              :refer [dispatch subscribe]]))

(def ledger-header
  [:div.container-fluid
   [:div.row.buckit--ledger-header
    [:span.col-sm-2.col-xs-4 "Date"]
    [:span.col-sm-2.hidden-xs "Payee"]
    [:span.col-sm-3.col-xs-4 "Category"]
    [:span.col-sm-3.hidden-xs "Memo"]
    [:span.col-sm-2.col-xs-4 "Amount"]]])

(defn- account-to-show
  [context transaction accounts] ; FIXME make accounts part of context?
  (let [other-splits (ctx/other-splits context transaction)]
    (if (> (count other-splits) 1)
      "Splits"
      (->> other-splits
           first
           models.split/account-id
           (get accounts)
           models.account/name))))

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
  [context transaction]
  (let [amount (:amount (ctx/main-split context transaction))]
    [:span {:class (amount-glyphicon amount)
            :aria-hidden true}
     ; FIXME other currencies?
     (str " $" (js/Math.abs amount))]))

(defn- read-only-row
  [context transaction]
  (let [accounts (subscribe [:accounts])
        payees   (subscribe [:payees])]
    (fn
      [context transaction]
      [:div.row
       {:on-click (events/transaction-clicked-fn context transaction)}
       [:span.col-sm-2.col-xs-4 (:date transaction)]
       [:span.col-sm-2.hidden-xs (->> transaction
                                      models.transaction/payee-id
                                      (get @payees)
                                      models.payee/name)]
       [:span.col-sm-3.col-xs-4 (account-to-show context transaction @accounts)]
       [:span.col-sm-3.hidden-xs]
       [:span.col-sm-2.col-xs-4 (amount-to-show context transaction)]])))

(defn- ledger-row
  [context transaction]
  (let [is-selected? (ctx/is-selected? context transaction)]
    [:div.container-fluid.buckit--ledger-row
     {:class (when is-selected? "active")}
     (if (and is-selected? (:edit? context))
       [editor/editor context transaction]
       [read-only-row context transaction])]))

; (defn- )

(defn ledger
  [context]
  (let [queries      (subscribe [:queries])
        transactions (subscribe [:transactions])]
    (fn
      [{:keys [account-id selected-transaction-id] :as context}]
      {:pre [(utils/nil-or-integer? account-id)
             (utils/nil-or-integer? selected-transaction-id)]}
      (js/console.log "in ledger, context:" (clj->js context))
      (let [query            (ctx/transactions-query context)
            query-result     (get @queries (:query-id query))
            transactions     (ctx/filter-transactions context (vals @transactions))]
        (cond
          ; -----------------------------------------------------------------
          ; NO QUERY
          ; -----------------------------------------------------------------
          (nil? query-result)
          (do (js/setTimeout #(dispatch [:http-request query]))
              [:div.buckit--spinner])
          ; -----------------------------------------------------------------
          ; PENDING QUERY
          ; -----------------------------------------------------------------
          (db.query/pending? query-result)
          [:div.buckit--spinner]
          ; -----------------------------------------------------------------
          ; FAILED QUERY
          ; -----------------------------------------------------------------
          (db.query/failed? query-result)
          [:div [:p.text-danger i18n/transactions-not-loaded-error]]
          ; -----------------------------------------------------------------
          ; SUCCESSFUL QUERY
          ; -----------------------------------------------------------------
          (db.query/successful? query-result)
          [:div.buckit--ledger
           ledger-header
           (doall
             (for [transaction transactions]
               (with-meta
                 (ledger-row context transaction)
                 {:key (models.transaction/id transaction)})))
           (when (and (not selected-transaction-id) (:edit? context))
             [:div.container-fluid.buckit--ledger-row.active
              [editor/editor context (models.transaction/create account-id)]])])))))
