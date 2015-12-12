(ns buckit.frontend.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]))

(register-sub
  :url-path
  (fn [db _]
    (reaction (:url-path @db))))

(register-sub
  :url-params
  (fn [db _]
    (reaction (:url-params @db))))

(register-sub
  :accounts
  (fn [db _]
    (reaction (:accounts @db))))

(register-sub
  :payees
  (fn [db _]
    (reaction (:payees @db))))

(register-sub
  :transactions
  (fn [db _]
    (reaction (:transactions @db))))

(register-sub
  :pending-initializations
  (fn [db _]
    (reaction (:pending-initializations @db))))
