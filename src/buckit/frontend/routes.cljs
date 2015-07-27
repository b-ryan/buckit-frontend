(ns buckit.frontend.routes
  (:require [secretary.core :as secretary :refer-macros [defroute]]
            [goog.events :as events]
            [re-frame.core :refer [dispatch dispatch-sync]])
  (:import goog.History
           goog.History.EventType))

(defroute home
  "/"
  []
  (dispatch [:url-changed []]))

(defroute accounts
  "/accounts"
  []
  (dispatch [:url-changed [:accounts]]))

(defroute account-details
  "/accounts/:account-id"
  [account-id]
  (dispatch [:url-changed [:accounts] {:account-id (int account-id)}]))

(defroute budget
  "/budget"
  []
  (dispatch [:url-changed [:budget]]))

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
