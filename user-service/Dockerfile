# Берем готовый образ с Java 17
FROM eclipse-temurin:17-jre-alpine

# Создаем рабочую папку внутри контейнера
WORKDIR /app

# Копируем собранный jar-файл в контейнер
COPY target/user-service-1.0-SNAPSHOT.jar app.jar

# Запускаем приложение
CMD ["java", "-jar", "app.jar"]