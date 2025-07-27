# Use a lightweight JDK base image
FROM eclipse-temurin:17-jdk-jammy

# Set the working directory inside the container
WORKDIR /app

# Copy the jar into the container
COPY target/*.jar app.jar

# Expose the port (Spring Boot default is 8080)
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
