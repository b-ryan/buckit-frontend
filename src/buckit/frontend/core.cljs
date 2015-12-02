(ns buckit.frontend.core
  (:require [reagent.core :as reagant]
            [re-frame.core :refer [dispatch dispatch-sync]]
            [buckit.frontend.handlers]
            [buckit.frontend.subs]
            [buckit.frontend.routes]
            [buckit.frontend.views.core :as views.core]))

(defn initialize-db
  []
  (dispatch-sync [:initialize-db]))

(defn main
  []
  (reagant/render-component [views.core/main]
                            (.getElementById js/document "buckit")))
