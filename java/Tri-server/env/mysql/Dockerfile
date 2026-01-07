FROM mysql:8.0.30

ENV TZ=Asia/Shanghai

RUN ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
COPY ./1schema.sql  /docker-entrypoint-initdb.d
COPY ./conf/my.cnf  /etc/mysql/my.cnf