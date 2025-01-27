# Task Manager API

Bem-vindo à **Task Manager API**, um projeto desenvolvido em Clojure para gerenciar tarefas com operações básicas de CRUD (Create, Read, Update e Delete). 

---

## 0. Requisitos para iniciar

- **Java**: Instale uma versão entre **8** e **17** para evitar problemas de incompatibilidade com as bibliotecas, já que o projeto foi testado apenas entre essas versões.
- **Leiningen**: Utilize a versão mais recente do **Leiningen**.
- **Clojure**: Instale a versão mais recente do Clojure.
- **IDE para edição do código**: Pode-se usar qualquer IDE de sua preferência, como **IntelliJ IDEA** ou editores de terminal como **Vim** ou **Nano**. Este código foi desenvolvido no **IntelliJ IDEA**.

---

## 1. Primeiros passos

### Clonando o projeto

Clone o projeto em um diretório e navegue via terminal de comando até a raiz do projeto


Lá vc encontrará uma estrutura de diretórios padrão para a execução da API.

---

## 2. Estrutura do projeto

Aqui estão os primeiros arquivos que você irá utilizar:

- **`core.clj`**: Contém o ponto de entrada da aplicação e é responsável por iniciar o servidor.
- **`schema.clj`**: Define os schemas (validações) da API usando **Clojure Specs**.
- **`routes.clj`**: Contém toda a lógica funcional da API, como criação, listagem, atualização e exclusão de tarefas.

---

## 3. Configurando o servidor

No arquivo `core.clj`, localizado em `src/task_manager/core.clj`, o servidor Jetty é configurado para rodar a API. O código é o seguinte:

```clojure
(ns task-manager.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [task-manager.routes :refer [app-routes]]))

(defn -main []
  (run-jetty app-routes {:port 3000 :join? false}))
```


**Ring**:
    - O Ring é uma biblioteca Clojure usada para construir aplicativos web. Ele é comparável ao **Express** no Node.js ou ao **Flask** no Python.
    - Ele define uma interface simples para lidar com requisições HTTP.

---

## 4. Validações com schemas

Nosso código irá fazer uso das specs em Clojure. Elas são schemas que funcionam como aqueles brinquedos de encaixar formas geométricas, só permitindo passar a forma que se encaixa nos padrões. Vamos precisar disso para impedir que tentem criar uma tarefa sem um título, por exemplo, ou tentem adicionar algo diferente de pendente ou concluído no status. Existem outras formas de criar validações, porém optei pelos schemas por ser algo muito utilizado, escalável e reutilizável por outros arquivos no futuro, caso necessário. Após essa introdução com exemplos geométricos emblemáticos (rs), a localização dos schemas deve estar no mesmo nível de core.clj, com o nome de schemas.cljc.

### Código dos schemas

```clojure
(ns task-manager.schema
  (:require [clojure.spec.alpha :as s]
            [spec-tools.core :as st]
            [clojure.string :as str]))

(def valid-status #{"pendente" "concluido"})

(s/def ::titulo (s/and string? #(not (str/blank? %))))
(s/def ::descricao string?)
(s/def ::status (fn [status] (contains? valid-status status)))
(s/def ::create-task (s/keys :req-un [::titulo] :opt-un [::descricao]))
(s/def ::update-task (s/and (s/keys :opt-un [::titulo ::status ::descricao]) not-empty))
```

- **`valid-status`**: Define os valores permitidos para o campo `status`.
- **`s/def`**: Cria uma validação (spec). Por exemplo:
    - `::titulo`: Deve ser uma string não vazia.
    - `::create-task`: Define os campos obrigatórios e opcionais para criar uma tarefa.
    - `::update-task`: Valida os campos enviados para atualizar uma tarefa.

---

## 5. Lógica funcional (routes.clj)

O arquivo `routes.clj` contém toda a lógica da API. Ele é responsável por processar as requisições HTTP e executar as operações no banco de dados (em memória).

Aqui está um resumo das rotas implementadas:

1. **Criar uma nova tarefa (`POST /tasks`)**:
    - Recebe um corpo JSON com os campos `titulo` e, opcionalmente, `descricao`.
    - Valida os dados com os schemas.
    - Gera um ID único e salva a tarefa com status inicial "pendente".

2. **Listar todas as tarefas (`GET /tasks`)**:
    - Retorna uma lista de todas as tarefas armazenadas no banco de dados em memória.

3. **Atualizar uma tarefa (`PUT /tasks/:id`)**:
    - Recebe um corpo JSON com os campos que devem ser atualizados.
    - Valida os dados com os schemas e aplica as mudanças na tarefa especificada pelo ID.

4. **Deletar uma tarefa (`DELETE /tasks/:id`)**:
    - Remove a tarefa correspondente ao ID informado.

### Exemplo de rota

```clojure
(POST "/tasks" req
  (let [task (json/parse-string (slurp (:body req)) true)]
    (if (schema/validate-data task ::schema/create-task)
      (let [id (generate-id)
            new-task (assoc task :id id :status "pendente")]
        (swap! tasks assoc id new-task)
        (response (json/generate-string new-task)))
      (-> (response (json/generate-string {:error (schema/explain-data task ::schema/create-task)}))
          (status 400)))))
```

### Banco de dados

Utilizamos um **Atom** como banco de dados em memória. Átomos permitem mutabilidade mas calma, rs essa mutabilidade é  controlada em Clojure, sendo ideais para cenários como este.

---


## 6. Testes da Aplicação

Os testes da aplicação estão localizados na pasta `tests`, no arquivo `core_test.clj`. Optei por utilizar as bibliotecas `clojure.test` e `ring.mock` para facilitar a criação dos testes e cobrir os casos de uso da API.

### Principais Funcionalidades Testadas

- **Criação de Tarefa (`POST /tasks`)**:
    - Criar uma tarefa com sucesso, retornando status 200 e validando os campos retornados.
    - Retornar erro ao tentar criar uma tarefa sem o campo obrigatório `titulo` (status 400).
    - Retornar erro ao enviar campos extras no corpo da requisição (status 400).

- **Listagem de Tarefas (`GET /tasks`)**:
    - Listar todas as tarefas com sucesso, retornando status 200 e verificando se o corpo contém tarefas.

- **Atualização de Tarefa (`PUT /tasks/:id`)**:
    - Atualizar uma tarefa existente com sucesso, retornando status 200 e validando os campos atualizados.
    - Retornar erro ao tentar atualizar uma tarefa inexistente (status 404).
    - Retornar erro ao enviar um status inválido (diferente de "pendente" ou "concluido") (status 400).
    - Retornar erro ao enviar um corpo vazio na atualização (status 400).

- **Deleção de Tarefa (`DELETE /tasks/:id`)**:
    - Deletar uma tarefa existente com sucesso, retornando status 200 e uma mensagem confirmando a exclusão.
    - Retornar erro ao tentar deletar uma tarefa inexistente (status 404).


### Como Executar os Testes

Para rodar os testes, utilize o Leiningen no terminal com o seguinte comando na pasta raiz do projeto:

```bash
lein test
```

---
## 7. Como Rodar a API Localmente

Para usar a API localmente, é necessário iniciar o projeto utilizando o **Leiningen**. Siga os passos abaixo:

### Iniciando o Servidor Localmente

No terminal, navegue até a pasta raiz do projeto e execute o seguinte comando:

```bash
lein run
```

Abra outra aba no terminal e teste
alguns exemplos de requisições para criar, editar, listar tarefas e testar os casos de falha.

#### Criar tarefa:
```
curl -X POST -H "Content-Type: application/json" \
-d '{"titulo": "Estudar Kafka", "descricao": "Ler algum livro relacionado."}' \
http://localhost:3000/tasks

```

#### Listar tarefas
```
curl -X GET http://localhost:3000/tasks
```

#### Atualizar tarefa
```
curl -X PUT -H "Content-Type: application/json" \
-d '{"titulo": "Estudar Clojure Avançado", "status": "concluido"}' \
http://localhost:3000/tasks/TASK_ID

```
#### Excluir uma tarefa
```
curl -X DELETE http://localhost:3000/tasks/TASK_ID
```


---

## 8. Tornando a API Pública

A API deste projeto já está publicada. Caso queira testá-la, pule para o próximo passo. Se preferir criar sua própria API pública, siga o guia abaixo.

### Por que Escolhi o Render?

Para tornar a API disponível para qualquer pessoa fazer requisições, utilizei o Render. É uma opção gratuita para criar servidores e proxies. Não há uma razão específica para ter escolhido o Render, já que existem outras opções muito boas como o Vercel. Mas como recentemente o utilizei para criar um proxy, então achei interessante utilizar também neste projeto.

### Passo 1: Configurando o Docker

Para tornar a API pública, será necessário usar o Docker para conectar o projeto ao Render. O Docker facilita muito o gerenciamento do projeto, e neste caso, utilizei uma imagem base que já contém o Leiningen instalado.

#### Explicação do Arquivo Docker

```dockerfile
# Usando a imagem base do Clojure com Leiningen já instalado
FROM clojure:lein

# Configura o diretório de trabalho
WORKDIR /app

# Copia os arquivos do projeto para o contêiner
COPY . /app

# Expondo a porta
EXPOSE 3000

# Comando para rodar o servidor (substitua com o comando correto)
CMD ["lein", "run"]
```

- **FROM**: Especifica a imagem base, neste caso, `clojure:lein`, que já vem com o Leiningen instalado.
- **WORKDIR**: Define o diretório de trabalho dentro do contêiner como `/app`.
- **COPY**: Copia todos os arquivos do projeto para o diretório de trabalho no contêiner.
- **EXPOSE**: Expõe a porta 3000 para acesso externo.
- **CMD**: Define o comando padrão para iniciar o servidor da aplicação, utilizando o Leiningen.

### Passo 2: Conectando o Render ao Código no GitHub

1. Acesse o site do [Render](https://render.com/) e crie uma conta, caso ainda não tenha.
2. Conecte sua conta do Render ao repositório do GitHub onde o código está hospedado.
3. Crie um novo serviço no Render, selecionando a opção de "Web Service".
4. Configure o serviço para usar o Dockerfile do projeto e defina a porta 3000 como a porta de entrada.
5. Após a configuração, o Render irá construir a imagem do Docker e disponibilizar a API em um domínio público.

---
## 10. Como utilizar a api publica:

A api está aberta no endpoint 

#### Criar tarefa:
```
curl -X POST -H "Content-Type: application/json" \
-d '{"titulo": "Estudar Clojure", "descricao": "Aprender mais sobre a linguagem."}' \
https://task-manager-32ru.onrender.com/tasks
```

#### Listar tarefas
```
curl -X GET https://task-manager-32ru.onrender.com/tasks
```

#### Atualizar tarefa
```
curl -X PUT -H "Content-Type: application/json" \
-d '{"titulo": "Estudar Clojure Avançado", "status": "concluido"}' \
https://task-manager-32ru.onrender.com/tasks/TASK_ID

```
#### Excluir uma tarefa
```
curl -X DELETE https://task-manager-32ru.onrender.com/tasks/TASK_ID
```


---
---
