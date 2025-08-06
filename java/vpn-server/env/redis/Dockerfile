FROM redis:latest

ENV TZ=Asia/Shanghai
RUN ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ADD ./redis.conf /usr/local/etc/redis/
CMD redis-server /usr/local/etc/redis/redis.conf