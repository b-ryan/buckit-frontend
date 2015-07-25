(ns ^:figwheel-always buckit.frontend.views.buckit
  (:require [reagent.core :as reagant]
            [buckit.frontend.views.accounts :as accounts]
            [re-frame.core :refer [subscribe]]))

(def sections [{:id :accounts :name "Accounts" :href "#/accounts"}
               {:id :budget :name "Budget" :href "#/budget"}])

(defn sections-ul
  [url-path]
  [:ul.nav.navbar-nav
   (doall (for [sec sections]
            ^{:key (:id sec)}
            [:li {:class (if (= (:id sec) (first @url-path))
                           "active"
                           nil)}
             [:a {:href (:href sec)} (:name sec)]]))])

(defn navbar-view
  [url-path]
  [:nav.navbar.navbar-default
   [:div.container-fluid
    [:div.navbar-header
     [:button.navbar-toggle.collapsed {:type "button"
                                       :data-toggle "collapse"
                                       :data-target "buckit-navbar-collapse"}
      [:span.sr-only "Toggle navigation"]
      [:span.icon-bar]
      [:span.icon-bar]
      [:span.icon-bar]]
     [:span.navbar-brand "Buckit"]]
    [:div.collapse.navbar-collapse {:id "buckit-navbar-collapse"}
     [sections-ul url-path]]]])

(defn main-view
  [url-path]
  (case (first @url-path)
    :accounts [accounts/accounts-view]
    [:p "default"]))

(defn buckit-view
  []
  (let [url-path (subscribe [:url-path])]
    (fn buckit-view-render
      []
      [:div
       [navbar-view url-path]
       [main-view url-path]])))
