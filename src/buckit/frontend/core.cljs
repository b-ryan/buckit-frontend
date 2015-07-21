(ns buckit.frontend.core
  (:require [reagent.core :as reagant]
            [secretary.core :as secretary :refer-macros [defroute]]
            [goog.events :as events]
            [buckit.frontend.views.buckit :refer [buckit-view active-section]])
  (:import goog.History
           goog.History.EventType))

(defn activate-section [section]
  (reset! active-section section))

(defn setup-routes! [state]

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

  (let [h (History.)]
    (goog.events/listen h EventType.NAVIGATE
                        (fn [event]
                          (secretary/dispatch! (.-token event))))
    (doto h (.setEnabled true))))

(defn main []
  (reagant/render-component [buckit-view]
                            (.getElementById js/document "buckit")))
