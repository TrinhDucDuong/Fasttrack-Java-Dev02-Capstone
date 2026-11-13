FROM eclipse-temurin:17-jre-jammy

WORKDIR /app
ARG SERVICE
COPY ${SERVICE}/target/${SERVICE}-1.0.0.jar app.jar
USER 10001:10001
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70", "-XX:InitialRAMPercentage=20", "-jar", "/app/app.jar"]

