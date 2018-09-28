(ns duct.router.ataraxy-test
  (:require [ataraxy.response :as response]
            [clojure.test :refer :all]
            [duct.core :as duct]
            [duct.router.ataraxy :as ataraxy]
            [integrant.core :as ig]))

(defn hello-handler [{[_ name] :ataraxy/result}]
  {:status 200, :headers {}, :body (str "Hello " name)})

(defn resource-handler [_]
  [::response/ok (duct/resource "duct/router/test.txt")])

(deftest router-test
  (let [config  {:duct.router/ataraxy
                 {:routes   '{[:get "/hello/" name] [:hello name]}
                  :handlers {:hello hello-handler}}}
        handler (:duct.router/ataraxy (ig/init config))]
    (is (= (handler {:request-method :get, :uri "/hello/world"})
           {:status 200, :headers {}, :body "Hello world"}))))

(deftest prep-test
  (let [routes '{[:get "/bar" id] [:foo/bar id]
                 [:get "/baz"]    [:foo/baz]}]
    (testing "no existing handlers"
      (is (= (ig/prep {:duct.router/ataraxy {:routes routes}})
             {:duct.router/ataraxy
              {:routes   routes
               :handlers
               {:foo/bar                        (ig/ref :foo/bar)
                :foo/baz                        (ig/ref :foo/baz)
                :ataraxy.error/unmatched-path   (ig/ref :duct.handler.static/not-found)
                :ataraxy.error/unmatched-method (ig/ref :duct.handler.static/method-not-allowed)
                :ataraxy.error/missing-params   (ig/ref :duct.handler.static/bad-request)
                :ataraxy.error/missing-destruct (ig/ref :duct.handler.static/bad-request)
                :ataraxy.error/failed-coercions (ig/ref :duct.handler.static/bad-request)
                :ataraxy.error/failed-spec      (ig/ref :duct.handler.static/bad-request)}}})))

    (testing "some existing handlers"
      (is (= (ig/prep {:duct.router/ataraxy
                       {:routes   routes
                        :handlers {:foo/bar (ig/ref :foo/custom)}}})
             {:duct.router/ataraxy
              {:routes   routes
               :handlers
               {:foo/bar                        (ig/ref :foo/custom)
                :foo/baz                        (ig/ref :foo/baz)
                :ataraxy.error/unmatched-path   (ig/ref :duct.handler.static/not-found)
                :ataraxy.error/unmatched-method (ig/ref :duct.handler.static/method-not-allowed)
                :ataraxy.error/missing-params   (ig/ref :duct.handler.static/bad-request)
                :ataraxy.error/missing-destruct (ig/ref :duct.handler.static/bad-request)
                :ataraxy.error/failed-coercions (ig/ref :duct.handler.static/bad-request)
                :ataraxy.error/failed-spec      (ig/ref :duct.handler.static/bad-request)}}})))))

(deftest resource-test
  (let [config   {:duct.router/ataraxy
                  {:routes   '{[:get "/"] [:foo name]}
                   :handlers {:foo resource-handler}}}
        handler  (:duct.router/ataraxy (ig/init config))
        response (handler {:request-method :get, :uri "/"})]
    (is (= (-> response :body slurp) "Hello World\n"))))
