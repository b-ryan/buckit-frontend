(ns buckit.frontend.handlers
  (:require-macros [cljs.core.async.macros      :refer [go]])
  (:require [buckit.frontend.db                 :as buckit.db]
            [buckit.frontend.db.query           :as db.query]
            [buckit.frontend.backend            :as backend]
            [buckit.frontend.models.core        :as models]
            [buckit.frontend.models.transaction :as models.transaction]
            [buckit.frontend.utils              :as utils]
            [cljs.core.async                    :refer [<!]]
            [cljs-http.client                   :as http]
            [re-frame.core                      :refer [dispatch path register-handler]]))

; FIXME "query" is being used for two different concepts. db.query should
; maybe be called db.query-result or something like that

(register-handler
  :initialize-db
  (fn [& _]
    (dispatch [:http-request {:query-id :all-accounts
                              :method   :get-many
                              :resource models/accounts}])
    (dispatch [:http-request {:query-id :all-payees
                              :method   :get-many
                              :resource models/payees}])
    buckit.db/initial-state))

(register-handler
  :url-changed
  (fn [db [_ url-path url-params]]
    (-> db
        (assoc buckit.db/url-path url-path)
        (assoc buckit.db/url-params url-params))))

(register-handler
  :http-request
  (fn [db [_ {:keys [query-id method resource args]
              :or   {args []}
              :as   query}]]
    {:pre [(some? query-id)
           (backend/valid-method? method)
           (models/valid-resource? resource)
           (sequential? args)]}
    (go (let [response (<! (apply backend/request method resource args))]
          (dispatch [:http-complete (assoc query :response response)])))
    (buckit.db/update-query db query-id db.query/set-pending)))

(register-handler
  :http-complete
  (fn [db [_ {:keys [query-id method resource response]
              :as   query}]]
    (let [body (:body response)
          objs (if (backend/returns-many? method)
                 (:objects body)
                 [body])
          db   (buckit.db/update-query db query-id db.query/set-complete response)]
      (if (:success response)
        (buckit.db/inject-resources db resource objs)
        db))))

(register-handler
  :save-transaction
  (fn [db [_ transaction :as query]]
    (go (let [response (<! (backend/save models/transactions transaction))]
          (dispatch [:transaction-save-complete query response])))
    (buckit.db/update-query db query db.query/set-pending)))

(register-handler
  :transaction-save-complete
  (fn [db [_ query response]]
    (js/console.log "response" (clj->js response))
    (buckit.db/update-query db query db.query/set-complete response)))
