(ns buckit.frontend.views.transactions.editor
  (:require [buckit.frontend.db.query                  :as db.query]
            [buckit.frontend.i18n                      :as i18n]
            [buckit.frontend.ui                        :as ui]
            [buckit.frontend.keyboard                  :as keyboard]
            [buckit.frontend.models.account            :as models.account]
            [buckit.frontend.models.core               :as models]
            [buckit.frontend.models.split              :as models.split]
            [buckit.frontend.models.payee              :as models.payee]
            [buckit.frontend.models.transaction        :as models.transaction]
            [buckit.frontend.routes                    :as routes]
            [buckit.frontend.utils                     :as utils]
            [buckit.frontend.views.transactions.events :as events]
            [re-frame.core                             :refer [dispatch subscribe]]
            [reagent.core                              :as reagent]))

(defn- editor-div
  [width content]
  [:div.buckit--ledger-editor-input {:class (str "col-sm-" width)} content])

(defn- input
  [& options]
  [:input.form-control.input-sm (apply hash-map options)])

(defn- date-editor
  [form]
  (let [path [:transaction models.transaction/date]]
  (editor-div 2 [ui/initial-focus-wrapper
                 (input :type "text" :placeholder "Date"
                        :value (get-in @form path)
                        :on-change (ui/input-change-fn form path))])))

(defn- payee-editor
  [form payees]
  (let [path [:transaction models.transaction/payee-id]]
    (editor-div 2 [:select.form-control.input-sm
                   {:type "text"
                    :value (get-in @form path)
                    :on-change (ui/input-change-fn form path)}
                   (into (list ^{:key :empty} [:option])
                         (for [[payee-id payee] @payees]
                           ^{:key payee-id}
                           [:option
                            {:key payee-id :visible? (constantly true)}
                            (models.payee/name payee)]))])))

(defn- account-editor
  [form accounts split-path]
  (let [path (conj split-path models.split/account-id)]
    (editor-div 3 [:select.form-control.input-sm
                   {:type "text"
                    :value (get-in @form split-path)
                    :on-change (ui/input-change-fn form split-path)}
                   (into (list ^{:key :empty} [:option])
                         (for [[account-id account] @accounts]
                           ^{:key account-id}
                           [:option
                            {:key account-id :visible? (constantly true)}
                            (models.account/name account)]))])))

(defn- memo-editor
  [form split-path]
  (let [path (conj split-path models.split/memo)]
    (editor-div 3 (input :type "number" :placeholder "Memo"
                         :value (get-in @form path)
                         :on-change (ui/input-change-fn form path)))))

(defn- amount-editor
  [form split-path]
  (let [path (conj split-path models.split/amount)]
    (editor-div 2 (input :type "number" :placeholder "Amount"
                         :value (get-in @form path)
                         :on-change (ui/input-change-fn form path)))))

(defn- split-editor
  [form accounts split-path]
  (list
    (with-meta
      (account-editor form accounts split-path)
      {:key :account-id})

    (with-meta
      (memo-editor form split-path)
      {:key :memo})

    (with-meta
      (amount-editor form split-path)
      {:key :amount})))

(defn editor
  "An editor for modifying and creating transactions. NOTE: You should not
  set this editor up in such a way that it will persist as the transaction or
  options change. As an example, it would not be good to have a static editor
  that is always open. You should instead make destroy and create a new editor
  as you need to edit different transactions."
  [{:keys [account-id] :as context} transaction]
  {:pre [(integer? account-id)]}
  (let [accounts       (subscribe [:accounts])
        payees         (subscribe [:payees])
        queries        (subscribe [:queries])
        splits         (:splits transaction)
        main-split     (models.split/split-for-account splits account-id)
        other-splits   (models.split/splits-for-other-accounts splits account-id)
        form           (reagent/atom {:transaction   transaction
                                      :main-split    main-split
                                      ; For some reason, this doesn't work unless
                                      ; it's a vector. I would guess it's because
                                      ; (get-in (list 1) [0]) => nil
                                      :other-splits  (vec other-splits)
                                      :pending-query nil
                                      :error         nil})
        cancel         (events/editor-cancel-fn context transaction)
        save           (events/editor-save-fn form)]
    (fn
      [{:keys [account-id] :as context} transaction]
      {:pre [(some? main-split)]}
      (let [pending-query (:pending-query @form)
            query-result  (when pending-query (get @queries pending-query))]
        (when (and pending-query (db.query/successful? query-result))
          (js/setTimeout (fn [] (swap! form assoc :pending-query nil))))
        (when (and pending-query (db.query/failed? query-result))
          (js/setTimeout (fn [] (swap! form assoc
                                       :pending-query nil
                                       :error         i18n/generic-save-error))))
        [:form.buckit--transaction-editor
         {:on-key-down #(when (= (.-which %) keyboard/escape) (cancel %))}
         [:div.row
          (date-editor form)
          (payee-editor form payees)
          (split-editor form accounts [:main-split])]
         (doall
           (for [i (range (count other-splits))]
             ^{:key i}
             [:div.row
              [:div.col-sm-4]
              (split-editor form accounts [:other-splits i])]))
         [:div.row
          [:div.col-sm-8
           [:p.text-danger (:error @form)]]
          [:div.col-sm-4
           [:div.btn-toolbar.pull-right
            [:button.btn.btn-danger.btn-xs
             {:type "button" :on-click cancel} "Cancel"]
            [:button.btn.btn-success.btn-xs.has-spinnner
             {:type "submit" :on-click save
              :class (when pending-query "show-spinner")
              :disabled pending-query}
             (if pending-query
               [:span.buckit--btn-spinner.glyphicon.glyphicon-refresh]
               "Save")]]]]]))))
