(ns buckit.frontend.views.sidebar
  (:require [buckit.frontend.models.account :as models.account]
            [buckit.frontend.routes         :as routes]
            [re-frame.core                  :refer [subscribe]]))

(defn- show-account?
  [account]
  (contains? models.account/owned-account-types (:type account)))

(def ^:private matching-routes
  #{routes/transactions})

(defn sidebar
  []
  (let [accounts   (subscribe [:accounts])
        url-params (subscribe [:url-params])
        url-path   (subscribe [:url-path])]
    (fn
      []
      [:ul.nav.buckit--sidebar
       (doall
         (for [[account-id account] @accounts
               :when (show-account? account)
               :let [href    (routes/transactions-url {:query-params {:account_id account-id}})
                     active? (and (contains? matching-routes @url-path)
                                  (= account-id (:account_id @url-params)))]]
           ^{:key account-id}
           [:li {:class (when active? "active")}
            [:a {:href href} (:name account)
             [:span.sr-only (when active? "(current)")]]]))])))
