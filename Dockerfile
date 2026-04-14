FROM openjdk:21-jdk-slim

WORKDIR /app

COPY . .

RUN ./mvnw clean package -DskipTests

CMD ["java", "-jar", "target/research-assistant-0.0.1-SNAPSHOT.jar"]