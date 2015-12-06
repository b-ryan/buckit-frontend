(ns buckit.frontend.core
  (:require [reagent.core :as reagant]
            [re-frame.core :refer [dispatch dispatch-sync]]
            [buckit.frontend.handlers]
            [buckit.frontend.subs]
            [buckit.frontend.routes]
            [buckit.frontend.views.core :as views.core]))

(defn init!
  []
  (dispatch-sync [:initialize-db])
  (buckit.frontend.routes/init!))

(defn main
  []
  (reagant/render-component [views.core/main]
                            (.getElementById js/document "buckit")))
