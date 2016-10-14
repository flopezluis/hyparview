(ns hyparview.manager-test
  (:require [clojure.test :refer :all]
            [org.senatehouse.expect-call :refer :all]
            [hyparview.manager :refer :all]
            [hyparview.transport :refer :all]
            [manifold.stream :as s]))

(def member1 {:id "127.0.0.1:5557"})
(def member2 {:id "127.0.0.1:5556"})

(defn load-fixtures [] 
  (add-to-active-view member1)
  (add-to-active-view member2))

(deftest test-is-there-a-slot-free?
  (testing "Tests if there are free slots in views"
    (load-fixtures)
    (is (= (is-there-a-slot-free? :active-view {:id "127.0.0.1:5555"}) true))
    (is (= (is-there-a-slot-free? :active-view {:id "127.0.0.1:5556"} ) false))))

(deftest test-forward-join
  (testing "that the forward is sent to the active view"
    (load-fixtures)
    (expect-call [(send-forward-join [{:id "127.0.0.1:5557"} {:id "localhost:5555"} _])
                  (send-forward-join [{:id "127.0.0.1:5556"} {:id "localhost:5555"} _])]
      (forward-join {:id "localhost:5555"}))))
