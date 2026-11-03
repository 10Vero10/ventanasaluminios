# Imagen única para Render (ADR-022): el backend sirve también el frontend compilado. Docker Compose no la usa; ahí
# siguen backend/Dockerfile y frontend/Dockerfile con nginx.

# Etapa 1: frontend
FROM node:22-alpine AS frontend
WORKDIR /app
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# Etapa 2: backend
FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /app
COPY backend/pom.xml .
RUN mvn -B -q dependency:go-offline
COPY backend/src ./src
RUN mvn -B -q package -DskipTests

# Etapa 3: ejecución
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 10001 app && mkdir -p /app/uploads && chown app /app/uploads
COPY --from=backend /app/target/*.jar app.jar
COPY --from=frontend /app/dist ./site
ENV SITE_DIR=/app/site
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
