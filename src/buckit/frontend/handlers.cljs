(ns buckit.frontend.handlers
  (:require-macros [cljs.core.async.macros      :refer [go]])
  (:require [buckit.frontend.db                 :as buckit.db]
            [buckit.frontend.db.query           :as db.query]
            [buckit.frontend.http               :as http]
            [buckit.frontend.models.core        :as models]
            [buckit.frontend.models.transaction :as models.transaction]
            [buckit.frontend.utils              :as utils]
            [cljs.core.async                    :refer [<!]]
            [re-frame.core                      :refer [dispatch path register-handler]]))

(register-handler
  :initialize-db
  (fn [& _]
    (let [db buckit.db/initial-state]
      (doall
        (for [resource (buckit.db/pending-initializations db)]
          (go (let [response (<! (http/get-many resource))]
                (dispatch [:resource-loaded resource response])))))
      db)))

(register-handler
  :resource-loaded
  ; TODO handle errors
  (fn [db [_ resource response]]
    (let [objs (-> response :body :objects)]
      (-> db
          (buckit.db/inject-resources resource objs)
          (buckit.db/complete-initialization resource)))))

(register-handler
  :url-changed
  (fn [db [_ url-path url-params]]
    (-> db
        (assoc buckit.db/url-path url-path)
        (assoc buckit.db/url-params url-params))))

(register-handler
  :load-account-transactions
  (fn [db [_ account-id :as query]]
    (go (let [response (<! (http/get-many http/transactions
                                          {:filters [{:name "splits__account_id"
                                                      :op "any"
                                                      :val account-id}]}))]
          (dispatch [:transactions-loaded query response])))
    (buckit.db/update-query db query {db.query/status db.query/pending-status})))

(register-handler
  :transactions-loaded
  ; TODO handle errors
  (fn [db [_ query response]]
    (let [transactions (-> response :body :objects)]
            (js/console.log (clj->js transactions))
      (-> db
          (buckit.db/update-query query {db.query/status db.query/complete-status})
          (buckit.db/inject-resources http/transactions transactions)))))

(register-handler
  :update-transaction
  ; TODO handle errors
  (fn [db [_ transaction]]
    (let [transaction-id (models.transaction/id transaction)]
      (go (let [response (<! (http/put http/transactions transaction-id transaction))]
            (js/console.log (clj->js response))))
      (assoc-in db [http/transactions transaction-id] transaction))))
