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

(def columns
  [{:name            "Date"
    :width-on-mobile 4
    :width-normal    2}
   {:name            "Payee"
    :width-on-mobile 0
    :width-normal    2}
   {:name            "Category"
    :width-on-mobile 4
    :width-normal    3}
   {:name            "Memo"
    :width-on-mobile 0
    :width-normal    3}
   {:name            "Amount"
    :width-on-mobile 4
    :width-normal    2}])

(defn- col-width->class
  [width size]
  (if (> width 0)
    (str "col-" size "-" width)
    (str "hidden-" size)))

(defn- column-class
  [column]
  (str (col-width->class (:width-on-mobile column) "xs")
       " "
       (col-width->class (:width-normal column) "sm")))

(defn- header
  [columns]
  [:div.container-fluid
   [:div.row.buckit--ledger-header
    (for [column columns]
      ^{:key (:name column)}
      [:span {:class (column-class column)} (:name column)])]])

(defmulti display-value (fn [_ _ column] (:name column)))

(defmethod display-value "Date"
  [context transaction _]
  (:date transaction))

(defmethod display-value "Payee"
  [context transaction _]
  (->> transaction
       models.transaction/payee-id
       (get @(:payees context))
       models.payee/name))

(defmethod display-value "Category"
  [context transaction _]
  (let [other-splits (ctx/other-splits context transaction)
        accounts     (:accounts context)]
    (if (> (count other-splits) 1)
      "Splits"
      (->> other-splits
           first
           models.split/account-id
           (get @accounts)
           models.account/name))))

(defmethod display-value "Memo"
  ; FIXME
  [& _]
  "")

(defn- amount-glyphicon
  "Returns a left or right arrow glyphicon to show whether money left the
  account or is entering it."
  [amount]
  (let [expense? (< amount 0)]
    (str "glyphicon "
         (if expense? "glyphicon-arrow-right" "glyphicon-arrow-left")
         " "
         (if expense? "expense" "income"))))

(defmethod display-value "Amount"
  [context transaction _]
  (let [amount (:amount (ctx/main-split context transaction))]
    [:span {:class (amount-glyphicon amount)
            :aria-hidden true}
     ; FIXME other currencies?
     (str " $" (js/Math.abs amount))]))

(defn- read-only-row
  [context transaction columns]
  (let [accounts (subscribe [:accounts])
        payees   (subscribe [:payees])]
    (fn
      [context transaction columns]
      (let [context (assoc context
                           :accounts accounts
                           :payees   payees)]
        [:div.row
         {:on-click (events/transaction-clicked-fn context transaction)}
         (doall (for [column columns]
                  ^{:key (:name column)}
                  [:span
                   {:class (column-class column)}
                   (display-value context transaction column)]))]))))

(defn- ledger-row
  [context transaction columns]
  (let [is-selected? (ctx/is-selected? context transaction)]
    [:div.container-fluid.buckit--ledger-row
     {:class (when is-selected? "active")}
     (if (and is-selected? (:edit? context))
       [editor/editor context transaction]
       [read-only-row context transaction columns])]))

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
            transactions     (ctx/filter-transactions context (vals @transactions))
            ; FIXME alter for :no-account mode
            columns          columns]
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
           [header columns]
           (doall
             (for [transaction transactions]
               ^{:key (models.transaction/id transaction)}
               [ledger-row context transaction columns]))
           (when (and (not selected-transaction-id) (:edit? context))
             [:div.container-fluid.buckit--ledger-row.active
              [editor/editor context (models.transaction/create account-id)]])])))))
