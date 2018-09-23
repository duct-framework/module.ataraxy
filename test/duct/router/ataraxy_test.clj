(ns duct.router.ataraxy-test
  (:require [clojure.test :refer :all]
            [duct.router.ataraxy :as ataraxy]
            [integrant.core :as ig]))

(defn hello-handler [{[_ name] :ataraxy/result}]
  {:status 200, :headers {}, :body (str "Hello " name)})

(def config
  {:duct.router/ataraxy
   {:routes   '{[:get "/hello/" name] [:hello name]}
    :handlers {:hello hello-handler}}})

(deftest router-test
  (let [handler (:duct.router/ataraxy (ig/init config))]
    (is (= (handler {:request-method :get, :uri "/hello/world"})
           {:status 200, :headers {}, :body "Hello world"}))))

(deftest prep-test
  (let [routes '{[:get "/bar" id] [:foo/bar id]
                 [:get "/baz"]    [:foo/baz]}]
    (testing "no existing handlers"
      (is (= (ig/prep {:duct.router/ataraxy {:routes routes}})
             {:duct.router/ataraxy
              {:routes   routes
               :handlers {:foo/bar (ig/ref :foo/bar)
                          :foo/baz (ig/ref :foo/baz)}}})))

    (testing "some existing handlers"
      (is (= (ig/prep {:duct.router/ataraxy
                       {:routes   routes
                        :handlers {:foo/bar (ig/ref :foo/custom)}}})
             {:duct.router/ataraxy
              {:routes   routes
               :handlers {:foo/bar (ig/ref :foo/custom)
                          :foo/baz (ig/ref :foo/baz)}}})))))
