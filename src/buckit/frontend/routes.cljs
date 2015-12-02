(ns buckit.frontend.routes
  (:require [secretary.core :as secretary :refer-macros [defroute]]
            [goog.events :as events]
            [re-frame.core :refer [dispatch dispatch-sync]])
  (:import goog.History
           goog.History.EventType))

(def home                        :home)
(def accounts                    :accounts)
(def account-details             :account-details)
(def account-transactions        :account-transactions)
(def account-transaction-details :account-transaction-details)
(def account-transaction-edit    :account-transaction-edit)
(def budget                      :budget)

(defn- ->int
  [x]
  (js/parseInt x))

(defroute home-url
  "/"
  []
  (dispatch [:url-changed home]))

(defroute accounts-url
  "/accounts"
  []
  (dispatch [:url-changed accounts]))

(defroute account-details-url
  "/accounts/:account-id"
  [account-id]
  (dispatch [:url-changed account-details
             {:account-id (->int account-id)}]))

(defroute account-transactions-url
  "/accounts/:account-id/transactions"
  [account-id]
  (dispatch [:url-changed account-transactions
             {:account-id (->int account-id)}]))

(defroute account-transaction-details-url
  "/accounts/:account-id/transactions/:transaction-id"
  [account-id transaction-id]
  (dispatch [:url-changed account-transaction-details
             {:account-id (->int account-id)
              :transaction-id (->int transaction-id)}]))

(defroute account-transaction-edit-url
  "/accounts/:account-id/transactions/:transaction-id/edit"
  [account-id transaction-id]
  (dispatch [:url-changed account-transaction-edit
             {:account-id (->int account-id)
              :transaction-id (->int transaction-id)}]))

(defroute budget-url
  "/budget"
  []
  (dispatch [:url-changed budget]))

(defroute
  "*"
  []
  (js/console.log "404"))

(secretary/set-config! :prefix "#")

(defonce history
  (doto (History.)
        (goog.events/listen EventType.NAVIGATE
                            (fn [event]
                              (secretary/dispatch! (.-token event))))
        ; The below enables the history polling loop and fires an initial event
        ; Note that the event listener above MUST be registered BEFORE calling
        ; .setEnabled so that we capture the first event
        (.setEnabled true)))

(defn go-to
  [url]
  (set! (.-location js/document) url))
