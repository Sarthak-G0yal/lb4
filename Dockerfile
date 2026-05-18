FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/java-lb4-1.0-SNAPSHOT.jar /app/app.jar
COPY target/dependency /app/dependency
COPY src/main/resources/config.yaml /app/config.yaml
ENTRYPOINT ["java", "-cp", "/app/app.jar:/app/dependency/*"]