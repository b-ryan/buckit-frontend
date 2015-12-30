(ns buckit.frontend.routes
  (:require [goog.events    :as events]
            [re-frame.core  :refer [dispatch dispatch-sync]]
            [secretary.core :as secretary :refer-macros [defroute]])
  (:import goog.History
           goog.History.EventType))

(def home                        :home)
(def accounts                    :accounts)
(def account-details             :account-details)
(def budget                      :budget)
(def transactions                :transactions)

(defn- ->int
  [x]
  (when x (js/parseInt x)))

(defn- ->boolean
  [x]
  (js/console.log x)
  (if x
    (not (contains? #{"false"} x))
    false))

(defroute home-url
  "/"
  []
  (dispatch [:url-changed home]))

(defroute accounts-url
  "/accounts"
  []
  (dispatch [:url-changed accounts]))

(defroute account-details-url
  "/accounts/:account_id"
  [account-id]
  (dispatch [:url-changed account-details
             {:account_id (->int account-id)}]))

(defroute budget-url
  "/budget"
  []
  (dispatch [:url-changed budget]))

(defroute transactions-url
  "/transactions"
  [query-params]
  (dispatch [:url-changed transactions
             (-> query-params
                 (update-in [:id] ->int)
                 (update-in [:account_id] ->int)
                 (update-in [:edit] ->boolean))]))

(defroute
  "*"
  []
  (js/console.log "404"))

(defn go-to
  [url]
  (set! (.-location js/document) url))

(secretary/set-config! :prefix "#")

(defn init!
  []
  (doto (History.)
        (goog.events/listen EventType.NAVIGATE
                            (fn [event]
                              (secretary/dispatch! (.-token event))))
        ; The below enables the history polling loop and fires an initial event
        ; Note that the event listener above MUST be registered BEFORE calling
        ; .setEnabled so that we capture the first event
        (.setEnabled true)))
