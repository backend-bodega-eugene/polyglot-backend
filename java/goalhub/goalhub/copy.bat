@echo off

rmdir /s /q release
mkdir release\jars

copy gateway-service\target\gateway-service-0.0.1-SNAPSHOT.jar release\jars\gateway-service.jar
copy user-service\target\user-service-0.0.1-SNAPSHOT.jar release\jars\user-service.jar
copy match-service\target\match-service-0.0.1-SNAPSHOT.jar release\jars\match-service.jar
copy order-service\target\order-service-0.0.1-SNAPSHOT.jar release\jars\order-service.jar
copy admin-service\target\admin-service-0.0.1-SNAPSHOT.jar release\jars\admin-service.jar

dir release\jars

pause