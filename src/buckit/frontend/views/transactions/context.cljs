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
      (utils/filter-map-by-v some?)))

(defn <-url-params
  [url-params]
  (-> url-params
      (rename-keys {:account_id :account-id
                    :id         :selected-transaction-id
                    :edit       :edit?})
      (select-keys #{:account-id :selected-transaction-id :edit?})))

(defn new-transaction
  ; FIXME doesn't work for :no-account mode
  [context]
  (models.transaction/create (:account-id context)))
; ----------------------------------------------------------------------------
(defmulti transactions-query mode)

(defmethod transactions-query :no-account
  [_]
  {:query-id [:load-transactions]
   :method   :get-many
   :resource models/transactions})

(defmethod transactions-query :single-account
  [{:keys [account-id]}]
  {:query-id [:load-transactions account-id]
   :method   :get-many
   :resource models/transactions
   :args     [{:filters [{:name "splits__account_id"
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
  (first (models.transaction/splits transaction)))

; ----------------------------------------------------------------------------
(defmulti other-splits mode)

(defmethod other-splits :single-account
  [context transaction]
  (models.split/splits-for-other-accounts
    (models.transaction/splits transaction)
    (:account-id context)))

(defmethod other-splits :default
  [_ transaction]
  (rest (models.transaction/splits transaction)))
