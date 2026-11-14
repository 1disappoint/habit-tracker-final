FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /workspace

COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY user-service/pom.xml user-service/pom.xml
COPY habit-service/pom.xml habit-service/pom.xml

RUN mvn -B -q -pl api-gateway -am dependency:go-offline

COPY api-gateway/src api-gateway/src
RUN mvn -B -q -pl api-gateway -am package -DskipTests

FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S app && adduser -S app -G app

WORKDIR /app
COPY --from=build /workspace/api-gateway/target/api-gateway-1.0-SNAPSHOT.jar app.jar

USER app
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
