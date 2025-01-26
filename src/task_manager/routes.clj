(ns task-manager.routes
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :refer [response status]]
            [cheshire.core :as json]))

;; Banco de dados em memória
(def tasks (atom {}))
(defn generate-id [] (str (java.util.UUID/randomUUID)))

;; Endpoints
(defroutes app-routes
           ;; Criar uma nova tarefa
           (POST "/tasks" req
             (let [task (json/parse-string (slurp (:body req)) true)]
               (if (contains? task :titulo)
                 (let [id (generate-id)
                       new-task (assoc task :id id :status "pendente")]
                   (swap! tasks assoc id new-task)
                   (response (json/generate-string new-task)))
                 (-> (response (json/generate-string {:error "O campo 'titulo' é obrigatório"}))
                     (status 400)))))

           ;; Listar todas as tarefas
           (GET "/tasks" []
             (if (empty? @tasks)
               (response (json/generate-string {:message "Nenhuma tarefa encontrada"}))
               (response (json/generate-string (vals @tasks)))))


           ;; Atualizar uma tarefa
           (PUT "/tasks/:id" [id :as req]
             (if-let [task (get @tasks id)]
               (let [updated-task (merge task (json/parse-string (slurp (:body req)) true))]
                 (swap! tasks assoc id updated-task)
                 (response (json/generate-string updated-task)))
               ;; Caso o ID seja inválido
               (-> (response (json/generate-string {:error "Tarefa não encontrada"}))
                   (status 404))))

           ;; Deletar uma tarefa
           (DELETE "/tasks/:id" [id]
             (if (contains? @tasks id)
               (do
                 (swap! tasks dissoc id)
                 (response (json/generate-string {:message "Tarefa deletada"})))
               (-> (response (json/generate-string {:error "Tarefa não encontrada"}))
                   (status 404))))

           ;; Rota de erro para endpoints não definidos
           (route/not-found (json/generate-string {:error "Endpoint não encontrado"})))
