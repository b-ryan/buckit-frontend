(ns ^:figwheel-always buckit.frontend.views.buckit
  (:require [reagent.core :as reagant]))

(def sections [{:id :accounts :name "Accounts" :href "#/accounts"}
               {:id :budget :name "Budget" :href "#/budget"}])

(def active-section (reagant/atom nil))

(defn sections-ul []
  [:ul.nav.navbar-nav
   (doall (for [sec sections]
            ^{:key (:id sec)}
            [:li {:class (if (= (:id sec) @active-section) "active" nil)}
             [:a {:href (:href sec)} (:name sec)]]))])

(defn navbar-view []
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
     [sections-ul]]]])

(defmulti main-component
  "Will be dispatched based on the active section."
  identity)

(defmethod main-component :accounts
  [_]
  [:p "in accounts"])

(defmethod main-component :default
  [_]
  [:p "default"])

(defn buckit-view []
  [:div
   [navbar-view]
   [main-component @active-section]])
