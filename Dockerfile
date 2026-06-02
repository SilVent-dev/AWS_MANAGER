# ===================== Stage 1: Build =====================
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copia pom.xml primeiro para cachear dependências
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copia código fonte e compila
COPY src ./src
RUN mvn clean package -DskipTests -q

# ===================== Stage 2: Runtime =====================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Cria usuário não-root por segurança
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copia JAR do stage de build
COPY --from=builder /app/target/*.jar app.jar

# Ajusta permissões
RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -q --spider http://localhost:8080/api/actuator/health || exit 1

# JVM otimizada para containers
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
