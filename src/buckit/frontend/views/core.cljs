(ns buckit.frontend.views.core
  (:require [buckit.frontend.routes :as routes]
            [buckit.frontend.views.accounts :as views.accounts]
            [buckit.frontend.views.navbar :as views.navbar]
            [buckit.frontend.views.sidebar :as views.sidebar]
            [buckit.frontend.views.transactions :as views.transactions]
            [re-frame.core :refer [subscribe]]))

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
   (:account-id url-params)
   (:transaction-id url-params)])

(defmethod main-content routes/account-transaction-details
  [_ url-params]
  [views.transactions/transactions
   (:account-id url-params)
   (:transaction-id url-params)])

(defmethod main-content routes/account-transaction-edit
  [_ url-params]
  [views.transactions/transactions
   (:account-id url-params)
   (:transaction-id url-params)
   :edit-selected? true])

(defmethod main-content routes/budget
  [& args]
  [:p "budget"])

(defmethod main-content :default
  [& args]
  [:p "404"])

(defn main
  []
  (let [url-path   (subscribe [:url-path])
        url-params (subscribe [:url-params])]
    (fn
      []
      [:div
       [views.navbar/navbar]
       [:div.container-fluid
        [:div.row
         [:div.col-sm-2.buckit--sidebar-wrapper [views.sidebar/sidebar]]
         [:div.col-sm-10.col-sm-offset-2.buckit--main
          (main-content @url-path @url-params)]]]])))
