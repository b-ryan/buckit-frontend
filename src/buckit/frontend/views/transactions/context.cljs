(ns buckit.frontend.views.transactions.context
  "context map:

  :account-id              ID of the account currently being worked on
                           (required)

  :selected-transaction-id ID of the transaction highlighted or being edited
                           (optional -- default: nil)

  :edit?                   Indicaes whether edit mode will be used.
                           (optional -- default: false)

  The context may also be used to hold subscriptions or other data needed for
  sub-views."
  (:require [buckit.frontend.models.core        :as models]
            [buckit.frontend.models.split       :as models.split]
            [buckit.frontend.models.transaction :as models.transaction]
            [buckit.frontend.utils              :as utils]
            [cljs-time.coerce                   :as time.coerce]
            [cljs-time.core                     :as time.core]
            [cljs-time.format                   :as time.fmt]
            [clojure.set                        :refer [rename-keys]]))

(defn valid?
  [{:keys [account-id selected-transaction-id edit?]}]
  (and (utils/nil-or-integer? account-id)
       (utils/nil-or-integer? selected-transaction-id)
       (utils/nil-or-boolean? edit?)))

(def valid-mode? (partial contains? #{:no-account :single-account}))

(defn mode
  [context]
  (let [account-id (:account-id context)]
    (cond
      ; FIXME allow for multiple accounts?
      (nil? account-id) :no-account
      :else             :single-account)))

(defn is-selected?
  "Returns true if the given transaction is the one that should be selected."
  [context transaction]
  (= (:selected-transaction-id context)
     (models.transaction/id transaction)))

(defn ->url-params
  [context]
  (-> context
      (rename-keys {:account-id              :account_id
                    :selected-transaction-id :id
                    :edit?                   :edit})
      (select-keys #{:account_id :id :edit})
      (utils/remove-map-by-v #(empty? (str %)))))

(defn <-url-params
  [url-params]
  (-> url-params
      (rename-keys {:account_id :account-id
                    :id         :selected-transaction-id
                    :edit       :edit?})
      (select-keys #{:account-id :selected-transaction-id :edit?})))

(defn- show-account-column?
  "Returns true if the column for showing/editing the account of the main split
  should be shown."
  [context]
  (not= (mode context) :single-account))

(defn- hidden-column?
  [column]
  (and (zero? (:width-on-mobile column))
       (zero? (:width-normal column))))

(defn get-columns
  "Returns a vector of column objects that determine what to show in the
  ledger and how wide they should be."
  [context]
  ; All columns where :is-split-property? is true should be to the right.
  (let [show-account? (show-account-column? context)]
    (remove hidden-column?
            [{:name               "Date"
              :width-on-mobile    4
              :width-normal       2
              :is-split-property? false}
             {:name               "Account"
              :width-on-mobile    0
              :width-normal       (if show-account? 2 0)
              :is-split-property? false}
             {:name               "Payee"
              :width-on-mobile    0
              :width-normal       2
              :is-split-property? false}
             {:name               "Category"
              :width-on-mobile    4
              :width-normal       (if show-account? 2 3)
              :is-split-property? true}
             {:name               "Memo"
              :width-on-mobile    0
              :width-normal       (if show-account? 2 3)
              :is-split-property? true}
             {:name               "Amount"
              :width-on-mobile    4
              :width-normal       2
              :is-split-property? true}])))

; ----------------------------------------------------------------------------
(defn- create-split
  "Creates a new split. If an account ID is given, it will be used. Otherwise
  the account ID will be nil. The amount will be 0."
  [to-merge]
  (merge {models.split/id             nil
          models.split/account-id     nil
          models.split/amount         0
          models.split/primary-split? false}
         to-merge))

(defn- create-transaction
  "Creates a transaction with 2 splits. If an account ID is provided, it will
  be used as the account ID of the first split. The date will be today."
  [& [account-id]]
  {models.transaction/id          nil
   ;TODO do not format the date, just pass it along.
   ; consider using built-in :year-month-day formatter
   models.transaction/date        (time.fmt/unparse (time.fmt/formatter "yyyy-MM-dd")
                                                    (time.coerce/to-date-time (time.core/today)))
   models.transaction/payee-id    nil
   models.transaction/splits      [(create-split {models.split/account-id     account-id
                                                  models.split/primary-split? true})
                                   (create-split {})]})

(defmulti new-transaction mode)

(defmethod new-transaction :no-account
  [_]
  (create-transaction))

(defmethod new-transaction :single-account
  [context]
  (create-transaction (:account-id context)))

; ----------------------------------------------------------------------------
(defmulti transactions-query mode)

(defmethod transactions-query :no-account
  [_]
  {:query-id   [:load-transactions]
   :method     :get-many
   :model-type models/transactions})

(defmethod transactions-query :single-account
  [{:keys [account-id]}]
  {:query-id   [:load-transactions account-id]
   :method     :get-many
   :model-type models/transactions
   :args       [{:filters [{:name "splits__account_id"
                            :op   "any"
                            :val  account-id}]}]})

; ----------------------------------------------------------------------------
(defmulti filter-transactions mode)

(defmethod filter-transactions :no-account
  [_ transactions]
  transactions)

(defmethod filter-transactions :single-account
  [{:keys [account-id]} transactions]
  (models/account-transactions account-id transactions))

; ----------------------------------------------------------------------------
(defmulti main-split mode)

(defmethod main-split :single-account
  [context transaction]
  (models.split/split-for-account
    (models.transaction/splits transaction)
    (:account-id context)))

(defmethod main-split :default
  [_ transaction]
  (let [splits (models.transaction/splits transaction)]
    (first (or (seq (filter models.split/primary-split? splits))
               splits))))

(defn other-splits
  [context transaction]
  (remove #(= % (main-split context transaction))
          (models.transaction/splits transaction)))
