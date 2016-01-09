(ns buckit.frontend.ui
  (:require [buckit.frontend.models.account :as models.account]
            [buckit.frontend.models.payee   :as models.payee]
            [buckit.frontend.utils          :as utils]
            [re-frame.core                  :refer [subscribe]]
            [reagent.core                   :as reagent]))

(def initial-focus-wrapper
  (with-meta identity
    {:component-did-mount #(.focus (reagent/dom-node %))}))

(defn update-form-fn
  [form path]
  (fn [e]
    (swap! form assoc-in path (-> e .-target .-value))))

; TODO the below should maybe be in a different namespace
(defn date-selector
  [{:keys [class form path]}]
  [:input
   {:class       class
    :type        "text"
    :placeholder "Date"
    :value       (get-in @form path)
    :on-change   (update-form-fn form path)}])

(defn autocomplete
  [{:keys [items display-fn] :as opts}]
  (reagent/create-class
    {:reagent-render
     (fn
       [{:keys [class value on-change placeholder]
         :as   opts}]
       [:input {:class       class
                :type        "text"
                :on-change   on-change
                :placeholder placeholder}])

     :component-did-mount
     (fn
       [this]
       (.autocomplete (js/$ (reagent/dom-node this))
                      (clj->js {:source (map display-fn items)})))}))

(defn account-selector
  [opts]
  (let [accounts (subscribe [:accounts])]
    (fn
      [{:keys [class value on-change]
        :as   opts}]
      [:select
       {:class     class
        :type      "text"
        :value     (str value)
        :on-change on-change}
       (into (list ^{:key :empty} [:option {:value ""} "-All Accounts-"])
             (for [[account-id account] @accounts]
               ^{:key account-id}
               [:option {:key account-id :value account-id}
                (models.account/name account)]))])))

(defn account-editor
  [opts]
  (let [accounts (subscribe [:accounts])]
    (fn
      [{:keys [class form path placeholder]
        :as   opts}]
      [autocomplete {:class       class
                     :value       (get-in @form path)
                     :on-change   (update-form-fn form path)
                     :placeholder placeholder
                     :items       (vals @accounts)
                     :display-fn  models.account/name}])))

(defn payee-editor
  [opts]
  (let [payees (subscribe [:payees])]
    (fn
      [{:keys [class form path]
        :as   opts}]
      [autocomplete {:class       class
                     :value       (get-in @form path)
                     :on-change   (update-form-fn form path)
                     :placeholder "Payee"
                     :items       (vals @payees)
                     :display-fn  models.payee/name}])))
