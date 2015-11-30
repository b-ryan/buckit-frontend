(ns buckit.frontend.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]))

(defn- lookup-sub
  "Returns a function that can be used for a subscription that creates a lookup
  map of an underlying list."
  [k]
  (fn [db _]
    (let [items   (reaction (k @db))
          grouped (group-by :id @items)
          ks      (keys grouped)
          vs      (map first (vals grouped))]
      ; TODO does dereferincing @items need to happen within the reaction to
      ; get the performance benefits?
      (reaction (zipmap ks vs)))))

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
  :accounts-by-id
  (lookup-sub :accounts))

(register-sub
  :payees
  (fn [db _]
    (reaction (:payees @db))))

(register-sub
  :payees-by-id
  (lookup-sub :payees))

(register-sub
  :transactions
  (fn [db _]
    (reaction (:transactions @db))))
