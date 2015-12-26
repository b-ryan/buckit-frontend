(ns buckit.frontend.views.core-test
  (:require [buckit.frontend.routes      :as routes]
            [buckit.frontend.views.core  :as views.core]
            [cljs.test                   :refer-macros [deftest is testing run-tests]]
            [reagent.core                :as reagent]))

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


(deftest load-failure
  (let [url-path   (atom routes/account-transactions)
        url-params (atom {:account-id 1})
        queries    (atom {})]
    (with-redefs [re-frame.core/subscribe (fn [[k]] (condp = k
                                                      :url-path   url-path
                                                      :url-params url-params
                                                      :queries    queries))]
      (with-mounted-component (views.core/main)
        (fn [component div]
          (is (> (.-length (.getElementsByClassName js/document "buckit--loading-overlay"))
                 0)))))))
