# GoalHub Backend

GoalHub Backend 是一个基于 Spring Boot 3、Spring Cloud Gateway、Nacos、MyBatis-Plus、MySQL 和 MongoDB 的多模块后端项目。

项目按业务边界拆分为网关、用户、比赛、订单和后台管理服务，公共 DTO、统一响应、异常、JWT 工具和基础日志能力由公共模块复用。

## 技术栈

| 技术 | 说明 |
| --- | --- |
| Java 21 | 项目统一 Java 版本。 |
| Spring Boot 3.5.x | 服务基础框架。 |
| Spring Cloud 2025.x | 微服务基础能力。 |
| Spring Cloud Gateway | API 网关和路由转发。 |
| Spring Cloud Alibaba Nacos | 服务注册与发现。 |
| MyBatis-Plus | 数据访问层。 |
| MySQL 8.4 | 业务数据存储。 |
| MongoDB 8.0 | 业务日志、异常日志和系统日志存储。 |
| springdoc-openapi | OpenAPI / Swagger 文档。 |
| Maven | 多模块构建管理。 |

## 项目结构

```text
goalhub
├── admin-service       # 后台管理服务
├── boot                # 通用启动和日志能力
├── common              # 公共 DTO、响应、异常、工具类
├── gateway-service     # 网关服务
├── match-service       # 比赛服务
├── order-service       # 订单服务
├── user-service        # 用户服务
├── docker              # 本地依赖服务数据和初始化脚本目录
├── docker-compose.yml  # 本地 Nacos、MySQL、MongoDB 编排
├── pom.xml             # Maven 父工程
└── readme.md
```

## 模块说明

| 模块 | 职责 |
| --- | --- |
| `common` | 公共 DTO、分页响应、统一响应、错误码、业务异常、JWT 工具等。 |
| `boot` | 通用基础能力，目前主要承载日志相关复用能力。 |
| `gateway-service` | 统一入口、路由转发、跨域处理、JWT 鉴权和可信身份头写入。 |
| `user-service` | 用户注册、登录、用户账户、账户流水和内部后台用户管理。 |
| `match-service` | 足球联赛、球队、比赛、国际化、玩法、比赛投注选项、比赛结果和用户关注。 |
| `order-service` | 投注订单、订单明细、审核、冻结、结算和账户余额远程调用。 |
| `admin-service` | 管理员登录、菜单权限、后台用户、应用用户、比赛和订单后台转发管理。 |

## 服务端口

| 服务 | 端口 |
| --- | --- |
| `gateway-service` | `8000` |
| `user-service` | `9001` |
| `match-service` | `9002` |
| `admin-service` | `9003` |
| `order-service` | `9004` |
| `nacos` | `8848` |
| `mysql` | `3306` |
| `mongodb` | `27017` |

## 网关路由

| 外部路径 | 目标服务 |
| --- | --- |
| `/api/user/**` | `user-service` |
| `/api/soccer/**` | `match-service` |
| `/api/order/**` | `order-service` |
| `/admin/**` | `admin-service` |

## 本地依赖

项目根目录提供 `docker-compose.yml`，用于启动本地依赖：

```bash
docker compose up -d
```

默认依赖配置：

| 依赖 | 地址 | 账号 | 密码 |
| --- | --- | --- | --- |
| Nacos | `http://127.0.0.1:8848` | 无鉴权 | 无鉴权 |
| MySQL | `127.0.0.1:3306` | `goalhub` | `goalhub123456` |
| MongoDB | `127.0.0.1:27017` | `root` | `root123456` |

MySQL 默认数据库为 `goalhub_user`。初始化 SQL 可以放到 `docker/mysql/init`，容器首次启动时会自动执行。

## 启动顺序

建议按以下顺序启动：

1. 启动本地依赖：`docker compose up -d`
2. 启动 `user-service`
3. 启动 `match-service`
4. 启动 `order-service`
5. 启动 `admin-service`
6. 启动 `gateway-service`

服务启动后会注册到 Nacos，网关通过服务名进行负载转发。

## 常用命令

在项目根目录执行：

```bash
# 构建全部模块
mvn clean package

# 跳过测试构建
mvn clean package -DskipTests

# 只构建指定服务及其依赖
mvn clean package -pl user-service -am
```

## API 文档

启用 springdoc 的服务可以访问本地 Swagger：

| 服务 | Swagger UI |
| --- | --- |
| `user-service` | `http://127.0.0.1:9001/swagger-ui.html` |
| `admin-service` | `http://127.0.0.1:9003/swagger-ui.html` |
| `order-service` | `http://127.0.0.1:9004/swagger-ui.html` |

`match-service` 当前配置文件未显式配置 springdoc 路径，如需访问可按模块依赖和配置补充。

## 开发约定

- 统一响应使用 `response.Result<T>`。
- 业务异常使用 `exception.BusinessException`。
- 错误码统一维护在 `response.ResultCode`。
- Controller 和 DTO 使用 Swagger / OpenAPI 注解补充接口文档。
- Service、Mapper、Entity 使用标准 JavaDoc 注释说明职责、参数和返回值。
- Feign 调用返回 `Result` 时应校验远程调用是否成功。
- 涉及金额、结算、审核、状态流转的代码应显式校验状态和参数。
- 涉及余额变更的逻辑应使用事务，并在必要时使用数据库行锁。

## 说明

本项目仍在持续迭代中，部分错误码、接口权限、数据初始化脚本和接口文档可能会随业务推进继续补充。
