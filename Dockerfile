# ==============================================================================
# BUILD
# ==============================================================================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

# OTIMIZAÇÃO DE CACHE:
# Copia apenas arquivos de configuração primeiro.
# Se você não mudou o pom.xml, o Docker reutiliza o cache desta camada
# e não baixa as dependências novamente.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código fonte e compila
COPY src ./src
RUN mvn clean package -DskipTests

# Extrai o JAR para nome padrão
RUN cp target/*.jar app.jar

# ==============================================================================
# RUNTIME
# ==============================================================================
FROM eclipse-temurin:21-jre-jammy

# Cria um grupo e usuário de sistema para não rodar como root
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

WORKDIR /app

# Cria diretório de uploads com permissões corretas
RUN mkdir -p /app/uploads && chown -R appuser:appgroup /app/uploads

# Copia apenas o artefato do estágio de build (Redução de tamanho)
COPY --from=build --chown=appuser:appgroup /workspace/app.jar ./app.jar

# PERFORMANCE JAVA EM CONTAINER:
# -XX:MaxRAMPercentage=75.0: O Java detecta a RAM do Container e usa 75%
# Deixa 25% para o SO e para o ONNX Runtime
ENV JAVA_OPTS="-XX:+UseZGC -XX:+ZGenerational -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Muda para o usuário restrito
USER appuser

# Documenta a porta exposta
EXPOSE 10808

# ENTRYPOINT em modo Exec (JSON Array) para passar sinais de SO (SIGTERM)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]