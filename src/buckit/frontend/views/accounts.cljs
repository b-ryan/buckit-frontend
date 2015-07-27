(ns ^:figwheel-always buckit.frontend.views.accounts
  (:require [reagent.core :as reagant]
            [re-frame.core :refer [subscribe]]
            [secretary.core :as secretary]
            [buckit.frontend.routes :as routes]))

(defn account-selector
  "on-change-fn will be called with the account that is selected."
  [accounts current-id]
  ; FIXME this doesn't actually seem to respect the data. Once you have changed
  ; the selection, changing the data does not update the <select>
  (let [empty-text "-- Select an Account --"]
    [:select {:class "form-control"
              :defaultValue (or current-id empty-text)
              :on-change (fn [e]
                           (routes/go-to (routes/account-details
                                           {:account-id (.-target.value e)})))}
     (when-not current-id
       [:option {:disabled true} empty-text])
     (for [account @accounts]
       ^{:key (:id account)}
       [:option {:value (:id account)} (:name account)])]))

(defn accounts-view
  []
  (let [accounts (subscribe [:accounts])
        url-params (subscribe [:url-params])]
    (fn
      []
      [account-selector accounts (:account-id @url-params)])))
