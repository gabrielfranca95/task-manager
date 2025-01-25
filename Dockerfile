# Usando a imagem base do OpenJDK 17
FROM openjdk:17-slim

# Instalando o Leiningen
RUN apt-get update && apt-get install -y curl \
    && curl -O https://github.com/technomancy/leiningen/releases/download/2.9.8/leiningen-2.9.8-standalone.jar \
    && mv leiningen-2.9.8-standalone.jar /usr/local/bin/lein \
    && chmod +x /usr/local/bin/lein

# Configura o diretório de trabalho
WORKDIR /app

# Copia os arquivos do projeto para o contêiner
COPY . /app

# Expondo a porta
EXPOSE 3000

# Comando para rodar o servidor (substitua com o comando correto)
CMD ["lein", "run"]
