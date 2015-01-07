(ns jiksnu.modules.core.sections.activity-sections
  (:require [ciste.core :refer [with-format]]
            [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [edit-button
                                            show-section-minimal
                                            show-section uri title index-block
                                            index-line index-section update-button]]
            [clojure.core.incubator :refer [-?>]]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.namespace :as ns]
            [jiksnu.modules.core.sections :refer [admin-index-line admin-index-block
                                                  admin-index-section ]]
            [jiksnu.modules.core.sections.user-sections :as sections.user]
            [jiksnu.modules.web.sections :refer [dropdown-menu format-links]]
            [jiksnu.session :as session]
            [jiksnu.util :as util]
            [slingshot.slingshot :refer [throw+]])
  (:import jiksnu.model.Activity))

(defn index-formats
  [activities]
  (map
   (fn [[f h]]
     (let [def (format-links f)]
       (merge def
              {:href h})))
   [[:as        "/api/statuses/public_timeline.as"]
    [:atom      "/api/statuses/public_timeline.atom"]
    [:json      "/api/statuses/public_timeline.json"]
    [:n3        "/api/statuses/public_timeline.n3"]
    [:rdf       "/api/statuses/public_timeline.rdf"]
    [:viewmodel "/api/statuses/public_timeline.viewmodel"]
    [:xml       "/api/statuses/public_timeline.xml"]]))

(defn timeline-formats
  [user]
  (map
   (fn [[f h]]
     (let [def (format-links f)]
       (merge def
              {:href h})))
   [[:json (sections.user/user-timeline-link user "json")]
    [:atom (sections.user/user-timeline-link user "atom")]
    [:as   (sections.user/user-timeline-link user "as")]
    [:n3   (sections.user/user-timeline-link user "n3")]
    [:rdf  (sections.user/user-timeline-link user "rdf")]
    [:xml  (sections.user/user-timeline-link user "xml")]]))

(defsection admin-index-block [Activity]
  [items & [page]]
  (map #(admin-index-line % page) items))

(defsection admin-index-block [Activity :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] (index-line m page)))
       doall))

(defsection admin-index-section [Activity]
  [items & [page]]
  (admin-index-block items page))

(defsection admin-index-section [Activity :viewmodel]
  [items & [page]]
  (admin-index-block items page))

(defsection index-block [Activity]
  [items & [page]]
  (doall (map #(index-line % page) items)))

(defsection index-block [Activity :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] (index-line m page)))
       doall))

(defsection index-block [Activity :xml]
  [activities & _]
  [:statuses {:type "array"}
   (map index-line activities)])

(defsection index-line [Activity]
  [activity & [page]]
  (show-section activity page))

(defsection index-section [Activity]
  [items & [page]]
  (index-block items page))

(defsection show-section [Activity :twitter]
  [activity & _]
  (merge
   {:text (:title activity)
    :truncated false
    :created_at (util/date->twitter (.toDate (:published activity)))
    :source (:source activity)
    :id (:_id activity)
    ;; :in_reply_to_user_id nil
    ;; :in_reply_to_screen_name nil

    ;; TODO: test for the presence of a like
    :favorited false
    :user (let [user (model.activity/get-author activity)]
            (show-section user))
    :statusnet_html (:content activity)}
   (when-let [conversation (first (:conversation-uris activity))]
     {:statusnet_conversation_id conversation})
   (let [irt (first (:irts activity))]
     {:in_reply_to_status_id irt})
   (when-let [attachments (:attachments activity)]
     {:attachments attachments})))

(defsection show-section [Activity :model]
  [activity & [page]]

  ;; (dissoc activity :links)
  activity
  )

;; (defsection show-section [Activity :viewmodel]
;;   [activity & [page]]
;;   (dissoc activity :links))

(defsection show-section [Activity :xml]
  [activity & _]
  [:status
   [:text (h/h (or (:title activity)
                   (:content activity)))]
   [:truncated "false"]
   [:created_at (-?> activity :published .toDate util/date->twitter)]
   [:source (:source activity)]
   [:id (:_id activity)]
   [:in_reply_to_status_id]
   [:in_reply_to_user_id]
   [:favorited "false" #_(liked? (current-user) activity)]
   [:in_reply_to_screen_name]
   (show-section (model.activity/get-author activity))
   (when (:geo activity)
     (list [:geo]
           [:coordnates]
           [:place]))
   [:contributors]
   [:entities
    [:user_mentions
     ;; TODO: list mentions
     ]
    [:urls
     ;; TODO: list urls
     ]
    [:hashtags
     ;; TODO: list hashtags
     ]]])

(defsection title [Activity]
  [activity & options]
  (:title activity))

(defsection uri [Activity]
  [activity & options]
  (str "/notice/" (:_id activity)))