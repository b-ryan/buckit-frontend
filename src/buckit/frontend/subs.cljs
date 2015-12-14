(ns buckit.frontend.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [buckit.frontend.db          :as buckit.db]
            [buckit.frontend.models.core :as models]
            [buckit.frontend.utils       :as utils]
            [re-frame.core               :refer [register-sub subscribe]]))

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
  :pending-initializations
  (fn [db _]
    (reaction (buckit.db/pending-initializations @db))))

(register-sub
  :queries
  (fn [db _]
    (reaction (buckit.db/queries @db))))
