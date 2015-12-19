(ns buckit.frontend.views.transactions.editor
  (:require [buckit.frontend.db.query           :as db.query]
            [buckit.frontend.ui                 :as ui]
            [buckit.frontend.http               :as http]
            [buckit.frontend.keyboard           :as keyboard]
            [buckit.frontend.models.account     :as models.account]
            [buckit.frontend.models.core        :as models]
            [buckit.frontend.models.split       :as models.split]
            [buckit.frontend.models.payee       :as models.payee]
            [buckit.frontend.models.transaction :as models.transaction]
            [buckit.frontend.routes             :as routes]
            [buckit.frontend.utils              :as utils]
            [re-frame.core                      :refer [dispatch subscribe]]
            [reagent.core                       :as reagent]))

(defn- editor-div
  [width content]
  [:div.buckit--ledger-editor-input {:class (str "col-sm-" width)} content])

(defn- input
  [& options]
  [:input.form-control.input-sm (apply hash-map options)])

(defn- date-editor-template
  [form]
  (let [path [:transaction models.transaction/date]]
  (editor-div 2 [ui/initial-focus-wrapper
                 (input :type "text" :placeholder "Date"
                        :value (get-in @form path)
                        :on-change (ui/input-change-fn form path))])))

(defn- payee-editor-template
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

(defn- account-editor-template
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

(defn- memo-editor-template
  [form split-path]
  (let [path (conj split-path models.split/memo)]
    (editor-div 3 (input :type "number" :placeholder "Memo"
                         :value (get-in @form path)
                         :on-change (ui/input-change-fn form path)))))

(defn- amount-editor-template
  [form split-path]
  (let [path (conj split-path models.split/amount)]
    (editor-div 2 (input :type "number" :placeholder "Amount"
                         :value (get-in @form path)
                         :on-change (ui/input-change-fn form path)))))

(defn- split-editor-template
  [form accounts split-path]
  (list
    (with-meta
      (account-editor-template form accounts split-path)
      {:key :account-id})

    (with-meta
      (memo-editor-template form split-path)
      {:key :memo})

    (with-meta
      (amount-editor-template form split-path)
      {:key :amount})))

(defn- editor-cancel-fn
  [account-id transaction]
  (fn [e]
    (.preventDefault e)
    (routes/go-to
      (if-let [transaction-id (models.transaction/id transaction)]
        (routes/account-transaction-details-url
          {:account-id account-id :transaction-id transaction-id})
        (routes/account-transactions-url
          {:account-id account-id})))))

(defn- editor-save-fn
  [form]
  (fn [e]
    (.preventDefault e)
    (let [result      @form
          splits      (into [(:main-split result)]
                            (:other-splits result))
          transaction (-> result
                          :transaction
                          (assoc :splits splits))
          query       [:save-transaction transaction]]
      (dispatch query)
      (swap! form assoc :pending-query query))))

(defn editor
  [account-id transaction]
  (let [accounts       (subscribe [:accounts])
        payees         (subscribe [:payees])
        queries        (subscribe [:queries])
        splits         (:splits transaction)
        main-split     (models.split/split-for-account splits account-id)
        other-splits   (models.split/splits-for-other-accounts splits account-id)
        form           (reagent/atom {:transaction transaction
                                      :main-split main-split
                                      ; For some reason, this doesn't work unless
                                      ; it's a vector. I would guess it's because
                                      ; (get-in (list 1) [0]) => nil
                                      :other-splits (vec other-splits)
                                      :pending-query nil})
        cancel         (editor-cancel-fn account-id transaction)
        save           (editor-save-fn form)]
    (fn
      [account-id transaction]
      (assert main-split)
      (let [pending-query (:pending-query @form)
            query-result  (when pending-query (get @queries pending-query))]

        (js/console.log "pending query:" (clj->js (:pending-query @form)))

        (when (and pending-query (db.query/successful? query-result))
          (js/setTimeout (fn []
                           (swap! form assoc :pending-query nil))))

        [:form
         {:on-key-down #(when (= (.-which %) keyboard/escape) (cancel %))}
         [:div.row
          (date-editor-template form)
          (payee-editor-template form payees)
          (split-editor-template form accounts [:main-split])]
         (doall
           (for [i (range (count other-splits))]
             ^{:key i}
             [:div.row
              [:div.col-sm-4]
              (split-editor-template form accounts [:other-splits i])]))
         [:div.row
          [:div.col-sm-12
           (when pending-query [:p "Saving..."])
           [:div.btn-toolbar.pull-right
            [:input.btn.btn-danger.btn-xs {:type "button"
                                           :on-click cancel
                                           :value "Cancel"}]
            [:input.btn.btn-success.btn-xs {:type "submit"
                                            :on-click save
                                            :value "Save"}]]]]]))))
