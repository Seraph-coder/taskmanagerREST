# Используем официальный образ Eclipse Temurin OpenJDK 21
FROM eclipse-temurin:21-jdk

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем JAR файл приложения
COPY target/taskmanager-0.0.1-SNAPSHOT.jar app.jar

# Открываем порт 8080
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]
