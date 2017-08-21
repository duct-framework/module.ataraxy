(ns duct.router.ataraxy
  (:require [ataraxy.core :as ataraxy]
            [integrant.core :as ig]))

(defmethod ig/init-key :duct.router/ataraxy [_ options]
  (ataraxy/handler options))
