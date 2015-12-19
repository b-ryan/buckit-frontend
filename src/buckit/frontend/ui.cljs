(ns buckit.frontend.ui
  (:require [reagent.core :as reagent]))

(defn input-change-fn
  [form path]
  (fn [e]
    (swap! form assoc-in path (-> e .-target .-value))))

(def initial-focus-wrapper
  (with-meta identity
    {:component-did-mount #(.focus (reagent/dom-node %))}))
