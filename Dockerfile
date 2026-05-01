FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests -pl tutor-link-web -am

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/tutor-link-web/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
