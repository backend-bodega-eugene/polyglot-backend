# GoalHub Backend

GoalHub 是一个基于 Spring Boot、Spring Cloud Gateway、Nacos、MyBatis-Plus 和 MySQL 的多模块后端项目。

## 项目结构

```text
goalhub
├── common
├── gateway-service
├── user-service
├── match-service
├── admin-service
├── docker
├── docker-compose.yml
└── pom.xml
```

## 模块说明

| 模块 | 说明 |
| --- | --- |
| `common` | 公共模块，存放 DTO、统一响应、错误码、业务异常和 JWT 工具类。 |
| `gateway-service` | 网关服务，负责统一入口、路由转发、跨域配置和 JWT 鉴权。 |
| `user-service` | 用户服务，负责用户注册、登录、密码校验和用户基础数据管理。 |
| `match-service` | 比赛服务，负责足球联赛、比赛列表、比赛详情、热门比赛和用户关注赛事。 |
| `admin-service` | 后台管理服务，负责管理员登录、后台用户管理、菜单管理和应用用户管理。 |
| `docker` | Docker 持久化目录和初始化脚本目录。 |
| `docker-compose.yml` | 本地依赖服务编排，包含 Nacos 和 MySQL。 |
| `pom.xml` | Maven 父工程，统一管理子模块和依赖版本。 |

## 服务端口

| 服务 | 端口 |
| --- | --- |
| `gateway-service` | `8000` |
| `user-service` | `9001` |
| `match-service` | `9002` |
| `admin-service` | `9003` |
| `nacos` | `8848` |
| `mysql` | `3306` |
