(ns ^:figwheel-always buckit.frontend.views.navbar
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn- collapse-button []
  (dom/button #js {:type "button"
                   :className "navbar-toggle collapsed"
                   :data-toggle "collapse"
                   :data-target "#buckit-navbar-collapse"}
              (dom/span #js {:className "sr-only"} "Toggle navigation")
              (dom/span #js {:className "icon-bar"})
              (dom/span #js {:className "icon-bar"})
              (dom/span #js {:className "icon-bar"})))

(defn- brand []
  (dom/span #js {:className "navbar-brand"} "Buckit"))

(defn- navbar-header []
  (dom/div #js {:className "navbar-header"}
           (collapse-button)
           (brand)))

(defn- link [section active-section]
  (let [href (:href section)
        name (:name section)
        active? (= (:key section) active-section)
        className (if active? "active" nil)]
    (dom/li #js {:className className}
            (dom/a #js {:href href } name))))

(defn- links [{:keys [sections active-section]}]
  (dom/div #js {:className "collapse navbar-collapse"
                :id "buckit-navbar-collapse"}
           (apply dom/ul #js {:className "nav navbar-nav"}
                  (map #(link % active-section) sections))))

(defn navbar-view [data owner]
  (reify
    om/IRender
    (render [this]
      (dom/nav #js {:className "navbar navbar-default"}
               (dom/div #js {:className "container-fluid"}
                        (navbar-header)
                        (links data))))))
