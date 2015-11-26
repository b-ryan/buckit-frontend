(ns buckit.frontend.core
  (:require [reagent.core :as reagant]
            [re-frame.core :refer [dispatch dispatch-sync]]
            [buckit.frontend.handlers]
            [buckit.frontend.subs]
            [buckit.frontend.routes]
            [buckit.frontend.views.buckit :as views.buckit]))

(defn initialize-db
  []
  (dispatch-sync [:initialize-db]))

(defn main
  []
  (reagant/render-component [views.buckit/buckit]
                            (.getElementById js/document "buckit")))
