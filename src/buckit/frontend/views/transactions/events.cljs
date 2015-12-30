(ns buckit.frontend.views.transactions.events
  (:require [buckit.frontend.db.query           :as db.query]
            [buckit.frontend.i18n               :as i18n]
            [buckit.frontend.ui                 :as ui]
            [buckit.frontend.keyboard           :as keyboard]
            [buckit.frontend.models.account     :as models.account]
            [buckit.frontend.models.core        :as models]
            [buckit.frontend.models.split       :as models.split]
            [buckit.frontend.models.payee       :as models.payee]
            [buckit.frontend.models.transaction :as models.transaction]
            [buckit.frontend.routes             :as routes]
            [buckit.frontend.utils              :as utils]
            [re-frame.core                      :refer [dispatch subscribe]]
            [reagent.core                       :as reagent]))

;TODO maybe these should be handlers?

(defn transaction-clicked-fn
  [{:keys [account-id selected-transaction-id]} transaction]
  (let [transaction-id (models.transaction/id transaction)
        is-selected?   (= selected-transaction-id transaction-id)]
    (fn [e]
      (routes/go-to (routes/transactions-url
                      {:query-params {:id         transaction-id
                                      :account_id account-id
                                      :edit       is-selected?}})))))

(defn new-transaction-clicked-fn
  [{:keys [account-id]}]
  (fn [e]
    (routes/go-to (routes/transactions-url
                    {:query-params {:account_id account-id
                                    :edit       true}}))))

(defn editor-cancel-fn
  [{:keys [account-id] :as context} transaction]
  (let [base-params    {:account_id account-id}
        transaction-id (models.transaction/id transaction) 
        params         (if transaction-id
                         (assoc base-params :id transaction-id)
                         base-params)]
    (fn [e]
      (.preventDefault e)
      (routes/go-to (routes/transactions-url
                      {:query-params params})))))

(defn editor-save-fn
  [form]
  (fn [e]
    (.preventDefault e)
    (let [result      @form
          splits      (into [(:main-split result)]
                            (:other-splits result))
          transaction (-> result
                          :transaction
                          (assoc :splits splits))
          query-id    [:save-transaction (cljs.core/random-uuid)]
          query       {:query-id query-id
                       :method   :save
                       :resource models/transactions
                       :args     [transaction]}]
      (dispatch [:http-request query])
      (swap! form assoc
             :pending-query query-id
             :error         nil))))
