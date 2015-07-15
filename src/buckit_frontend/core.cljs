(ns ^:figwheel-always buckit-frontend.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [buckit-frontend.navbar :refer [navbar-view]]))

(enable-console-print!)

(defonce app-state
  (atom {:text "Hello Buck!"
         :navbar {:sections [{:name "Accounts"
                              :href "/accounts"}
                             {:name "Budget"
                              :href "/budget"}]
                  :active-name nil}}))

(defn buckit [data owner]
  (reify
    om/IRender
    (render [this]
      (om/build navbar-view (:navbar data)))))

(om/root buckit app-state {:target (. js/document (getElementById "buckit"))})


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
