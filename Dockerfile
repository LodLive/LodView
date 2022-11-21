FROM openjdk:11-jdk-slim as builder
WORKDIR /app
COPY . /app
RUN ./gradlew clean build

# Execute container as user.
FROM openjdk:11-jdk-slim
LABEL maintainer=g.nespolino@gmail.com
USER 1001
COPY --from=builder /app/build/libs/lodview.war /lodview.war
CMD ["java", "-jar", "/lodview.war"]
EXPOSE 8080 8009
