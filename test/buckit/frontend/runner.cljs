(ns buckit.frontend.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [buckit.frontend.backend-test]
            [buckit.frontend.db-test]))

(doo-tests 'buckit.frontend.backend-test
           'buckit.frontend.db-test)
