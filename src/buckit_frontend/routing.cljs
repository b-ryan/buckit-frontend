(ns buckit-frontend.routing
  (:require [secretary.core :as secretary :refer-macros [defroute]]
            [goog.events :as events])
  (:import goog.History
           goog.History.EventType))

(defonce app-state
  (atom {:sections [{:name "Accounts" :href "#/accounts"}
                    {:name "Budget" :href "#/budget"}]
         :active-section nil
         :url nil}))

(defroute "/" []
  (swap! app-state assoc :active-section nil)
  (js/console.log "you're home!"))

(defroute "/accounts" []
  (swap! app-state assoc :active-section "Accounts")
  (js/console.log "hi accounts"))

(defroute "*" []
  (js/console.log "404"))

(defn on-navigate [event]
  (secretary/dispatch! (.-token event)))

(let [h (History.)]
  (goog.events/listen h EventType.NAVIGATE on-navigate)
  (doto h (.setEnabled true)))
