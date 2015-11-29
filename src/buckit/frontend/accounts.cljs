(ns buckit.frontend.accounts
  (:require [clojure.set]))

(def asset
  "Bank accounts are examples of asset accounts. They contain cash or other
  assets."
  "asset")

(def liability
  "Liability accounts are those which create obligations to another party. The
  best example is a credit account, which puts you into debt when you use it."
  "liability")

(def income
  "This account type is used for transactions where some amount of money is
  coming into an account. For example, when you receive a pay check, the
  money coming in will be considered coming from an income account."
  "income")

(def expense
  "Expense are the opposite of income accounts. For example, paying money to a
  grocery store would be taking money from an asset or liability account and
  sending it to the 'Groceries' expense account."
  "expense")

(def all-account-types #{asset liability income expense})

(def owned-account-types
  "These account types are 'owned' by the user. Like bank accounts, credit
  accounts, etc., as oppsosed to things like groceries that are considered
  'foreign'."
  #{asset liability})

(def foreign-account-types
  "See the docs on owned-account-types."
  (clojure.set/difference all-account-types owned-account-types))
