(ns ^:figwheel-always buckit.frontend.views.accounts
  (:require [reagent.core :as reagant]))

(defonce accounts (reagant/atom [{:id 1 :name "Checking"}
                             {:id 2 :name "Savings"}]))

(defonce selected-account (reagant/atom nil))

(defn account-selector
  "on-change-fn will be called with the account that is selected."
  []
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
  [account-selector])
