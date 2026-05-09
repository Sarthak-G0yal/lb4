FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/java-lb4-1.0-SNAPSHOT.jar /app/app.jar
ENTRYPOINT ["java", "-cp", "/app/app.jar", "org.lb4.loadbalancer.tools.TcpEchoServer"]