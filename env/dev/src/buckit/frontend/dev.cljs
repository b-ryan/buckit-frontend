(ns buckit.frontend.dev
  (:require [buckit.frontend.core :as core]
            [figwheel.client :as figwheel :include-macros true]))

(enable-console-print!)

(defn on-js-reload []
  (core/main))

(core/setup-routes! core/app-state)
(core/main)
