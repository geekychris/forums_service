# Build stage
FROM maven:3.9-amazoncorretto-23 AS build
WORKDIR /app
COPY pom.xml .
# Download all dependencies to optimize future builds with Docker layer caching
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src
# Build the application
RUN mvn package -DskipTests

# Runtime stage
FROM amazoncorretto:23-alpine
WORKDIR /app

# Create a non-root user to run the application
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Set the entrypoint
ENTRYPOINT ["java","-jar","/app/app.jar"]

