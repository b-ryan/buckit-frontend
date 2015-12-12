(ns buckit.frontend.handlers
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [buckit.frontend.db :as buckit.db]
            [buckit.frontend.http :as http]
            [buckit.frontend.models.transaction :as models.transaction]
            [cljs.core.async :refer [<!]]
            [re-frame.core :refer [dispatch path register-handler]]))

(register-handler
  :initialize-db
  (fn [& _]
    (let [db buckit.db/initial-state]
      (doall
        (for [resource (:pending-initializations db)]
          (go (let [response (<! (http/get-many resource))]
                (dispatch [:resource-loaded resource response])))))
      db)))

(register-handler
  :resource-loaded
  ; TODO handle errors
  (fn [db [_ resource response]]
    (let [objs (-> response :body :objects)]
      (js/console.log (str (count objs) " object(s) loaded for resource " resource))
      (-> db
          (assoc resource (zipmap (map :id objs) objs))
          (update-in [:pending-initializations] disj resource)))))

(register-handler
  :url-changed
  (fn [db [_ url-path url-params]]
    (-> db
        (assoc :url-path url-path)
        (assoc :url-params url-params))))

(register-handler
  :update-transaction
  ; TODO handle errors
  (fn [db [_ transaction]]
    (let [transaction-id (models.transaction/id transaction)]
      (go (let [response (<! (http/put http/transactions transaction-id transaction))]
            (js/console.log (clj->js response))))
      (assoc-in db [http/transactions transaction-id] transaction))))
