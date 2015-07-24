(ns buckit.frontend.dev
  (:require [buckit.frontend.core :as core]
            [secretary.core :as secretary :refer-macros [defroute]]))

(enable-console-print!)

(defn on-js-reload []
  (core/main))

(core/main)
