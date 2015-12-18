(ns buckit.frontend.views.transactions
  (:require [buckit.frontend.db.query           :as db.query]
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
            [reagent.core                       :as reagent]
            [reagent-forms.core                 :as forms]))

(defn- split-for-account
  [splits account-id]
  (first (filter #(= (models.split/account-id %) account-id) splits)))

(defn- splits-for-other-accounts
  [splits account-id]
  (remove #(= (models.split/account-id %) account-id) splits))

(defn- account-to-show
  [accounts other-splits]
  (if (> (count other-splits) 1)
    "Splits"
    (->> other-splits
         first
         models.split/account-id
         (get accounts)
         models.account/name)))

(defn- amount-glyphicon
  "Returns a left or right arrow glyphicon to show whether money left the
  account or is entering it."
  [amount]
  (let [expense? (< amount 0)]
    (str "glyphicon "
         (if expense? "glyphicon-arrow-right" "glyphicon-arrow-left")
         " "
         (if expense? "expense" "income"))))

(defn- amount-to-show
  [main-split]
  {:pre [(some? main-split)]}
  (let [amount   (:amount main-split)]
    [:span {:class (amount-glyphicon amount)
            :aria-hidden true}
     ; FIXME other currencies?
     (str " $" (js/Math.abs amount))]))

(def ledger-header
  [:div.container-fluid
   [:div.row.buckit--ledger-header
    [:span.col-sm-2 "Date"]
    [:span.col-sm-2 "Payee"]
    [:span.col-sm-3 "Category"]
    [:span.col-sm-3 "Memo"]
    [:span.col-sm-2 "Amount"]]])

(defn- ledger-row
  [account-id transaction & {:keys [is-selected?]}]
  (let [accounts (subscribe [:accounts])
        payees   (subscribe [:payees])]
    (fn
      [account-id transaction & {:keys [is-selected?]}]
      (let [splits       (:splits transaction)
            main-split   (split-for-account splits account-id)
            other-splits (splits-for-other-accounts splits account-id)]
        (assert main-split)
        [:div.row
         {:on-click #(routes/go-to
                       ((if is-selected?
                          routes/account-transaction-edit-url
                          routes/account-transaction-details-url)
                          {:account-id account-id
                           :transaction-id (models.transaction/id transaction)}))}
          [:span.col-sm-2 (:date transaction)]
          [:span.col-sm-2 (->> transaction
                               models.transaction/payee-id
                               (get @payees)
                               models.payee/name)]
          [:span.col-sm-3 (account-to-show @accounts other-splits)]
          [:span.col-sm-3]
          [:span.col-sm-2 (amount-to-show main-split)]]))))

(defn- editor-div
  [width content]
  [:div.buckit--ledger-editor-input {:class (str "col-sm-" width)} content])

(defn- input
  [& options]
  [:input.form-control.input-sm (apply hash-map options)])

(def initial-focus-wrapper
  ; TODO move
  (with-meta identity
    {:component-did-mount #(.focus (reagent/dom-node %))}))

(defn- input-on-change-fn
  [form path]
  (fn [e]
    (swap! form assoc-in path (-> e .-target .-value))))

(defn- date-editor-template
  [form]
  (let [path [:transaction models.transaction/date]]
  (editor-div 2 [initial-focus-wrapper
                 (input :type "text" :placeholder "Date"
                        :value (get-in @form path)
                        :on-change (input-on-change-fn form path))])))

(defn- payee-editor-template
  [form payees]
  (let [path [:transaction models.transaction/payee-id]]
    (editor-div 2 [:select.form-control.input-sm
                   {:type "text"
                    :value (get-in @form path)
                    :on-change (input-on-change-fn form path)}
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
                    :on-change (input-on-change-fn form split-path)}
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
                         :on-change (input-on-change-fn form path)))))

(defn- amount-editor-template
  [form split-path]
  (let [path (conj split-path models.split/amount)]
    (editor-div 2 (input :type "number" :placeholder "Amount"
                         :value (get-in @form path)
                         :on-change (input-on-change-fn form path)))))

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

(defn- editor
  [account-id transaction]
  (let [accounts       (subscribe [:accounts])
        payees         (subscribe [:payees])
        queries        (subscribe [:queries])
        splits         (:splits transaction)
        main-split     (split-for-account splits account-id)
        other-splits   (splits-for-other-accounts splits account-id)
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

(defn- toolbar
  [{:keys [account-id]}]
  [:div.buckit--transactions-toolbar
   [:button.btn.btn-default
    {:on-click #(routes/go-to (routes/account-transaction-create-url
                                {:account-id account-id}))}
    "+ Transaction"]])

(defn- ledger
  [context]
  (let [queries      (subscribe [:queries])
        transactions (subscribe [:transactions])]
    (fn
      [{:keys [account-id selected-transaction-id] :as context}]
      (let [query        [:load-account-transactions account-id]
            query-result (get @queries query)
            transactions (filter (partial models/account-in-splits? account-id)
                                 (vals @transactions))]

        (cond

          (db.query/successful? query-result)
          [:div.buckit--ledger
           ledger-header
           (doall
             (for [transaction transactions
                   :let [transaction-id (models.transaction/id transaction)
                         is-selected?   (= selected-transaction-id transaction-id)]]
               ^{:key transaction-id}
               [:div.container-fluid.buckit--ledger-row
                {:class (when is-selected? "active")}
                (if (and is-selected? (:edit-selected? context))
                  [editor account-id transaction]
                  [ledger-row account-id transaction
                   :is-selected? (= selected-transaction-id transaction-id)])]))
           (when (:create-transaction? context)
             [:div.container-fluid.buckit--ledger-row.active
              [editor account-id (-> (models.transaction/create account-id))]])]

          (db.query/failed? query-result)
          [:div "there was an error"]

          (db.query/pending? query-result)
          [:div.buckit--spinner]

          ; otherwise we haven't issued the request to load the transactions
          :else
          (dispatch query))))))

(defn transactions
  "context map:

  :account-id              ID of the account currently being worked on
  (required)

  :create-transaction?     Indicates whether the editor to create a new
  transaction should be shown.
  (optional -- default: false)

  :selected-transaction-id ID of the transaction highlighted or being edited
  (optional -- default: nil)

  :edit-selected?          Indicaes whether :selected-transaction-id is being
  edited
  (optional -- default: false)
  "
  [{:keys [account-id selected-transaction-id] :as context}]
  {:pre [(integer? account-id)
         (or (nil? selected-transaction-id) (integer? selected-transaction-id))]}
  [:div.container-fluid.buckit--transactions-view
   [:div.row [toolbar context]]
   [:div.row [ledger context]]])
