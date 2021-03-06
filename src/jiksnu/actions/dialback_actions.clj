(ns jiksnu.actions.dialback-actions
  (:require [ciste.core :refer [defaction]]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.dialback :as model.dialback]
            [jiksnu.model.user :as model.user]
            [jiksnu.namespace :as ns]
            [jiksnu.session :as session]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms]
            [slingshot.slingshot :refer [throw+]]))

(def index*    (templates.actions/make-indexer 'jiksnu.model.dialback :sort-clause {:date 1}))

(defaction index
  [& options]
  (apply index* options))

(defn prepare-create
  [activity]
  (transforms/set-_id activity))

(defaction create
  [params]
  (let [item (model.dialback/create params)]
    (model.dialback/fetch-by-id (:_id item))))

;; (defaction delete
;;   [activity]
;;   (let [actor-id (session/current-user-id)
;;         author (:author activity)]
;;     (if (or (session/is-admin?) (= actor-id author))
;;       (model.activity/delete activity)
;;       ;; TODO: better exception type
;;       (throw+ {:type :authorization
;;                :msg "You are not authorized to delete that activity"}))))

(defn confirm
  [params]
  ;; TODO: validate
  true)
