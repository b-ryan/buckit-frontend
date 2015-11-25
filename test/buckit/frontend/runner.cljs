(ns buckit.frontend.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [buckit.frontend.models-test]))

(doo-tests 'buckit.frontend.models-test)
