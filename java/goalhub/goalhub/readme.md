# GoalHub Backend

GoalHub Backend 是一个基于 Java 21、Spring Boot 3.5、Spring Cloud 2025、Spring Cloud Gateway、Nacos、MyBatis-Plus、MySQL、Redis 和 MongoDB 的多模块后端项目。

项目按业务边界拆分为网关、用户、比赛、订单和后台管理服务。公共 DTO、统一响应、错误码、业务异常、JWT 工具和日志能力由 `common` 与 `boot` 模块复用。

## 技术栈

| 技术 | 用途 |
| --- | --- |
| Java 21 | 统一运行和编译版本 |
| Spring Boot 3.5.14 | 服务基础框架 |
| Spring Cloud 2025.0.1 | 微服务基础能力 |
| Spring Cloud Gateway WebFlux | API 网关、路由转发、跨域和限流 |
| Spring Cloud Alibaba Nacos | 服务注册与发现 |
| OpenFeign | 服务间 HTTP 调用 |
| MyBatis-Plus | ORM 与分页 |
| MySQL 8.4 | 业务主库 |
| Redis 7.2 | 缓存、验证码限流等 |
| MongoDB 8.0 | 业务日志、系统日志、错误日志 |
| springdoc-openapi | OpenAPI / Swagger 文档 |
| Maven | 多模块构建 |
| Docker Compose | 本地依赖和生产容器编排 |

## 模块结构

```text
goalhub
├── common              # 公共 DTO、统一响应、错误码、业务异常、JWT 工具
├── boot                # 通用启动能力、日志写入能力、i18n 配置
├── gateway-service     # 网关、路由、跨域、限流、身份透传
├── user-service        # 用户、账户、余额、登录注册、验证码
├── match-service       # 联赛、球队、比赛、玩法、赔率、冠军赔率、赛果
├── order-service       # 下单、订单查询、订单预判、冠军投注、账户扣款调用
├── admin-service       # 后台管理聚合接口和内部服务转发
├── docker-compose.yml  # 本地基础组件编排
├── pro-docker-compose.yml # 生产容器编排模板
└── pom.xml             # Maven 父工程
```

## 服务职责

| 模块 | 职责 |
| --- | --- |
| `gateway-service` | 统一入口，路由 `/api/user/**`、`/api/soccer/**`、`/api/order/**`、`/admin/**`，并提供验证码接口限流。 |
| `user-service` | 用户注册登录、验证码、用户账户、账户流水、余额扣减、后台用户账户管理。 |
| `match-service` | 足球联赛、球队、比赛、玩法赔率、冠军赔率、赛果录入与内部快照查询。 |
| `order-service` | 普通赛事投注、冠军投注、订单分页、系统赛果预判、订单明细管理和账户服务远程扣款。 |
| `admin-service` | 后台管理员、菜单权限、用户、比赛、赔率、订单、日志等管理入口。 |
| `common` | DTO、分页响应、统一响应、错误码、业务异常、JWT 和 Swagger 注解依赖。 |
| `boot` | 通用日志能力和国际化消息配置。 |

## 默认端口

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
| `redis` | `6379` |

## 网关路由

| 外部路径 | 目标服务 |
| --- | --- |
| `/api/user/**` | `user-service` |
| `/api/soccer/**` | `match-service` |
| `/api/order/**` | `order-service` |
| `/admin/**` | `admin-service` |

## 本地启动

先启动本地基础组件：

```bash
docker compose up -d
```

本地 `docker-compose.yml` 会启动：

| 组件 | 地址 | 说明 |
| --- | --- | --- |
| Nacos | `127.0.0.1:8848` | 本地服务注册与发现 |
| MySQL | `127.0.0.1:3306` | 默认库 `goalhub_user` |
| MongoDB | `127.0.0.1:27017` | 日志库 `goalhub_logs` |
| Redis | `127.0.0.1:6379` | 缓存与限流 |

默认账号：

| 组件 | 用户 | 密码 |
| --- | --- | --- |
| MySQL | `goalhub` | `goalhub123456` |
| MongoDB root | `root` | `root123456` |
| Nacos | 当前本地配置关闭鉴权 | 当前本地配置关闭鉴权 |

然后按依赖关系启动服务：

1. `user-service`
2. `match-service`
3. `order-service`
4. `admin-service`
5. `gateway-service`

也可以先全部打包，再分别运行各模块 Jar。

```bash
mvn clean package -DskipTests
```

## 常用 Maven 命令

```bash
# 构建全部模块
mvn clean package

# 跳过测试构建全部模块
mvn clean package -DskipTests

# 构建指定模块及其依赖
mvn clean package -pl order-service -am -DskipTests
```

## 生产 Compose 部署

`pro-docker-compose.yml` 是生产部署模板，包含：

| 服务 | 容器名 | 说明 |
| --- | --- | --- |
| `nacos` | `goalhub-nacos` | 服务注册与发现 |
| `mysql` | `goalhub-mysql` | 业务主库 |
| `mongodb` | `goalhub-mongodb` | 日志库 |
| `redis` | `goalhub-redis` | 缓存与限流 |
| `gateway-service` | `goalhub-gateway` | 网关服务 |
| `user-service` | `goalhub-user` | 用户服务 |
| `match-service` | `goalhub-match` | 比赛服务 |
| `admin-service` | `goalhub-admin` | 后台服务 |
| `order-service` | `goalhub-order` | 订单服务 |
| `nginx` | `goalhub-nginx` | 对外 HTTP 入口 |

生产模板约定以下目录：

```text
.
├── jars                    # 各服务可执行 Jar
│   ├── gateway-service.jar
│   ├── user-service.jar
│   ├── match-service.jar
│   ├── admin-service.jar
│   └── order-service.jar
├── config                  # 各服务外部化配置
│   ├── gateway-service
│   ├── user-service
│   ├── match-service
│   ├── admin-service
│   └── order-service
├── data                    # MySQL、MongoDB、Redis、Nacos 持久化数据
├── mysql/init              # MySQL 初始化 SQL
└── nginx                   # Nginx 配置和静态资源
```

生产启动：

```bash
docker compose -f pro-docker-compose.yml up -d
```

生产模板里业务服务使用 `eclipse-temurin:21-jre` 运行挂载的 Jar，并通过：

```text
--spring.config.additional-location=file:/app/config/
```

加载对应服务的外部配置目录。

## API 文档

各 Web 服务引入了 `springdoc-openapi`，默认可访问：

| 服务 | Swagger UI |
| --- | --- |
| `user-service` | `http://127.0.0.1:9001/swagger-ui.html` |
| `match-service` | `http://127.0.0.1:9002/swagger-ui.html` |
| `admin-service` | `http://127.0.0.1:9003/swagger-ui.html` |
| `order-service` | `http://127.0.0.1:9004/swagger-ui.html` |

网关可按路由代理业务接口，Swagger 是否通过网关暴露取决于网关路由配置。

## 配置说明

本地各服务默认连接：

```yaml
nacos: 127.0.0.1:8848
mysql: 127.0.0.1:3306/goalhub_user
redis: localhost:6379
mongodb: mongodb://root:root123456@localhost:27017/goalhub_logs?authSource=admin
```

生产部署时建议通过 `config/<service>/` 下的外部配置覆盖数据库、Redis、MongoDB、Nacos、JWT、跨域和日志等参数。

## 开发约定

- 统一响应使用 `response.Result<T>`。
- 错误码维护在 `response.ResultCode`。
- 业务异常使用 `exception.BusinessException`。
- DTO 和 Controller 使用 Swagger / OpenAPI 注解补充接口文档。
- Service、Mapper、Entity 和核心业务类使用标准 JavaDoc 注释说明职责、参数和返回值。
- Feign 返回 `Result<T>` 时必须判断远程响应是否为空、状态码是否成功、数据是否为空。
- 金额字段使用 `BigDecimal`，涉及扣款、结算、预期返还时统一处理精度。
- 下单、扣款、订单状态流转、赛果预判属于高风险逻辑，必须保留日志和事务边界。

## 当前注意事项

- Compose 文件中的密码和 Token 是开发/模板配置，生产环境应改为环境变量或密钥管理。
- `pro-docker-compose.yml` 中基础组件仅通过 Docker 内部网络 `expose`，外部入口由 Nginx 负责。
- `depends_on` 只保证容器启动顺序，不保证依赖服务已就绪；生产建议补 healthcheck 或启动重试策略。
- 订单扣款涉及跨服务调用，后续建议继续完善幂等、补偿和对账机制。
- 冠军投注和普通赛事投注的结算路径不同，后续需要持续确认冠军投注结算流程。
