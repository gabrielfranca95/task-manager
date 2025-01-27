(ns task-manager.schema
  (:require [clojure.spec.alpha :as s]
            [spec-tools.core :as st]
            [clojure.string :as str]))

(def valid-status #{"pendente" "concluido"})

(defn extra-keys? [spec body]
  (let [decode-result (st/decode spec body st/fail-on-extra-keys-transformer)]
    (= :clojure.spec.alpha/invalid decode-result)))

(defn validate-data [data schema]
  (and (= "Success!\n" (s/explain-str schema data)) (not (extra-keys? schema data))))

(defn explain-data [data schema]
  (let [explained-schema (s/explain-str schema data)
        keyword-with-error (or (second (re-find #"\(contains\?\s*%\s*:(\w+)\)" explained-schema))
                               (second (re-find #"in:\s*\[:(\w+)\]" explained-schema))
                               (re-find #"\bnot-empty\b" explained-schema))]
    (case keyword-with-error
      "titulo" "O campo 'titulo' é obrigatório"
      "status" "Erro ao enviar o status, certifique-se de enviar um dos valores: pendente ou concluido"
      "not-empty" "Não é permitido enviar um corpo vazio para atualizar uma tarefa"
      "Erro no formato da requisição, verifique se há algum campo faltando, ou algum campo a mais")))

(s/def ::titulo (s/and string? #(not (str/blank? %))))
(s/def ::descricao string?)
(s/def ::status (fn [status] (contains? valid-status status)))
(s/def ::create-task (s/keys :req-un [::titulo]
                             :opt-un [::descricao]))
(s/def ::update-task (s/and (s/keys :opt-un [::titulo ::status ::descricao]) not-empty))