(ns buckit.frontend.handlers
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [buckit.frontend.db :as buckit.db]
            [buckit.frontend.http :as http]
            [cljs.core.async :refer [<!]]
            [re-frame.core :refer [dispatch path register-handler]]))

(register-handler
  :initialize-db
  (fn [db _]
    (doall
      (for [resource [http/accounts
                      http/payees
                      http/transactions]]
        (go (let [response (<! (http/query resource))]
              (dispatch [:resource-loaded resource response])))))
    buckit.db/initial-state))

(register-handler
  :resource-loaded
  (fn [db [_ resource response]]
    (let [objs (-> response :body :objects)]
      (js/console.log (str (count objs) " object(s) loaded for resource " resource))
      (js/console.log (clj->js objs))
      (assoc db resource (zipmap (map :id objs) objs)))))

(register-handler
  :url-changed
  (fn [db [_ url-path url-params]]
    (-> db
        (assoc :url-path url-path)
        (assoc :url-params url-params))))

(register-handler
  :update-transaction
  (fn [db [_ transaction]]
    (assoc-in db [:transactions (:id transaction)] transaction)))
