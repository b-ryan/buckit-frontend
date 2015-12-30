(ns buckit.frontend.views.transactions
  (:require [buckit.frontend.utils                      :as utils]
            [buckit.frontend.views.transactions.events  :as events]
            [buckit.frontend.views.transactions.ledger  :as ledger]))

(defn- toolbar
  [context]
  [:div.buckit--transactions-toolbar
   [:button.btn.btn-default
    {:on-click (events/new-transaction-clicked-fn context)}
    "+ Transaction"]])

(defn transactions
  [context]
  [:div.container-fluid.buckit--transactions-view
   [:div.row [toolbar context]]
   [:div.row [ledger/ledger context]]])
