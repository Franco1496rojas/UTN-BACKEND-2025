# Etapa de build
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build
# Copia todo el monorepo (filtrado por .dockerignore)
COPY . .
# Construye sólo el módulo api-gateway y sus dependencias
RUN mvn -B -DskipTests clean package spring-boot:repackage -pl api-gateway -am

# Etapa runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /build/api-gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]