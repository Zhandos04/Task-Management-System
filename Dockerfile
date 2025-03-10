FROM gradle:7.6.0-jdk17 AS build
COPY . /app
WORKDIR /app
RUN gradle build -x test


FROM openjdk:21-slim
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
