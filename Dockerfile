FROM maven:3-jdk-8 AS builder
WORKDIR /app
COPY . /app
RUN mvn compile war:war

FROM tomcat:7
LABEL maintainer=adrian.gschwend@zazuko.com
COPY --from=builder /app/target/lodview.war /usr/local/tomcat/webapps/lodview.war
CMD ["catalina.sh", "run"]
EXPOSE 8080 8009
