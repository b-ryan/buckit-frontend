(ns buckit.frontend.views.core
  (:require [buckit.frontend.db.query           :as db.query]
            [buckit.frontend.i18n               :as i18n]
            [buckit.frontend.routes             :as routes]
            [buckit.frontend.views.accounts     :as views.accounts]
            [buckit.frontend.views.navbar       :as views.navbar]
            [buckit.frontend.views.sidebar      :as views.sidebar]
            [buckit.frontend.views.transactions :as views.transactions]
            [re-frame.core                      :refer [subscribe]]))

(defmulti main-content
  (fn [route] route))

(defmethod main-content routes/home
  [& args]
  [:p "welcome home"])

(defmethod main-content routes/accounts
  [& args]
  [views.accounts/accounts])

(defmethod main-content routes/account-details
  [& args]
  [views.accounts/account-details])

(defmethod main-content routes/account-transactions
  [_ url-params]
  [views.transactions/transactions
   {:account-id              (:account-id url-params)}])

(defmethod main-content routes/account-transaction-create
  [_ url-params]
  [views.transactions/transactions
   {:account-id              (:account-id url-params)
    :create-transaction?     true}])

(defmethod main-content routes/account-transaction-details
  [_ url-params]
  [views.transactions/transactions
   {:account-id              (:account-id url-params)
    :selected-transaction-id (:transaction-id url-params)}])

(defmethod main-content routes/account-transaction-edit
  [_ url-params]
  [views.transactions/transactions
   {:account-id              (:account-id url-params)
    :selected-transaction-id (:transaction-id url-params)
    :edit-selected?          true}])

(defmethod main-content routes/budget
  [& args]
  [:p "budget"])

(defmethod main-content :default
  [& args]
  [:p "404"])

(defn main
  []
  (let [url-path   (subscribe [:url-path])
        url-params (subscribe [:url-params])
        queries    (subscribe [:queries])]
    (fn
      []
      (let [acc-result  (get @queries :all-accounts)
            pay-result  (get @queries :all-payees)
            all-results [acc-result pay-result]]
        [:div
         [views.navbar/navbar]
         (if (every? db.query/complete? all-results)
           (if (some db.query/failed? all-results)
             [:div [:p.text-danger i18n/init-error-message]]
             [:div.container-fluid
              [:div.row
               [:div.col-sm-2.buckit--sidebar-wrapper [views.sidebar/sidebar]]
               [:div.col-sm-10.col-sm-offset-2.buckit--main
                (main-content @url-path @url-params)]]])
           [:div.buckit--loading-overlay [:div.buckit--spinner.center-block]])]))))
