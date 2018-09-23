(ns duct.router.ataraxy
  (:require [ataraxy.core :as ataraxy]
            [clojure.set :as set]
            [integrant.core :as ig]))

(defn- missing-handlers [{:keys [routes handlers]}]
  (set/difference (set (ataraxy/result-keys routes)) (set (keys handlers))))

(defn- add-default-handlers [handler-map keys-to-add]
  (reduce (fn [m k] (assoc m k (ig/ref k))) handler-map keys-to-add))

(defmethod ig/prep-key :duct.router/ataraxy [_ options]
  (update options :handlers add-default-handlers (missing-handlers options)))

(defmethod ig/init-key :duct.router/ataraxy [_ options]
  (ataraxy/handler options))
