FROM openjdk:11-jdk-slim as builder
WORKDIR /app
COPY . /app
RUN ./gradlew clean build

FROM openjdk:11-jdk-slim
LABEL maintainer=g.nespolino@gmail.com
COPY --from=builder /app/build/libs/lodview.jar /lodview.jar
CMD ["java", "-jar", "/lodview.jar"]
EXPOSE 8080 8009
