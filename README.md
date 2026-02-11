# Tarkov Tactical Board Server

Escape from Tarkov 战术白板后端（Spring Boot 3 + Java 17）。

## 功能概览

- 地图服务
  - 公开地图查询：`/api/maps`
  - 管理端地图增删改查：`/api/admin/maps/**`（JWT）
- 白板协作服务
  - 创建白板实例（绑定 `mapId`）
  - 多人通过同一个 `instanceId` 进行 WebSocket 实时协作
  - 白板状态快照持久化（MySQL）
  - 状态保留至少 72 小时
  - 管理端实例列表、删除实例
- 文件服务（MinIO）
  - 文件上传：`/api/files/upload`
  - 文件下载：`/api/files/download`
- 认证服务
  - 管理端登录：`/api/auth/login`

## 技术栈

- Spring Boot 3.4
- Spring Security + JWT
- Spring WebSocket
- Spring Data JPA
- MySQL 8
- MinIO

## 本地启动（开发环境）

### 1. 环境要求

- JDK 17+
- Maven 3.9+
- MySQL 8+
- Docker（用于启动 MinIO）

### 2. 初始化 MySQL

创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS tarkov_board DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

项目启动时会自动初始化/补齐业务表结构（如地图表、白板实例表）。

默认连接配置在：`src/main/resources/application.yml`

### 3. 初始化 MinIO（Docker）

```bash
docker run -d \
  --name minio \
  -p 9000:9000 \
  -p 9001:9001 \
  -e "MINIO_ROOT_USER=minioadmin" \
  -e "MINIO_ROOT_PASSWORD=minioadmin" \
  -v minio_data:/data \
  quay.io/minio/minio server /data --console-address ":9001"
```

- API: `http://localhost:9000`
- Console: `http://localhost:9001`
- 默认账号：`minioadmin / minioadmin`

### 4. 配置后端参数

编辑：`src/main/resources/application.yml`

重点检查：

- `spring.datasource.*`
- `minio.*`
- `app.auth.adminUsername`
- `app.auth.adminPasswordHash`
- `app.jwt.secret`

生成 bcrypt 密码哈希：

```bash
htpasswd -bnBC 10 "" "你的密码" | tr -d ':\n'
```

### 5. 启动项目

```bash
mvn spring-boot:run
```

默认地址：`http://localhost:8080`

### 6. 快速验证

健康检查：

```bash
curl http://localhost:8080/api/health
```

地图列表：

```bash
curl http://localhost:8080/api/maps
```

## 认证与管理端

### 登录获取 JWT

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"你的密码"}'
```

返回 `accessToken` 后，请在管理端接口中带上：

- `Authorization: Bearer <accessToken>`

### 管理端地图接口

- `GET /api/admin/maps`
- `POST /api/admin/maps`
- `PUT /api/admin/maps/{id}`
- `DELETE /api/admin/maps/{id}`

## 白板协作接口

### 1) 创建实例（必须传 `mapId`）

```bash
curl -X POST http://localhost:8080/api/whiteboard/instances \
  -H "Content-Type: application/json" \
  -d '{"mapId":1}'
```

返回包含：

- `instanceId`
- `mapId`
- `wsPath`（如 `/ws/whiteboard/{instanceId}`）
- `createdAt`
- `expireAt`

### 2) 查询实例（进入页面时取 `mapId`）

```bash
curl http://localhost:8080/api/whiteboard/instances/{instanceId}
```

### 3) 状态读取与保存

读取：

```bash
curl http://localhost:8080/api/whiteboard/instances/{instanceId}/state
```

保存：

```bash
curl -X PUT http://localhost:8080/api/whiteboard/instances/{instanceId}/state \
  -H "Content-Type: application/json" \
  -d '{"state":{"shapes":[]}}'
```

### 4) WebSocket 协作

- 聊天广播（历史调试用）：`ws://localhost:8080/ws/chat`
- 白板协作：`ws://localhost:8080/ws/whiteboard/{instanceId}`

白板消息转发规则：

- 同 `instanceId` 的连接之间实时广播
- 服务端不会修改前端 JSON payload

状态持久化约定：

- 当消息 `type` 为以下值时，服务端会尝试落库快照：
  - `snapshot`
  - `state_snapshot`
  - `full_state`
- 快照字段读取优先级：`state` > `data`

### 5) 保留与清理策略

- 每次实例创建或快照保存，过期时间刷新为“当前时间 + 72 小时”
- 后端内置定时任务，每小时自动清理过期实例

## 管理端白板实例接口（JWT）

- `GET /api/admin/whiteboard/instances?includeExpired=true|false`
- `DELETE /api/admin/whiteboard/instances/{instanceId}`

列表返回字段包括：

- `instanceId`
- `mapId`
- `createdAt`
- `updatedAt`
- `expireAt`
- `active`
- `hasState`

## CORS 说明

当前后端允许以下来源模式：

- `http://localhost:*`
- `http://127.0.0.1:*`
- `http://192.168.*:*`
- `http://10.*:*`
- `http://172.*:*`

如果前端采用同域名反向代理（推荐），可避免大多数跨域问题。

## API 文档

- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## 生产部署（Ubuntu + Docker，尽量不影响同机其他应用）

### 1. 目录与版本

```bash
mkdir -p /opt/tarkov-board
cd /opt/tarkov-board
git clone <your-repo-url> backend
cd backend
git checkout release/1.0
git pull
git checkout v1.0.1
```

### 2. 用 Docker 启动 MySQL + MinIO + 后端

建议：仅将后端映射到 `127.0.0.1:18080`，由 Nginx 对外转发；MySQL/MinIO 不直接暴露公网端口。

可参考最小启动思路：

- MySQL：容器内 `3306`
- MinIO：容器内 `9000`
- 后端：容器内 `8080`，宿主机映射 `127.0.0.1:18080`

### 3. Nginx 反向代理

- 前端静态资源由 Nginx 提供
- `/api/`、`/ws/` 反代到 `http://127.0.0.1:18080`

这样前后端同域访问，CORS 风险最低。

### 4. HTTPS 与防火墙

- 使用 `certbot` 配置 HTTPS
- 云防火墙/UFW 仅开放 `22/80/443`
- 不开放 `3306/9000/9001` 到公网

## 发布流程

### 常规发布

```bash
git checkout release/1.0
mvn -DskipTests compile
git add .
git commit -m "chore: release update"
git tag -a v1.0.x -m "Release v1.0.x"
git push origin release/1.0
git push origin v1.0.x
```

### 覆盖已存在 Tag（谨慎）

```bash
git tag -fa v1.0.1 -m "Release v1.0.1"
git push origin v1.0.1 --force
```

