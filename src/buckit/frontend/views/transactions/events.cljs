(ns buckit.frontend.views.transactions.events
  (:require [buckit.frontend.models.core                :as models]
            [buckit.frontend.models.transaction         :as models.transaction]
            [buckit.frontend.routes                     :as routes]
            [buckit.frontend.utils                      :as utils]
            [buckit.frontend.views.transactions.context :as ctx]
            [re-frame.core                              :refer [dispatch]]))

;TODO maybe these should be handlers?

(defn transaction-clicked-fn
  [context transaction]
  (let [transaction-id (models.transaction/id transaction)
        is-selected?   (ctx/is-selected? context transaction)
        new-context    (assoc context
                              :selected-transaction-id transaction-id
                              :edit?                   is-selected?)]
    (fn [e]
      (routes/go-to (routes/transactions-url
                      {:query-params (ctx/->url-params new-context)})))))

(defn new-transaction-clicked-fn
  [{:keys [account-id] :as context}]
  (let [new-context (assoc context
                           :selected-transaction-id nil
                           :edit?                   true)]
    (fn [e]
      (routes/go-to (routes/transactions-url
                      {:query-params (ctx/->url-params new-context)})))))

(defn editor-cancel-fn
  [{:keys [account-id] :as context} transaction]
  (let [new-context (assoc context :edit? false)]
    (fn [e]
      (.preventDefault e)
      (routes/go-to (routes/transactions-url
                      {:query-params (ctx/->url-params new-context)})))))

(defn editor-save-fn
  [form]
  {:pre [(some? form)]}
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

(defn new-account-chosen-fn
  [context]
  (fn [e]
    (.preventDefault e)
    (let [account-id  (-> e .-target .-value)
          new-context (assoc context :account-id account-id)]
      (routes/go-to (routes/transactions-url
                      {:query-params (ctx/->url-params new-context)})))))
