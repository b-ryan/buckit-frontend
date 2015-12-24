(ns buckit.frontend.backend-test
  (:require [buckit.frontend.backend :as backend]
            [cljs.test :refer-macros [deftest is testing run-tests]]))

(deftest returns-many?
  (is (= true (backend/returns-many? :get-many))))

(deftest returns-one?
  (is (= true (backend/returns-one? :get-one))))
