(ns duct.module.ataraxy-test
  (:require [clojure.test :refer :all]
            [duct.core :as duct]
            [duct.module.ataraxy :as ataraxy]
            [integrant.core :as ig]))

(defn bar-handler [{[_ id] :ataraxy/result}]
  {:status 200, :headers {}, :body (str "bar " id)})

(defn wrap-quz [handler]
  #(assoc-in (handler %) [:headers "X-Quz"] "quz"))

(def config
  {:duct.core/project-ns 'foo
   :duct.module/ataraxy  '{["/bar" id] ^:quz [:bar id], ["/baz"] [:test/baz]}
   :foo.handler/bar      bar-handler
   :foo.middlware/quz    wrap-quz
   :duct.handler.error/not-found          {:response "Not Found"}
   :duct.handler.error/method-not-allowed {:response "Method Not Allowed"}
   :duct.handler.error/bad-request        {:response "Bad Request"}})

(deftest module-test
  (is (= (duct/prep config)
         (merge config
                {:duct.core/handler
                 {:router (ig/ref :duct.router/ataraxy)}
                 :duct.router/ataraxy
                 {:routes
                  '{["/bar" id] ^:quz [:bar id], ["/baz"] [:test/baz]}
                  :handlers
                  {:bar      (ig/ref :foo.handler/bar)
                   :test/baz (ig/ref :foo.handler.test/baz)
                   :ataraxy.response/not-found
                   (ig/ref :duct.handler.error/not-found)
                   :ataraxy.response/method-not-allowed
                   (ig/ref :duct.handler.error/method-not-allowed)
                   :ataraxy.response/bad-request
                   (ig/ref :duct.handler.error/bad-request)
                   :ataraxy.response/internal-server-error
                   (ig/ref :duct.handler.error/internal-error)}
                  :middleware
                  {:quz (ig/ref :foo.middleware/quz)}}}))))
