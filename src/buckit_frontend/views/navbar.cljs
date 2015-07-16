(ns ^:figwheel-always buckit-frontend.views.navbar
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

(defn- link [section]
  (let [href (:href section)
        name (:name section)
        className (if (:active section) "active" nil)]
    (dom/li #js {:className className}
            (dom/a #js {:href href } name))))

(defn- links [{:keys [sections]}]
  (dom/div #js {:className "collapse navbar-collapse"
                :id "buckit-navbar-collapse"}
           (apply dom/ul #js {:className "nav navbar-nav"}
                  (map link sections))))

(defn- navbar-header []
  (dom/div #js {:className "navbar-header"}
           (collapse-button)
           (brand)))

(defn- inject-active-key [sections active-section]
  (map #(if (= active-section (:name %))
          (assoc % :active true)
          %)
       sections))

(defn navbar-view [data owner]
  (reify
    om/IRender
    (render [this]
      (dom/nav #js {:className "navbar navbar-default"}
               (dom/div #js {:className "container-fluid"}
                        (navbar-header)
                        (links data))))))
