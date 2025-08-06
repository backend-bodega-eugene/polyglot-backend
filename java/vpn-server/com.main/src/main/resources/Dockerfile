FROM openjdk:11.0.16-jre
MAINTAINER admin

ENV PARAMS=""

ADD app.jar /app.jar
RUN sh -c 'touch /app.jar'
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo 'Asia/Shanghai' >/etc/timezone
ENV JAVA_OPTS="-Xmx256m -Xms256m -Xmn128m -Xss256k"

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar $PARAMS /app.jar" ]
