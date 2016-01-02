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

(defn get-columns
  [context]
  "All columns where :is-split-property? is true should be to the right."
  [{:name               "Date"
    :width-on-mobile    4
    :width-normal       2
    :is-split-property? false}
   {:name               "Account"
    :width-on-mobile    0
    :width-normal       0
    :is-split-property? false} ; hmmmmm
   {:name               "Payee"
    :width-on-mobile    0
    :width-normal       2
    :is-split-property? false}
   {:name               "Category"
    :width-on-mobile    4
    :width-normal       3
    :is-split-property? true}
   {:name               "Memo"
    :width-on-mobile    0
    :width-normal       3
    :is-split-property? true}
   {:name               "Amount"
    :width-on-mobile    4
    :width-normal       2
    :is-split-property? true}])

(defn- non-splits-columns
  [columns]
  (remove :is-split-property? columns))

(defn- splits-columns
  [columns]
  (filter :is-split-property? columns))

(defn- total-mobile-width
  [columns]
  (->> columns
       (map :width-on-mobile)
       (reduce +)))

(defn- total-normal-width
  [columns]
  (->> columns
       (map :width-normal)
       (reduce +)))

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

(defmethod property-display "Account"
  [{:keys [accounts] :as context} transaction _]
  (->> transaction
       (ctx/main-split context)
       models.split/account-id
       (get @accounts)
       models.account/name))

(defmethod property-display "Payee"
  [{:keys [payees]} transaction _]
  (->> transaction
       models.transaction/payee-id
       (get @payees)
       models.payee/name))

(defmethod property-display "Category"
  [{:keys [accounts] :as context} transaction _]
  (let [other-splits (ctx/other-splits context transaction)]
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
(defmulti property-editor (fn [_ column] (:name column)))

(defmethod property-editor "Date"
  [{:keys [form]} _]
  (let [path [:transaction models.transaction/date]]
    [ui/initial-focus-wrapper
     [:input.form-control.input-sm {:type "text" :placeholder "Date"
                                    :value (get-in @form path)
                                    :on-change (ui/input-change-fn form path)}]]))

(defmethod property-editor "Account"
  [{:keys [form accounts]} _]
  ; FIXME
  [:p "hi"])

(defmethod property-editor "Payee"
  [{:keys [form payees]} _]
  (let [path [:transaction models.transaction/payee-id]]
    [:select.form-control.input-sm
     {:type "text"
      :value (get-in @form path)
      :on-change (ui/input-change-fn form path)}
     (into (list ^{:key :empty} [:option])
           (for [[payee-id payee] @payees]
             ^{:key payee-id}
             [:option {:key payee-id :value payee-id} (models.payee/name payee)]))]))

(defmethod property-editor "Category"
  [{:keys [form accounts]} _ split-path]
  (let [path (conj split-path models.split/account-id)]
    [:select.form-control.input-sm
     {:type "text"
      :value (get-in @form path)
      :on-change (ui/input-change-fn form path)}
     (into (list ^{:key :empty} [:option])
           (for [[account-id account] @accounts]
             ^{:key account-id}
             [:option {:key account-id :value account-id} (models.account/name account)]))]))

(defmethod property-editor "Memo"
  [{:keys [form]} _ split-path]
  (let [path (conj split-path models.split/memo)]
    [:input.form-control.input-sm {:type "text" :placeholder "Memo"
                                   :value (get-in @form path)
                                   :on-change (ui/input-change-fn form path)}]))

(defmethod property-editor "Amount"
  [{:keys [form]} _ split-path]
  (let [path (conj split-path models.split/amount)]
    [:input.form-control.input-sm {:type "number" :placeholder "Amount"
                                   :value (get-in @form path)
                                   :on-change (ui/input-change-fn form path)}]))

(defn- create-editors
  [editor-context columns root-path]
  (doall (for [column columns]
           ^{:key (:name column)}
           [:div.buckit--ledger-editor-input
            {:class (column-class column)}
            (property-editor editor-context column root-path)])))

(defn- editor-toolbar
  [{:keys [form cancel-fn save-fn]} & {:keys [show-spinner?]}]
  [:div.row
   (let [msg (:msg @form)]
     [:div.col-sm-8 [:p {:class (:class msg)} (:text msg)]])
   [:div.col-sm-4
    [:div.btn-toolbar.pull-right
     [:button.btn.btn-danger.btn-xs {:type "button" :on-click cancel-fn} "Cancel"]
     [:button.btn.btn-success.btn-xs.has-spinnner
      {:type "submit" :on-click save-fn
       :class (when show-spinner? "show-spinner")
       :disabled show-spinner?}
      (if show-spinner?
        [:span.buckit--btn-spinner.glyphicon.glyphicon-refresh]
        "Save")]]]])

(defn- editor-row
  "An editor for modifying and creating transactions. NOTE: You should not
  set this editor up in such a way that it will persist as the transaction or
  options change. As an example, it would not be good to have a static editor
  that is always open. You should instead make destroy and create a new editor
  as you need to edit different transactions."
  [context transaction columns]
  (let [accounts        (subscribe [:accounts])
        payees          (subscribe [:payees])
        queries         (subscribe [:queries])
        splits          (:splits transaction)
        main-split      (ctx/main-split context transaction)
        other-splits    (ctx/other-splits context transaction)
        form            (reagent/atom {:transaction   transaction
                                       :main-split    main-split
                                       ; For some reason, this doesn't work unless
                                       ; it's a vector. I would guess it's because
                                       ; (get-in (list 1) [0]) => nil
                                       :other-splits  (vec other-splits)
                                       :pending-query nil
                                       :msg           {}})
        cancel-fn       (events/editor-cancel-fn context transaction)
        save-fn         (events/editor-save-fn form)
        editor-context  (assoc context
                               :accounts  accounts
                               :payees    payees
                               :form      form
                               :cancel-fn cancel-fn
                               :save-fn   save-fn)

        splits-cols     (splits-columns columns)
        non-splits-cols (non-splits-columns columns)
        ]
    (fn
      [& _] ; normally you should match the arguments to the parent-level fn,
            ; but here we intentionally want the variables from the parent
            ; level to be in this closure.
      (let [pending-query (:pending-query @form)
            query-result  (when pending-query (get @queries pending-query))]

        ; FIXME move to another fn
        (when (and pending-query (db.query/complete? query-result))
          (js/setTimeout (fn [] (swap! form assoc
                                       :pending-query nil
                                       :msg           (if (db.query/failed? query-result)
                                                        {:text  i18n/generic-save-error
                                                         :class "text-danger"}
                                                        {:text  i18n/generic-save-success
                                                         :class "text-success"})))))

        [:form.buckit--transaction-editor
         {:on-key-down #(when (= (.-which %) keyboard/escape) (cancel-fn %))}
         [:div.row
          (create-editors editor-context non-splits-cols [])
          (create-editors editor-context splits-cols [:main-split])]
         (doall (for [i (range (count other-splits))]
                  ^{:key i}
                  [:div.row
                   [:div {:class (str "col-sm-" (- 12 (total-normal-width splits-cols)))}]
                   (create-editors editor-context splits-cols [:other-splits i])]))
         [editor-toolbar editor-context :show-spinner? pending-query]]))))

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
            transactions     (->> @transactions
                                  (vals)
                                  (ctx/filter-transactions context)
                                  (sort-by models.transaction/date))
            ; FIXME alter for :no-account mode
            columns          (get-columns context)]
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
