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

推荐使用根目录的 `docker-compose.prod.yml`，并采用同域部署：

- `proxy`：Caddy，负责 HTTPS，并把 `/api/*`、`/ws/*` 转发到后端，其余请求转发到前端
- `frontend`：前端容器，对外不直接暴露端口
- `app`：Spring Boot 后端，对外不直接暴露端口
- `mysql`：MySQL 8.4，数据持久化到 Docker volume

1. 准备环境变量
   ```bash
   cp .env.example .env
   ```
2. 编辑 `.env`，至少设置：
   - `MYSQL_ROOT_PASSWORD`
   - `MYSQL_DATABASE`
   - `APP_ADMIN_PASSWORD_HASH`
   - `APP_JWT_SECRET`
   - `APP_DOMAIN`
   - `APP_IMAGE_REPOSITORY`
   - `FRONTEND_IMAGE_REPOSITORY`
   - `APP_IMAGE_TAG`
   - `FRONTEND_IMAGE_TAG`
3. 在 `.env` 中设置统一站点域名供 Caddy 自动签发 HTTPS 证书：
   ```env
   APP_DOMAIN=app.example.com
   ```
4. 如果 Docker Hub 镜像是私有仓库，先登录：
   ```bash
   docker login
   ```
   如果镜像是公开仓库，可以跳过这一步。
5. 启动服务
   ```bash
   docker compose -f docker-compose.prod.yml --env-file .env up -d
   ```
6. 更新服务
   ```bash
   docker compose -f docker-compose.prod.yml --env-file .env pull
   docker compose -f docker-compose.prod.yml --env-file .env up -d
   ```
7. 查看日志
   ```bash
   docker compose -f docker-compose.prod.yml --env-file .env logs -f frontend
   docker compose -f docker-compose.prod.yml --env-file .env logs -f app
   ```

说明：

- `APP_IMAGE_REPOSITORY` 默认为 Docker Hub 仓库：
  ```env
  APP_IMAGE_REPOSITORY=luopc1218docker/tarkov-tactical-board-server
  ```
- `FRONTEND_IMAGE_REPOSITORY` 默认为 Docker Hub 仓库：
  ```env
  FRONTEND_IMAGE_REPOSITORY=luopc1218docker/tarkov-tactical-board-frontend
  ```
- 同域部署下，浏览器访问入口统一为 `https://APP_DOMAIN/`，后端接口走 `https://APP_DOMAIN/api/...`，WebSocket 走 `wss://APP_DOMAIN/ws/...`。
- 首次启动时，应用会自动创建基础表并初始化默认数据。
- `frontend`、`app`、`mysql` 都不会暴露到宿主机端口，只有 `proxy` 对外开放。

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
   - `MYSQL_ROOT_PASSWORD`
   - `MYSQL_DATABASE`
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
