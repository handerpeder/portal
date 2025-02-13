(ns portal.ui.api
  (:require [reagent.core :as r]))

(defonce viewers (r/atom []))

(defn register-viewer! [viewer-spec]
  (swap! viewers
         (fn [viewers]
           (assoc
            viewers
            (or
             (first
              (keep-indexed
               (fn [index {:keys [name]}]
                 (when (= (:name viewer-spec) name)
                   index))
               viewers))
             (count viewers))
            viewer-spec))))

(def ^:no-doc portal-api "Portal API for JS interop." #js {})

(set! (.-portal_api js/window) portal-api)