(ns buckit.frontend.views.accounts
  (:require [re-frame.core :refer [subscribe]]
            [buckit.frontend.routes :as routes]))

(defn accounts
  []
  (let [accounts (subscribe [:accounts])]
    (fn
      []
      [:div])))

(defn account-details
  []
  (let [accounts (subscribe [:accounts])
        url-params (subscribe [:url-params])]
    (fn
      []
      [:div])))
