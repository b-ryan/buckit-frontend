(ns buckit.frontend.views.core-test
  (:require [buckit.frontend.dom         :as dom]
            [buckit.frontend.routes      :as routes]
            [buckit.frontend.views.core  :as views.core]
            [cljs.test                   :refer-macros [deftest is testing run-tests]]))

(deftest load-failure
  (dom/with-subscriptions
    {:url-path   (atom routes/account-transactions)
     :url-params (atom {:account-id 1})
     :queries    (atom {})}
    #(dom/with-mounted-component (views.core/main)
       (fn [component div]
         (is (> (.-length (.getElementsByClassName div "buckit--loading-overlay"))
                0))))))
