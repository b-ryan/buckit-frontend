(ns buckit.frontend.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [buckit.frontend.db :as buckit.db]
            [buckit.frontend.models.core :as models]
            [buckit.frontend.utils :as utils]
            [re-frame.core :refer [register-sub subscribe]]))

(register-sub
  :url-path
  (fn [db _]
    (reaction (buckit.db/url-path @db))))

(register-sub
  :url-params
  (fn [db _]
    (reaction (buckit.db/url-params @db))))

(register-sub
  :accounts
  (fn [db _]
    (reaction (buckit.db/get-resource @db models/accounts))))

(register-sub
  :payees
  (fn [db _]
    (reaction (buckit.db/get-resource @db models/payees))))

(register-sub
  :transactions
  (fn [db _]
    (reaction (buckit.db/get-resource @db models/transactions))))

(register-sub
  :account-transactions
  (fn [db [_ account-id]]
    {:pre [(integer? account-id)]}
    (let [transactions (subscribe [:transactions])]
      (reaction (let [r (utils/filter-map-by-v (partial models/account-in-splits? account-id)
                        @transactions)]
                  (js/console.log "account-id" (clj->js account-id))
                  (js/console.log "r" (clj->js r))
                  r)))))

(register-sub
  :pending-initializations
  (fn [db _]
    (reaction (buckit.db/pending-initializations @db))))

(register-sub
  :queries
  (fn [db _]
    (reaction (buckit.db/queries @db))))
