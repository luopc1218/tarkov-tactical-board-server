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

1. 打包
   ```bash
   mvn clean package -DskipTests
   ```
2. 运行 Jar
   ```bash
   java -jar target/tarkov-tactical-board-server-0.0.1-SNAPSHOT.jar
   ```
3. 推荐使用环境变量覆盖敏感配置
   ```bash
   export SPRING_DATASOURCE_URL="jdbc:mysql://127.0.0.1:3306/tarkov_board?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai"
   export SPRING_DATASOURCE_USERNAME="root"
   export SPRING_DATASOURCE_PASSWORD="your_password"
   export APP_AUTH_ADMINUSERNAME="admin"
   export APP_AUTH_ADMINPASSWORDHASH="your_bcrypt_hash"
   export APP_JWT_SECRET="your_base64_secret"
   java -jar target/tarkov-tactical-board-server-0.0.1-SNAPSHOT.jar
   ```
4. 部署后验证
   ```bash
   curl http://127.0.0.1:8080/api/health
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

1. Build package
   ```bash
   mvn clean package -DskipTests
   ```
2. Run Jar
   ```bash
   java -jar target/tarkov-tactical-board-server-0.0.1-SNAPSHOT.jar
   ```
3. Recommended: override sensitive config with env vars
   ```bash
   export SPRING_DATASOURCE_URL="jdbc:mysql://127.0.0.1:3306/tarkov_board?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai"
   export SPRING_DATASOURCE_USERNAME="root"
   export SPRING_DATASOURCE_PASSWORD="your_password"
   export APP_AUTH_ADMINUSERNAME="admin"
   export APP_AUTH_ADMINPASSWORDHASH="your_bcrypt_hash"
   export APP_JWT_SECRET="your_base64_secret"
   java -jar target/tarkov-tactical-board-server-0.0.1-SNAPSHOT.jar
   ```
4. Post-deploy check
   ```bash
   curl http://127.0.0.1:8080/api/health
   ```
