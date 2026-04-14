FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

# 👇 ye line add kar
RUN chmod +x mvnw

RUN ./mvnw clean package -DskipTests

CMD ["java", "-jar", "target/research-assistant-0.0.1-SNAPSHOT.jar"]