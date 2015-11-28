(ns buckit.frontend.views.transactions
  (:require [re-frame.core :refer [subscribe]]))

(defn transactions
  []
  (let [accounts (subscribe [:accounts])
        url-params (subscribe [:url-params])]
    (fn
      []
      [:p "transactions for account " (:account-id @url-params)])))
