(ns buckit.frontend.views.navbar
  (:require [buckit.frontend.routes :as routes]
            [re-frame.core :refer [subscribe]]))

(def ^:private sections [{:name "Accounts"
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
            [:li {:class (if (contains? (:matches sec) url-path)
                           "active"
                           nil)}
             [:a {:href (:href sec)} (:name sec)]]))])

; FIXME collapse button doesn't seem to work
(defn navbar
  []
  (let [url-path (subscribe [:url-path])]
    (fn
      []
      [:nav.navbar.navbar-inverse.navbar-fixed-top
       [:div.navbar-header
        [:button.navbar-toggle.collapsed {:type "button"
                                          :data-toggle "collapse"
                                          :data-target "buckit-navbar-collapse"}
         [:span.sr-only "Toggle navigation"]
         [:span.icon-bar]
         [:span.icon-bar]
         [:span.icon-bar]]
        [:span.navbar-brand "Buckit"]]
       [:div.collapse.navbar-collapse.navbar-right {:id "buckit-navbar-collapse"}
        [sections-ul @url-path]]])))
