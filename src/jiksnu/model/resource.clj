(ns jiksnu.model.resource
  (:require [ciste.initializer :refer [definitializer]]
            [jiksnu.db :refer [_db]]
            [jiksnu.model :as model]
            [jiksnu.templates.model :as templates.model]
            [jiksnu.util :as util]
            [jiksnu.validators :refer [type-of]]
            [monger.collection :as mc]
            [monger.query :as mq]
            [net.cgrand.enlive-html :as enlive]
            [slingshot.slingshot :refer [throw+]]
            [validateur.validation :refer [validation-set presence-of acceptance-of]])
  (:import java.io.StringReader
           org.bson.types.ObjectId
           org.joda.time.DateTime))

(def collection-name "resources")
(def maker           #'model/map->Resource)
(def page-size       20)

(def create-validators
  (validation-set
   ;; (type-of :_id     String)
   ;; (type-of :domain  String)
   ;; (type-of :local   Boolean)
   ;; (type-of :created DateTime)
   ;; (type-of :updated DateTime)

))

(def count-records (templates.model/make-counter       collection-name))
(def delete        (templates.model/make-deleter       collection-name))
(def drop!         (templates.model/make-dropper       collection-name))
(def remove-field! (templates.model/make-remove-field! collection-name))
(def set-field!    (templates.model/make-set-field!    collection-name))
(def fetch-by-id   (templates.model/make-fetch-by-id   collection-name maker false))
(def create        (templates.model/make-create        collection-name #'fetch-by-id #'create-validators))
(def fetch-all     (templates.model/make-fetch-fn      collection-name maker))

(defn fetch-by-url
  [url]
  (first (fetch-all {:url url})))

(defn get-link
  [item rel content-type]
  (first (util/rel-filter rel (:links item) content-type)))

(defn ensure-indexes
  []
  (doto collection-name
    (mc/ensure-index @_db {:url 1} {:unique true})))

(defn response->tree
  [response]
  (enlive/html-resource (StringReader. (:body response))))

(defn get-links
  [tree]
  (enlive/select tree [:link]))

(defn meta->property
  "Convert a meta element to a property map"
  [meta]
  (let [attrs (:attrs meta)
        property (:property attrs)
        content (:content attrs)]
    (when (and property content)
      {property content})))

(defn get-meta-properties
  "Get a map of all the meta properties in the document"
  [tree]
  (->> (enlive/select tree [:meta])
       (map meta->property)
       (reduce merge)))

;; (definitializer
;;   (ensure-indexes))
