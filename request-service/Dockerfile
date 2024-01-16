FROM openjdk:17-oracle

MAINTAINER LGALI TEAM <dev.teams@lgalidz.com>

WORKDIR /opt

ADD target/*.jar app.jar

ENV JAVA_OPTS "-Djava.security.egd=file:/dev/./urandom"

CMD ["java", "-jar", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=9080", "/opt/app.jar"]
