FROM tomcat
MAINTAINER adrian.gschwend@zazuko.com

RUN cd /usr/local/tomcat/webapps/ && \
    curl -Ls $(curl -s https://api.github.com/repos/zazukoians/LodView/releases | grep browser_download_url | head -n 1 | cut -d '"' -f 4) > lodview.war

CMD ["catalina.sh", "run"]
EXPOSE 8080 8009
