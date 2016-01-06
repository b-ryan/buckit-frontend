(ns buckit.frontend.views.transactions
  (:require [buckit.frontend.ui                         :as ui]
            [buckit.frontend.utils                      :as utils]
            [buckit.frontend.views.transactions.events  :as events]
            [buckit.frontend.views.transactions.ledger  :as ledger]))

(defn- toolbar
  [context]
  [:div.row.buckit--transactions-toolbar
   [:div.col-xs-6.visible-xs-block
    [ui/account-selector
     {:class     "form-control"
      :value     (:account-id context)
      :on-change (events/new-account-chosen-fn context)}]]
   [:div.col-xs-6.col-sm-12
    [:button.btn.btn-default
     {:on-click (events/new-transaction-clicked-fn context)}
     "+ Transaction"]]])

(defn transactions
  [context]
  [:div.buckit--transactions-view
   [toolbar context]
   [ledger/ledger context]])
