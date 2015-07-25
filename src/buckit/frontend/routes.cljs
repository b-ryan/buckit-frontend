(ns buckit.frontend.routes
  (:require [secretary.core :as secretary :refer-macros [defroute]]
            [goog.events :as events]
            [re-frame.core :refer [dispatch dispatch-sync]]
            [buckit.frontend.handlers]
            [buckit.frontend.subs]
            [buckit.frontend.views.buckit :refer [buckit-view]])
  (:import goog.History
           goog.History.EventType))

(defroute "/" []
  (dispatch [:change-url-path []]))

(defroute "/accounts" []
  (dispatch [:change-url-path [:accounts]]))

(defroute "/accounts/:account-id" {account-id :account-id}
  (dispatch [:change-url-path [:accounts]])
  (dispatch [:change-url-params [{:account-id account-id}]]))

(defroute "/budget" []
  (dispatch [:change-url-path [:budget]]))

(defroute "*" []
  (js/console.log "404"))

(defonce history
  (doto (History.)
        (goog.events/listen EventType.NAVIGATE
                            (fn [event]
                              (secretary/dispatch! (.-token event))))
        ; The below enables the history polling loop and fires an initial event
        ; Note that the event listener above MUST be registered BEFORE calling
        ; .setEnabled so that we capture the first event
        (.setEnabled true)))
