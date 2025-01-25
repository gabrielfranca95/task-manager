# Usando a imagem base do Clojure
FROM clojure:openjdk-17-lein-slim

# Configura o diretório de trabalho
WORKDIR /app

# Copia os arquivos do projeto para dentro do container
COPY . .

# Instala as dependências do projeto
RUN lein deps

# Expõe a porta 3000, onde o app vai rodar
EXPOSE 3000

# Comando para rodar o app
CMD ["lein", "run"]

