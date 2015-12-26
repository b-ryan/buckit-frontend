(ns buckit.frontend.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [buckit.frontend.backend-test]
            [buckit.frontend.db-test]
            [buckit.frontend.views.core-test]))

(doo-tests 'buckit.frontend.backend-test
           'buckit.frontend.db-test
           'buckit.frontend.views.core-test)
