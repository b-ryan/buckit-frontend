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
    (dispatch [:http-request {:query-id   :all-accounts
                              :method     :get-many
                              :model-type models/accounts}])
    (dispatch [:http-request {:query-id   :all-payees
                              :method     :get-many
                              :model-type models/payees}])
    buckit.db/initial-state))

(register-handler
  :url-changed
  (fn [db [_ url-path url-params]]
    (-> db
        (assoc buckit.db/url-path url-path)
        (assoc buckit.db/url-params url-params))))

(register-handler
  :http-request
  (fn [db [_ {:keys [query-id method model-type args]
              :or   {args []}
              :as   query}]]
    {:pre [(some? query-id)
           (backend/valid-method? method)
           (models/valid-model-type? model-type)
           (sequential? args)]}
    (go (let [response (<! (apply backend/request method model-type args))]
          (dispatch [:http-complete (assoc query :response response)])))
    (buckit.db/update-query db query-id #(db.query/create-pending query-id))))

(register-handler
  :http-complete
  (fn [db [_ {:keys [query-id method model-type response]
              :as   query}]]
    (let [body (:body response)
          objs (if (backend/returns-many? method)
                 (:objects body)
                 [body])
          db   (buckit.db/update-query db query-id db.query/set-complete response)]
      (if (:success response)
        (buckit.db/inject-resources db model-type objs)
        db))))
