(ns buckit.frontend.views.sidebar
  (:require [buckit.frontend.accounts :as accounts]
            [buckit.frontend.routes :as routes]
            [re-frame.core :refer [subscribe]]))

(defn- show-account?
  [account]
  (contains? accounts/owned-account-types (:type account)))

(defn sidebar
  []
  (let [accounts (subscribe [:accounts])
        url-params (subscribe [:url-params])]
    (fn
      []
      [:div {:class "buckit--sidebar"}
            (for [account @accounts :when (show-account? account)]
              ^{:key (:id account)}
              [:div [:a {:href (routes/account-transactions-url {:account-id (:id account)})}
                        (:name account)]])])))
