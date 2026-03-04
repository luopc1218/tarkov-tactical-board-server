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

推荐流程：推送 `master` 或 `v*` tag 后，GitHub Actions 会自动构建并推送 GHCR 镜像：
`ghcr.io/luopc1218/tarkov-tactical-board-server-backend`

1. 服务器登录 GHCR（使用具备 `read:packages` 的 GitHub Token）
   ```bash
   echo "<YOUR_GITHUB_TOKEN>" | docker login ghcr.io -u <YOUR_GITHUB_USERNAME> --password-stdin
   ```
2. 配置部署环境变量（示例）
   ```bash
   cp .env.example .env
   # 编辑 .env，至少设置 MYSQL_*/APP_JWT_SECRET/APP_ADMIN_PASSWORD_HASH
   # 可选：APP_IMAGE_TAG=v1.5.4（默认 latest）
   ```
3. 拉取并启动后端镜像
   ```bash
   docker compose pull app
   docker compose up -d app
   ```
4. 部署后验证
   ```bash
   curl http://127.0.0.1:18080/api/health
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

Recommended flow: after pushing `master` or a `v*` tag, GitHub Actions builds and publishes image to GHCR:
`ghcr.io/luopc1218/tarkov-tactical-board-server-backend`

1. Login GHCR on server (token must include `read:packages`)
   ```bash
   echo "<YOUR_GITHUB_TOKEN>" | docker login ghcr.io -u <YOUR_GITHUB_USERNAME> --password-stdin
   ```
2. Prepare deploy env vars (example)
   ```bash
   cp .env.example .env
   # edit .env, at least set MYSQL_*/APP_JWT_SECRET/APP_ADMIN_PASSWORD_HASH
   # optional: APP_IMAGE_TAG=v1.5.4 (default latest)
   ```
3. Pull and start backend image
   ```bash
   docker compose pull app
   docker compose up -d app
   ```
4. Post-deploy check
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
