FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY user-service/pom.xml user-service/pom.xml
COPY habit-service/pom.xml habit-service/pom.xml
# jOOQ генерирует таблицы из этого файла, поэтому он нужен на этапе сборки.
COPY db/init.sql db/init.sql
COPY tracking-service/pom.xml tracking-service/pom.xml

RUN mvn -B -q -pl habit-service -am dependency:go-offline

COPY habit-service/src habit-service/src
RUN mvn -B -q -pl habit-service -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S app && adduser -S app -G app

WORKDIR /app
COPY --from=build /workspace/habit-service/target/habit-service-1.0.0.jar app.jar

USER app
EXPOSE 8082
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
