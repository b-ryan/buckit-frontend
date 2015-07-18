(ns ^:figwheel-always buckit.frontend.views.buckit
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [buckit.frontend.views.navbar :refer [navbar-view]]))

(defmulti main-component :active-section)

(defmethod main-component :accounts
  [data]
  (dom/p nil "in accounts"))

(defmethod main-component :default
  [data]
  (dom/p nil "default"))

(defn buckit-view [data owner]
  (reify
    om/IRender
    (render [this]
      (dom/div nil
               (om/build navbar-view data)
               (main-component data)))))
