(ns buckit.frontend.views.sidebar
  (:require [buckit.frontend.routes :as routes]
            [re-frame.core :refer [subscribe]]))

(defn sidebar
  []
  (let [accounts (subscribe [:accounts])
        url-params (subscribe [:url-params])]
    (fn
      []
      [:div {:class "buckit--sidebar"}
            (for [account @accounts]
              ^{:key (:id account)}
              [:div [:a {:href (routes/account-transactions-url {:account-id (:id account)})}
                        (:name account)]])])))
