(ns buckit.frontend.views.transactions.ledger
  (:require [buckit.frontend.db.query                   :as db.query]
            [buckit.frontend.i18n                       :as i18n]
            [buckit.frontend.ui                         :as ui]
            [buckit.frontend.keyboard                   :as keyboard]
            [buckit.frontend.models.account             :as models.account]
            [buckit.frontend.models.core                :as models]
            [buckit.frontend.models.split               :as models.split]
            [buckit.frontend.models.payee               :as models.payee]
            [buckit.frontend.models.transaction         :as models.transaction]
            [buckit.frontend.routes                     :as routes]
            [buckit.frontend.utils                      :as utils]
            [buckit.frontend.views.transactions.context :as ctx]
            [buckit.frontend.views.transactions.events  :as events]
            [re-frame.core                              :refer [dispatch subscribe]]
            [reagent.core                               :as reagent]))

(def columns
  [{:name            "Date"
    :width-on-mobile 4
    :width-normal    2}
   {:name            "Payee"
    :width-on-mobile 0
    :width-normal    2}
   {:name            "Category"
    :width-on-mobile 4
    :width-normal    3}
   {:name            "Memo"
    :width-on-mobile 0
    :width-normal    3}
   {:name            "Amount"
    :width-on-mobile 4
    :width-normal    2}])

(defn- col-width->class
  [width size]
  (if (> width 0)
    (str "col-" size "-" width)
    (str "hidden-" size)))

(defn- column-class
  [column]
  (str (col-width->class (:width-on-mobile column) "xs")
       " "
       (col-width->class (:width-normal column) "sm")))

(defn- header
  [columns]
  [:div.container-fluid
   [:div.row.buckit--ledger-header
    (for [column columns]
      ^{:key (:name column)}
      [:span {:class (column-class column)} (:name column)])]])

; ----------------------------------------------------------------------------
;     READ ONLY
; ----------------------------------------------------------------------------
(defmulti property-display (fn [_ _ column] (:name column)))

(defmethod property-display "Date"
  [context transaction _]
  (:date transaction))

(defmethod property-display "Payee"
  [context transaction _]
  (->> transaction
       models.transaction/payee-id
       (get @(:payees context))
       models.payee/name))

(defmethod property-display "Category"
  [context transaction _]
  (let [other-splits (ctx/other-splits context transaction)
        accounts     (:accounts context)]
    (if (> (count other-splits) 1)
      "Splits"
      (->> other-splits
           first
           models.split/account-id
           (get @accounts)
           models.account/name))))

(defmethod property-display "Memo"
  ; FIXME
  [& _]
  "")

(defn- amount-glyphicon
  "Returns a left or right arrow glyphicon to show whether money left the
  account or is entering it."
  [amount]
  (let [expense? (< amount 0)]
    (str "glyphicon "
         (if expense? "glyphicon-arrow-right" "glyphicon-arrow-left")
         " "
         (if expense? "expense" "income"))))

(defmethod property-display "Amount"
  [context transaction _]
  (let [amount (:amount (ctx/main-split context transaction))]
    [:span {:class (amount-glyphicon amount)
            :aria-hidden true}
     ; FIXME other currencies?
     (str " $" (js/Math.abs amount))]))

(defn- read-only-row
  [context transaction columns]
  (let [accounts (subscribe [:accounts])
        payees   (subscribe [:payees])]
    (fn
      [context transaction columns]
      (let [context (assoc context
                           :accounts accounts
                           :payees   payees)]
        [:div.row
         {:on-click (events/transaction-clicked-fn context transaction)}
         (doall (for [column columns]
                  ^{:key (:name column)}
                  [:span
                   {:class (column-class column)}
                   (property-display context transaction column)]))]))))

; ----------------------------------------------------------------------------
;     EDITOR
; ----------------------------------------------------------------------------
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

(defn editor-row
  "An editor for modifying and creating transactions. NOTE: You should not
  set this editor up in such a way that it will persist as the transaction or
  options change. As an example, it would not be good to have a static editor
  that is always open. You should instead make destroy and create a new editor
  as you need to edit different transactions."
  [context transaction columns]
  (let [accounts       (subscribe [:accounts])
        payees         (subscribe [:payees])
        queries        (subscribe [:queries])
        splits         (:splits transaction)
        main-split     (ctx/main-split context transaction)
        other-splits   (ctx/other-splits context transaction)
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
      [context transaction]
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

; ----------------------------------------------------------------------------
;     LEDGER
; ----------------------------------------------------------------------------
(defn- ledger-row
  [context transaction columns]
  (let [is-selected? (ctx/is-selected? context transaction)]
    [:div.container-fluid.buckit--ledger-row
     {:class (when is-selected? "active")}
     (if (and is-selected? (:edit? context))
       [editor-row context transaction columns]
       [read-only-row context transaction columns])]))

(defn ledger
  [context]
  (let [queries      (subscribe [:queries])
        transactions (subscribe [:transactions])]
    (fn
      [{:keys [selected-transaction-id] :as context}]
      {:pre [(ctx/valid? context)]}
      (js/console.log "in ledger, context:" (clj->js context))
      (let [query            (ctx/transactions-query context)
            query-result     (get @queries (:query-id query))
            transactions     (ctx/filter-transactions context (vals @transactions))
            ; FIXME alter for :no-account mode
            columns          columns]
        (cond
          ; -----------------------------------------------------------------
          ; NO QUERY
          ; -----------------------------------------------------------------
          (nil? query-result)
          (do (js/setTimeout #(dispatch [:http-request query]))
              [:div.buckit--spinner])
          ; -----------------------------------------------------------------
          ; PENDING QUERY
          ; -----------------------------------------------------------------
          (db.query/pending? query-result)
          [:div.buckit--spinner]
          ; -----------------------------------------------------------------
          ; FAILED QUERY
          ; -----------------------------------------------------------------
          (db.query/failed? query-result)
          [:div [:p.text-danger i18n/transactions-not-loaded-error]]
          ; -----------------------------------------------------------------
          ; SUCCESSFUL QUERY
          ; -----------------------------------------------------------------
          (db.query/successful? query-result)
          [:div.buckit--ledger
           [header columns]
           (doall
             (for [transaction transactions]
               ^{:key (models.transaction/id transaction)}
               [ledger-row context transaction columns]))
           (when (and (not selected-transaction-id) (:edit? context))
             [:div.container-fluid.buckit--ledger-row.active
              [editor-row context (ctx/new-transaction context) columns]])])))))
