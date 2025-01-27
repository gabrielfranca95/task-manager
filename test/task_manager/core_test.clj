(ns task-manager.core-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [cheshire.core :as json]
            [task-manager.routes :refer [app-routes]]))

;; Helper para parsear JSON das respostas
(defn parse-json [response]
  (json/parse-string (:body response) true))

(deftest test-create-task
  (testing "Criar uma nova tarefa com sucesso"
    (let [response (-> (mock/request :post "/tasks")
                       (mock/body (json/generate-string {:titulo "Minha tarefa"}))
                       app-routes)
          body (parse-json response)]
      (is (= 200 (:status response)))
      (is (= "Minha tarefa" (:titulo body)))
      (is (= "pendente" (:status body)))
      (is (contains? body :id))))

  (testing "Falha ao criar tarefa sem campo obrigatório"
    (let [response (-> (mock/request :post "/tasks")
                       (mock/body (json/generate-string {:descricao "Sem título"}))
                       app-routes)
          body (parse-json response)]
      (is (= 400 (:status response)))
      (is (= "O campo 'titulo' é obrigatório" (:error body)))))

  (testing "Falha ao criar tarefa com campo a mais no body"
    (let [response (-> (mock/request :post "/tasks")
                       (mock/body (json/generate-string {:titulo "Com título"
                                                         :cake "bolinho"}))
                       app-routes)
          body (parse-json response)]
      (is (= 400 (:status response)))
      (is (= "Erro no formato da requisição, verifique se há algum campo faltando, ou algum campo a mais" (:error body))))))

(deftest test-get-tasks
  (testing "Listar todas as tarefas"
    ;; Criar uma tarefa antes de testar
    (-> (mock/request :post "/tasks")
        (mock/body (json/generate-string {:titulo "Tarefa teste"}))
        app-routes)
    (let [response (-> (mock/request :get "/tasks")
                       app-routes)
          body (parse-json response)]
      (is (= 200 (:status response)))
      (is (not (empty? body)))
      (is (= "Tarefa teste" (get-in (first body) [:titulo]))))))

(deftest test-update-task
  (testing "Atualizar uma tarefa existente"
    ;; Criar uma tarefa antes de testar
    (let [create-response (-> (mock/request :post "/tasks")
                              (mock/body (json/generate-string {:titulo "Tarefa antiga"}))
                              app-routes)
          task-id (:id (parse-json create-response))
          update-response (-> (mock/request :put (str "/tasks/" task-id))
                              (mock/body (json/generate-string {:titulo "Tarefa atualizada"}))
                              app-routes)
          updated-task (parse-json update-response)]
      (is (= 200 (:status update-response)))
      (is (= "Tarefa atualizada" (:titulo updated-task)))))

  (testing "Falha ao atualizar uma tarefa inexistente"
    (let [response (-> (mock/request :put "/tasks/ID_INVALIDO")
                       (mock/body (json/generate-string {:titulo "Tarefa inválida"}))
                       app-routes)
          body (parse-json response)]
      (is (= 404 (:status response)))
      (is (= "Tarefa não encontrada" (:error body)))))

  (testing "Falha ao atualizar status com algo diferente de pendente ou concluido"
    (let [create-response (-> (mock/request :post "/tasks")
                              (mock/body (json/generate-string {:titulo "Tarefa antiga"}))
                              app-routes)
          task-id (:id (parse-json create-response))
          response (-> (mock/request :put (str "/tasks/" task-id))
                       (mock/body (json/generate-string {:titulo "Tarefa com status inválido"
                                                         :status "invalido"}))
                       app-routes)
          body (parse-json response)]
      (is (= 400 (:status response)))
      (is (= "Erro ao enviar o status, certifique-se de enviar um dos valores: pendente ou concluido" (:error body)))))

  (testing "Falha ao atualizar task com body vazio"
    (let [create-response (-> (mock/request :post "/tasks")
                              (mock/body (json/generate-string {:titulo "Tarefa antiga"}))
                              app-routes)
          task-id (:id (parse-json create-response))
          response (-> (mock/request :put (str "/tasks/" task-id))
                       (mock/body (json/generate-string {}))
                       app-routes)
          body (parse-json response)]
      (is (= 400 (:status response)))
      (is (= "Não é permitido enviar um corpo vazio para atualizar uma tarefa"
             (:error body))))))

(deftest test-delete-task
  (testing "Deletar uma tarefa existente"
    ;; Criar uma tarefa antes de testar
    (let [create-response (-> (mock/request :post "/tasks")
                              (mock/body (json/generate-string {:titulo "Tarefa a deletar"}))
                              app-routes)
          task-id (:id (parse-json create-response))
          delete-response (-> (mock/request :delete (str "/tasks/" task-id))
                              app-routes)
          delete-body (parse-json delete-response)]
      (is (= 200 (:status delete-response)))
      (is (= "Tarefa deletada" (:message delete-body)))))

  (testing "Falha ao deletar uma tarefa inexistente"
    (let [response (-> (mock/request :delete "/tasks/ID_INVALIDO")
                       app-routes)
          body (parse-json response)]
      (is (= 404 (:status response)))
      (is (= "Tarefa não encontrada" (:error body))))))

(deftest test-not-found
  (testing "Endpoint não encontrado"
    (let [response (-> (mock/request :get "/invalid-endpoint")
                       app-routes)
          body (parse-json response)]
      (is (= 404 (:status response)))
      (is (= "Endpoint não encontrado" (:error body))))))

(deftest test-task-validation
  (testing "Cada tarefa deve ter um ID único"
    (let [response1 (-> (mock/request :post "/tasks")
                        (mock/body (json/generate-string {:titulo "Tarefa 1"}))
                        app-routes)
          response2 (-> (mock/request :post "/tasks")
                        (mock/body (json/generate-string {:titulo "Tarefa 2"}))
                        app-routes)
          id1 (:id (parse-json response1))
          id2 (:id (parse-json response2))]
      (is (not= id1 id2))))

  (testing "Tarefa aceita descrição opcional"
    (let [response (-> (mock/request :post "/tasks")
                       (mock/body (json/generate-string {:titulo "Tarefa com descrição"
                                                         :descricao "Detalhes da tarefa"}))
                       app-routes)
          body (parse-json response)]
      (is (= 200 (:status response)))
      (is (= "Tarefa com descrição" (:titulo body)))
      (is (= "Detalhes da tarefa" (:descricao body))))))