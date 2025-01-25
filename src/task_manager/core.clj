(ns task-manager.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [task-manager.routes :refer [app-routes]]))

(defn -main []
  (run-jetty app-routes {:port 3000 :join? false}))

