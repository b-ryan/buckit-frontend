(ns ^:figwheel-always buckit.frontend.views.navbar
  (:require [re-frame.core :refer [subscribe]]
            [reagent.core :as reagant]))

(def sections [{:id :accounts :name "Accounts" :href "#/accounts"}
               {:id :budget :name "Budget" :href "#/budget"}])

(defn sections-ul
  [url-path]
  [:ul.nav.navbar-nav
   (doall (for [sec sections]
            ^{:key (:id sec)}
            [:li {:class (if (= (:id sec) (first url-path))
                           "active"
                           nil)}
             [:a {:href (:href sec)} (:name sec)]]))])

(defn navbar
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
