(ns ^:figwheel-always buckit-frontend.views.buckit
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [buckit-frontend.views.navbar :refer [navbar-view]]))

(defn buckit-view [data owner]
  (reify
    om/IRender
    (render [this]
      (om/build navbar-view data))))
