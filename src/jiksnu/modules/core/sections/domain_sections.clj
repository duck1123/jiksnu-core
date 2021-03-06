(ns jiksnu.modules.core.sections.domain-sections
  (:require [ciste.core :refer [with-format]]
            [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [actions-section index-block index-line
                                            show-section uri]]
            [jiksnu.session :refer [current-user is-admin?]]
            [jiksnu.modules.core.sections :refer [admin-index-block
                                                  admin-index-line]]
            [jiksnu.namespace :as ns]
            [jiksnu.session :as session])
  (:import jiksnu.model.Domain))

;; admin-index-block

(defsection show-section [Domain :jrd]
  [item & [page]]
  (let [id (:_id item)
        template (format "http://%s/main/xrd?uri={uri}" id)]
    {:host id
     :links [{:template template
              :rel "lrdd"
              :title "Resource Descriptor"}]}))

(defsection show-section [Domain :model]
  [item & [page]]
  item)

(defsection show-section [Domain :xrd]
  [item & [page]]
  (let [id (:_id item)]
    ["XRD" {"xmlns" ns/xrd
            "xmlns:hm" ns/host-meta}
     ["hm:Host" id]
     ["Subject" id]
     (map
      ;; TODO: show-section [Link :xrd]
      (fn [{:keys [title rel href template] :as link}]
        [:Link (merge {}
                      (when rel {:rel rel})
                      (when href {:href href})
                      (when template {:template template}))
         (when title [:Title title])])
      (:links item))]))

;; uri

(defsection uri [Domain]
  [domain & _]
  (str "/main/domains/" (:_id domain)))
