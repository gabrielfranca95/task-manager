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
