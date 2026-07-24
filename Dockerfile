# ════════════════════════════════════════════════════════════════════════════
#  Immagine unica per Fly.io: il frontend React compilato viene servito dal JAR
#  di Spring Boot (stessa origine → niente CORS, un solo container).
# ════════════════════════════════════════════════════════════════════════════

# ── STAGE 1: build del frontend React ───────────────────────────────────────
FROM node:20-alpine AS frontend-build
WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build
# Produce /app/dist

# ── STAGE 2: build del backend con il frontend tra le risorse statiche ───────
FROM eclipse-temurin:17-jdk-alpine AS backend-build
WORKDIR /app
RUN apk add --no-cache maven
# Dipendenze prima del codice: sfrutta la cache dei layer Docker
COPY backend/pom.xml .
RUN mvn dependency:go-offline -B
COPY backend/src ./src
# Il bundle React finisce in classpath:/static (servito da Spring Boot)
COPY --from=frontend-build /app/dist ./src/main/resources/static
RUN mvn clean package -DskipTests -B

# ── STAGE 3: runtime minimale ────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN apk add --no-cache wget
COPY --from=backend-build /app/target/*.jar app.jar
# Flag calibrati per una VM piccola:
#   MaxRAMPercentage  → la JVM si adatta alla RAM del container
#   UseSerialGC       → il GC parallelo spreca memoria su poche CPU
#   TieredStopAtLevel → avvio piu' rapido
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
