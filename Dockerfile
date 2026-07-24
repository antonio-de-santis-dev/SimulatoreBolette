# Immagine single-container per Fly.io: Spring Boot serve sia le API (/api/**)
# sia il frontend React buildato (classpath:/static/), sulla stessa origine.

# --- Stage 1: build del frontend React ---
FROM node:20-alpine AS frontend
WORKDIR /frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# --- Stage 2: build del backend Spring Boot con gli statici del frontend ---
FROM eclipse-temurin:17-jdk-alpine AS backend
WORKDIR /app
RUN apk add --no-cache maven
COPY backend/pom.xml .
RUN mvn -q -B dependency:go-offline
COPY backend/src ./src
# Gli asset del frontend finiscono in classpath:/static/ dentro il JAR.
COPY --from=frontend /frontend/dist ./src/main/resources/static
RUN mvn -q -B clean package -DskipTests

# --- Stage 3: runtime ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=backend /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
