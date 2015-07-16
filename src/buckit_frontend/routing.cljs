(ns buckit-frontend.routing
  (:require [secretary.core :as secretary :refer-macros [defroute]]
            [goog.events :as events])
  (:import goog.History
           goog.History.EventType))

(defonce app-state
  (atom {:sections [{:key :accounts :name "Accounts" :href "#/accounts"}
                    {:key :budget :name "Budget" :href "#/budget"}]}))

(defn activate-section [section]
  (let [set-active #(assoc % :active (= (:key %) section))]
    (swap! app-state update-in [:sections] #(map set-active %))))

(defroute "/" []
  (activate-section nil)
  (js/console.log "you're home!"))

(defroute "/accounts" []
  (activate-section :accounts)
  (js/console.log "hi accounts"))

(defroute "/budget" []
  (activate-section :budget)
  (js/console.log "hi accounts"))

(defroute "*" []
  (js/console.log "404"))

(defn on-navigate [event]
  (js/console.log (.-token event))
  (secretary/dispatch! (.-token event)))

(let [h (History.)]
  (goog.events/listen h EventType.NAVIGATE on-navigate)
  (doto h (.setEnabled true)))
