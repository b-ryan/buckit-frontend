(ns ^:figwheel-always buckit.frontend.views.accounts
  (:require [reagent.core :as reagant]
            [re-frame.core :refer [subscribe]]))

(defn- selector-on-change-fn
  [selected-account accounts]
  )

(defn account-selector
  "on-change-fn will be called with the account that is selected."
  [selected-account accounts]
  [:select {:on-change (fn [e]
                         (let [account-id (int (.-target.value e))
                               account (first (filter #(= account-id (:id %))
                                                      @accounts))]
                           (reset! selected-account account)))}
   (doall (for [account @accounts]
            ^{:key (:id account)}
            [:option {:value (:id account)} (:name account)]))])

(defn accounts-view
  []
  (let [accounts (subscribe [:accounts])
        selected-account (reagant/atom nil)]
    (fn
      []
      [account-selector selected-account])))
