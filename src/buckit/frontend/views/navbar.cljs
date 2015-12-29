(ns buckit.frontend.views.navbar
  (:require [buckit.frontend.routes :as routes]
            [re-frame.core          :refer [subscribe]]))

(def ^:private sections [{:name "Transactions"
                          :href (routes/transactions-url)
                          :matches #{routes/account-transactions
                                     routes/account-transaction-details
                                     routes/account-transaction-create
                                     routes/account-transaction-edit
                                     routes/transactions}}
                         {:name "Accounts"
                          :href (routes/accounts-url)
                          :matches #{routes/accounts routes/account-details}}
                         {:name "Budget"
                          :href (routes/budget-url)
                          :matches #{routes/budget}}])

(defn- sections-ul
  [url-path]
  [:ul.nav.navbar-nav
   (doall (for [sec sections]
            ^{:key (:name sec)}
            [:li {:class (when (contains? (:matches sec) url-path)
                           "active")}
             [:a {:href (:href sec)} (:name sec)]]))])

(defn navbar
  []
  (let [url-path (subscribe [:url-path])]
    (fn
      []
      [:nav.buckit--navbar.navbar.navbar-inverse.navbar-fixed-top
       [:div.navbar-header [:span.navbar-brand "Buckit"]]
       [:div.navbar-right {:id "buckit-navbar-collapse"}
        [sections-ul @url-path]]])))
