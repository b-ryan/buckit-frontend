(ns buckit.frontend.core
  (:require [reagent.core :as reagant]
            [re-frame.core :refer [dispatch dispatch-sync]]
            [buckit.frontend.handlers]
            [buckit.frontend.subs]
            [buckit.frontend.routes]
            [buckit.frontend.views.buckit :refer [buckit-view]]))

(defn initialize-db
  []
  (dispatch-sync [:initialize-db]))

(defn main []
  (reagant/render-component [buckit-view]
                            (.getElementById js/document "buckit")))
