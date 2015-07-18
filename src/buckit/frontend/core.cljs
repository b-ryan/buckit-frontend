(ns buckit.frontend.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [secretary.core :as secretary :refer-macros [defroute]]
            [goog.events :as events]
            [buckit.frontend.views.buckit :refer [buckit-view]])
  (:import goog.History
           goog.History.EventType))

(defn activate-section [state section]
  (swap! state assoc :active-section section))

(defn setup-routes! [state]

  (defroute "/" []
    (activate-section state nil)
    (js/console.log "you're home!"))

  (defroute "/accounts" []
    (activate-section state :accounts)
    (js/console.log "hi accounts"))

  (defroute "/budget" []
    (activate-section state :budget)
    (js/console.log "hi accounts"))

  (defroute "*" []
    (js/console.log "404"))

  (let [h (History.)]
    (goog.events/listen h EventType.NAVIGATE
                        (fn [event]
                          (secretary/dispatch! (.-token event))))
    (doto h (.setEnabled true))))

(defonce app-state
  (atom {:sections [{:key :accounts :name "Accounts" :href "#/accounts"}
                    {:key :budget :name "Budget" :href "#/budget"}]}))

(defn main []
  (om/root buckit-view
           app-state
           {:target (. js/document
                       (getElementById "buckit"))}))
