(ns duct.router.ataraxy
  (:require [ataraxy.core :as ataraxy]
            [integrant.core :as ig]))

(derive :duct.router/ataraxy :duct/router)

(def ^:private default-endpoints
  {:ataraxy/not-found (constantly nil)})

(defmethod ig/init-key :duct.router/ataraxy [_ {:keys [routes endpoints]}]
  (ataraxy/handler routes (merge default-endpoints endpoints)))
