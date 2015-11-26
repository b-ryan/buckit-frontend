(ns ^:figwheel-always buckit.frontend.views.buckit
  (:require [buckit.frontend.views.accounts :as views.accounts]
            [buckit.frontend.views.navbar :as views.navbar]
            [re-frame.core :refer [subscribe]]
            [reagent.core :as reagant]))

(defmulti content first)

(defmethod content :accounts
  [_]
  [views.accounts/accounts])

(defmethod content :default
  [_]
  [:p "default"])

(defn buckit
  []
  (let [url-path-atom (subscribe [:url-path])]
    (fn
      []
      (let [url-path @url-path-atom]
        [:div
         [views.navbar/navbar url-path]
         [content url-path]]))))
