(ns buckit.frontend.http-test
  (:require [buckit.frontend.http :as m]
            [cljs.test :refer-macros [deftest is testing run-tests]]))

(deftest url
  (is (= "http://localhost:8080/api/accounts" (m/url :accounts))))
