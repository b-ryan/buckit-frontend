(ns buckit.frontend.ui
  (:require [buckit.frontend.models.account :as models.account]
            [buckit.frontend.models.payee   :as models.payee]
            [buckit.frontend.utils          :as utils]
            [re-frame.core                  :refer [subscribe]]
            [reagent.core                   :as reagent]))

(def initial-focus-wrapper
  (with-meta identity
    {:component-did-mount #(.focus (reagent/dom-node %))}))

(defn input-change-fn
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
    :on-change   (input-change-fn form path)}])

(defn account-selector
  [opts]
  (let [accounts (subscribe [:accounts])]
    (fn
      [{:keys [class value on-change]}]
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
  [{:keys [form path] :as opts}]
  [account-selector (assoc opts
                           :value     (get-in @form path)
                           :on-change (input-change-fn form path))])

(defn payee-selector
  [opts]
  (let [payees (subscribe [:payees])]
    (fn
      [{:keys [class value on-change]}]
      [:select
       {:class     class
        :type      "text"
        :value     value
        :on-change on-change}
       (into (list ^{:key :empty} [:option])
             (for [[payee-id payee] @payees]
               ^{:key payee-id}
               [:option {:key payee-id :value payee-id}
                (models.payee/name payee)]))])))

(defn payee-editor
  [{:keys [form path] :as opts}]
  [payee-selector (assoc opts
                         :value     (get-in @form path)
                         :on-change (input-change-fn form path))])
