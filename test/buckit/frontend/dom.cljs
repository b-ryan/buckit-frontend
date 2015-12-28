(ns buckit.frontend.dom
  (:require [reagent.core :as reagent]))

(defn add-test-div
  []
  (let [body (.-body js/document)
        div  (.createElement js/document "div")]
    (.appendChild body div)
    div))

(defn with-mounted-component
  [component f]
  (let [div (add-test-div)]
    (reagent/render-component component div #(f component div))
    (reagent/unmount-component-at-node div)
    (reagent/flush)
    (.removeChild (.-body js/document) div)))

(defn with-subscriptions
  [subscriptions f]
  (assert (map? subscriptions))
  (with-redefs [re-frame.core/subscribe
                (fn [[k]]
                  (get subscriptions k))]
    (f)))

(defn mounted-with-subs
  [component subscriptions f]
  (with-subscriptions
    subscriptions
    #(with-mounted-component component f)))

(defn found-in
  [re div]
  (let [res (.-innerHTML div)]
    (if (re-find re res)
      true
      (do (println "Not found: " res)
          false))))
