FROM maven:3-jdk-11 AS builder
WORKDIR /app
COPY pom.xml /app
RUN mvn dependency:resolve -Dmaven.test.skip=true
COPY . /app
RUN mvn compile war:war

# Tomcat 10 needs further changes, see https://tomcat.apache.org/migration-10.html
FROM tomcat:9
LABEL maintainer=adrian.gschwend@zazuko.com
ENV CATALINA_OPTS="-XX:+UseSerialGC"
COPY --from=builder /app/target/lodview.war /usr/local/tomcat/webapps/lodview.war
CMD ["catalina.sh", "run"]
EXPOSE 8080 8009
