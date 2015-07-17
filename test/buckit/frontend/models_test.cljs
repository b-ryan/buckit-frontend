(ns buckit.frontend.models-test
  (:require-macros [cemerick.cljs.test
                    :refer [is deftest with-test run-tests testing test-var]])
  (:require [cemerick.cljs.test :as t]
            [buckit.frontend.models :as m]))

(deftest url
         (is (= "http://localhost:8080/api/accounts"
                (m/url "accounts"))))
