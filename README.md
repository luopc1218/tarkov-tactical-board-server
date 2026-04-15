# Tarkov Tactical Board Server

Backend service for the Tarkov Tactical Board project.

Frontend repository: [https://github.com/luopc1218/tarkov-tactical-board](https://github.com/luopc1218/tarkov-tactical-board)

Language: [中文](#中文) | [English](#english)

## 中文

### 技术栈

- Java 17+（`pom.xml` 指定 `java.version=17`）
- Maven 3.9+
- Spring Boot 3.4.2
- Spring Web / Validation / WebSocket
- Spring Data JPA + Hibernate
- Spring Security + JWT
- MySQL 8+
- Springdoc OpenAPI（Swagger UI）

### 启动

1. 环境准备
   - 安装 JDK 17+、Maven 3.9+
   - 准备 MySQL（可选使用项目脚本启动 Docker 依赖）
2. 启动依赖服务（开发环境）
   ```bash
   ./scripts/start-docker.sh
   ```
   停止依赖服务：
   ```bash
   docker compose -f docker/docker-compose.dev.yml down
   ```
3. 检查配置
   - 本地开发运行 `mvn spring-boot:run` 时，会读取项目根目录 `.env`，因为已配置 `spring.config.import: optional:file:.env[.properties]`
   - 文件：`src/main/resources/application.yml`
   - 重点项：`spring.datasource.*`、`app.auth.*`、`app.jwt.*`
4. 启动后端
   ```bash
   mvn spring-boot:run
   ```
5. 验证
   ```bash
   curl http://localhost:8080/api/health
   ```

### 本地 MySQL 预置数据与联调

如果你希望前端本地开发时直接连本地 MySQL 调试，推荐按下面的顺序：

1. 启动本地 MySQL（Docker）
   ```bash
   ./scripts/start-docker.sh
   ```
   说明：
   - 会启动 `docker/docker-compose.dev.yml` 里的 MySQL 8.4
   - 会自动创建数据库 `tarkov_board`
   - 默认对宿主机暴露 `127.0.0.1:3306`
2. 启动后端并自动预置数据
   ```bash
   mvn spring-boot:run
   ```
   或者直接一键启动：
   ```bash
   ./scripts/dev-local.sh
   ```
   说明：
   - 后端启动时会自动创建表
   - `tarkov_map` 会从 `src/main/resources/seeds/tarkov-maps.json` 自动灌入默认地图数据
   - `auth_admin` 会自动创建默认管理员账号
   - `./scripts/dev-local.sh` 会强制让 Spring Boot 连接开发 MySQL `127.0.0.1:3306`
3. 验证本地数据库里已经有预置数据
   ```bash
   mysql -h127.0.0.1 -P3306 -uroot -p
   ```
   然后执行：
   ```sql
   USE tarkov_board;
   SELECT COUNT(*) AS map_count FROM tarkov_map;
   SELECT username, created_at FROM auth_admin;
   ```

默认情况下，项目根目录 `.env` 提供的是部署/代理场景的端口：

- MySQL：`127.0.0.1:10003`
- 后端：`http://127.0.0.1:10002/eftboard-server`

如果你使用 `docker-compose.nginx.yml` 或 `docker-compose.prod.yml`，可以沿用这组端口。
如果你使用开发脚本 `./scripts/start-docker.sh`，则 MySQL 走 `127.0.0.1:3306`；此时推荐直接使用 `./scripts/dev-local.sh` 启动后端，它会自动覆盖成正确的开发端口。

### 打包与部署

推荐流程：推送 `master` 或 `v*` tag 后，GitHub Actions 自动构建并推送 Docker Hub 镜像：
`luopc1218docker/tarkov-tactical-board-server`

1. 配置部署环境变量（示例）
   ```bash
   cp .env.example .env
   # 编辑 .env，至少设置 MYSQL_ROOT_PASSWORD / APP_JWT_SECRET / APP_ADMIN_PASSWORD_HASH
   # 可选：APP_IMAGE_REPOSITORY / APP_IMAGE_TAG
   ```
2. 拉取并启动后端镜像
   ```bash
   docker compose pull app
   docker compose up -d app
   ```
3. 部署后验证
   ```bash
   curl http://127.0.0.1:18080/api/health
   ```

### 生产环境 Docker 部署（推荐）

推荐使用根目录的 `docker-compose.prod.yml`，只部署后端与 MySQL：

- `app`：Spring Boot 后端
- `mysql`：MySQL 8.4，数据持久化到 Docker volume

1. 准备环境变量
   ```bash
   cp .env.example .env
   ```
2. 编辑 `.env`，至少设置：
   - `MYSQL_HOST`
   - `MYSQL_BIND_ADDRESS`
   - `MYSQL_PORT`
   - `MYSQL_ROOT_PASSWORD`
   - `MYSQL_DATABASE`
   - `SERVER_PORT`
   - `APP_ADMIN_PASSWORD_HASH`
   - `APP_JWT_SECRET`
   - `APP_IMAGE_REPOSITORY`
   - `APP_IMAGE_TAG`
   - `APP_BIND_ADDRESS`
   - `APP_PORT`
3. 如果 Docker Hub 镜像是私有仓库，先登录：
   ```bash
   docker login
   ```
   如果镜像是公开仓库，可以跳过这一步。
4. 启动服务
   ```bash
   docker compose -f docker-compose.prod.yml --env-file .env up -d
   ```
5. 更新服务
   ```bash
   docker compose -f docker-compose.prod.yml --env-file .env pull
   docker compose -f docker-compose.prod.yml --env-file .env up -d
   ```
6. 查看日志
   ```bash
   docker compose -f docker-compose.prod.yml --env-file .env logs -f app
   ```

说明：

- `APP_IMAGE_REPOSITORY` 默认为 Docker Hub 仓库：
  ```env
  APP_IMAGE_REPOSITORY=luopc1218docker/tarkov-tactical-board-server
  ```
- `MYSQL_HOST` 在同一个 compose 内建议设置为 `mysql`
- `mysql` 会绑定到 `${MYSQL_BIND_ADDRESS}:${MYSQL_PORT}`，便于你按指定端口从宿主机连接
- 首次启动时，应用会自动创建基础表并初始化默认数据。
- `app` 会绑定到 `${APP_BIND_ADDRESS}:${APP_PORT}`，便于宿主机上的 Nginx 或其他反向代理接入。

### 宿主机 Nginx 反代部署

如果你已经在服务器上自行维护 `nginx`，更简单的方式是：

- 宿主机 `nginx` 继续监听 `80/443`
- Docker 只运行 `app + mysql`
- `app` 仅绑定到宿主机本地高位端口，例如 `127.0.0.1:18080`

使用文件：`docker-compose.nginx.yml`

1. 准备环境变量
   ```bash
   cp .env.example .env
   ```
2. 编辑 `.env`，至少设置：
   - `MYSQL_HOST`
   - `MYSQL_ROOT_PASSWORD`
   - `MYSQL_DATABASE`
   - `SERVER_PORT`
   - `APP_ADMIN_PASSWORD_HASH`
   - `APP_JWT_SECRET`
   - `APP_IMAGE_REPOSITORY`
   - `APP_IMAGE_TAG`
   - `APP_BIND_ADDRESS`
   - `APP_PORT`
3. 启动后端和数据库
   ```bash
   docker compose -f docker-compose.nginx.yml --env-file .env up -d
   ```
4. 验证后端
   ```bash
   curl http://127.0.0.1:18080/api/health
   ```
5. 更新服务
   ```bash
   docker compose -f docker-compose.nginx.yml --env-file .env pull
   docker compose -f docker-compose.nginx.yml --env-file .env up -d
   ```

Nginx 示例：

```nginx
server {
    listen 80;
    server_name your-domain.example.com;

    location /api/ {
        proxy_pass http://127.0.0.1:18080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /ws/ {
        proxy_pass http://127.0.0.1:18080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Jar 方式（手工部署）仍可使用：

1. 打包
   ```bash
   mvn clean package -DskipTests
   ```
2. 运行 Jar
   ```bash
   java -jar target/tarkov-tactical-board-server-0.0.1-SNAPSHOT.jar
   ```

## English

### Tech Stack

- Java 17+ (`java.version=17` in `pom.xml`)
- Maven 3.9+
- Spring Boot 3.4.2
- Spring Web / Validation / WebSocket
- Spring Data JPA + Hibernate
- Spring Security + JWT
- MySQL 8+
- Springdoc OpenAPI (Swagger UI)

### Run Locally

1. Prerequisites
   - Install JDK 17+ and Maven 3.9+
   - Prepare MySQL (or use the project script to start Docker dependencies)
2. Start dependencies (dev)
   ```bash
   ./scripts/start-docker.sh
   ```
   Stop dependencies:
   ```bash
   docker compose -f docker/docker-compose.dev.yml down
   ```
3. Check configuration
   - File: `src/main/resources/application.yml`
   - Key fields: `spring.datasource.*`, `app.auth.*`, `app.jwt.*`
4. Start backend
   ```bash
   mvn spring-boot:run
   ```
5. Verify
   ```bash
   curl http://localhost:8080/api/health
   ```

### Build and Deploy

Recommended flow: after pushing `master` or a `v*` tag, GitHub Actions builds and publishes image to Docker Hub:
`luopc1218docker/tarkov-tactical-board-server`

1. Prepare deploy env vars (example)
   ```bash
   cp .env.example .env
   # edit .env, at least set MYSQL_*/APP_JWT_SECRET/APP_ADMIN_PASSWORD_HASH
   # optional: APP_IMAGE_REPOSITORY / APP_IMAGE_TAG
   ```
2. Pull and start backend image
   ```bash
   docker compose pull app
   docker compose up -d app
   ```
3. Post-deploy check
   ```bash
   curl http://127.0.0.1:18080/api/health
   ```

Jar flow (manual deployment) is still available:

1. Build package
   ```bash
   mvn clean package -DskipTests
   ```
2. Run Jar
   ```bash
   java -jar target/tarkov-tactical-board-server-0.0.1-SNAPSHOT.jar
   ```
