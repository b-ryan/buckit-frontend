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
    (go (let [response (<! (http/get-many models/transactions
                                          {:filters [{:name "splits__account_id"
                                                      :op "any"
                                                      :val account-id}]}))]
          (dispatch [:transactions-loaded query response])))
    (buckit.db/pending-query db query)))

(register-handler
  :transactions-loaded
  ; TODO handle errors
  (fn [db [_ query response]]
    (let [transactions (-> response :body :objects)]
      (-> db
          (buckit.db/completed-query query)
          (buckit.db/inject-resources models/transactions transactions)))))

(defn- save-transaction
  [db query response-chan]
  (go (let [response (<! response-chan)]
        (dispatch [:transaction-save-complete query response])))
  (buckit.db/pending-query db query))

(register-handler
  :create-transaction
  (fn [db [_ transaction :as query]]
    (save-transaction db query (http/post models/transactions transaction))))

(register-handler
  :update-transaction
  (fn [db [_ transaction :as query]]
    (save-transaction db query (http/put models/transactions transaction))))

(register-handler
  :transaction-save-complete
  ; TODO handle errors
  (fn [db [_ query response]]
    (js/console.log (clj->js query) (clj->js response))
    (buckit.db/completed-query db query)))
