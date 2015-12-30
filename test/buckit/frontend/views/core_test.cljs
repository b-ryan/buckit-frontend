(ns buckit.frontend.views.core-test
  (:require [buckit.frontend.dom         :as dom]
            [buckit.frontend.routes      :as routes]
            [buckit.frontend.views.core  :as views.core]
            [cljs.test                   :refer-macros [deftest is testing run-tests]]))

(deftest load-in-progress
  (dom/mounted-with-subs
    [views.core/main]
    {:url-path   (atom routes/transactions)
     :url-params (atom {:account_id 1})
     :queries    (atom {})}
    (fn [component div]
      (is (> (.-length (.getElementsByClassName div "buckit--loading-overlay"))
             0)))))

(deftest load-failure
  (dom/mounted-with-subs
    [views.core/main]
    {:url-path   (atom routes/transactions)
     :url-params (atom {:account_id 1})
     :queries    (atom {:all-payees {:query-id :all-payees
                                     :status   :complete
                                     :response {:success false}}})}
    (fn [component div]
      (is (> (.-length (.getElementsByClassName div "text-danger"))
             0)))))
