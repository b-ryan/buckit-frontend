(ns ^:figwheel-always buckit-frontend.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [buckit-frontend.routing :refer [app-state]]
            [buckit-frontend.navbar :refer [navbar-view]]))

(enable-console-print!)

(defn buckit [data owner]
  (reify
    om/IRender
    (render [this]
      (om/build navbar-view data))))

(om/root buckit
         app-state
         {:target (. js/document
                     (getElementById "buckit"))})


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
