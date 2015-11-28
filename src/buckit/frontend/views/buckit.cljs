(ns buckit.frontend.views.buckit
  (:require [buckit.frontend.routes :as routes]
            [buckit.frontend.views.accounts :as views.accounts]
            [buckit.frontend.views.navbar :as views.navbar]
            [buckit.frontend.views.sidebar :as views.sidebar]
            [buckit.frontend.views.transactions :as views.transactions]
            [re-frame.core :refer [subscribe]]))

(defn- main-content
  [url-path]
  (condp = url-path
    routes/home [:p "welcome home"]
    routes/accounts [views.accounts/accounts]
    routes/account-details [views.accounts/account-details]
    routes/account-transactions [views.transactions/transactions]
    routes/budget [:p "budget"]
    [:p "404"]))

(defn buckit
  []
  (let [url-path (subscribe [:url-path])]
    (fn
      []
      [:div
       [views.navbar/navbar]
       [:div {:class "container-fluid"}
        [:div {:class "row"}
         [:div {:class "col-sm-2"} [views.sidebar/sidebar]]
         [:div {:class "col-sm-10"}
          (main-content @url-path)]]]])))
