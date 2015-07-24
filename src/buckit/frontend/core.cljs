(ns buckit.frontend.core
  (:require [reagent.core :as reagant]
            [secretary.core :as secretary :refer-macros [defroute]]
            [goog.events :as events]
            [re-frame.core :refer [dispatch dispatch-sync]]
            [buckit.frontend.handlers]
            [buckit.frontend.subs]
            [buckit.frontend.views.buckit :refer [buckit-view]])
  (:import goog.History
           goog.History.EventType))

(defonce history (History.))

(defroute "/" []
  (dispatch [:activate-section nil])
  (js/console.log "you're home!"))

(defroute "/accounts" []
  (dispatch [:activate-section :accounts])
  (js/console.log "hi accounts"))

(defroute "/budget" []
  (dispatch [:activate-section :budget])
  (js/console.log "hi accounts"))

(defroute "*" []
  (js/console.log "404"))

(goog.events/listen history EventType.NAVIGATE
                    (fn [event]
                      (secretary/dispatch! (.-token event))))

; Enable the history polling loop and fire initial event:
(doto history (.setEnabled true))

(defonce initialization (dispatch-sync [:initialize-db]))

(defn main []
  (reagant/render-component [buckit-view]
                            (.getElementById js/document "buckit")))
