# Tarkov Tactical Board Server

Escape from Tarkov 战术白板后端（Spring Boot）。

当前版本核心能力：
- 地图数据管理（公开查询 + 管理端增删改）
- 文件上传/下载（MinIO）
- JWT 登录鉴权（管理端接口）
- WebSocket 实时通信
- 白板协作实例（按 `instanceId` 多人协作）
- 白板状态持久化（MySQL）与至少 72 小时保留

## 环境要求

- JDK 17+
- Maven 3.9+
- MySQL 8+
- Docker（用于启动 MinIO，可选）

## 快速启动

1. 创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS tarkov_board DEFAULT CHARACTER SET utf8mb4;
```

2. 启动 MinIO（未安装时可用 Docker）：

```bash
docker run -d \
  --name minio \
  -p 9000:9000 \
  -p 9001:9001 \
  -e "MINIO_ROOT_USER=minioadmin" \
  -e "MINIO_ROOT_PASSWORD=minioadmin" \
  quay.io/minio/minio server /data --console-address ":9001"
```

3. 检查配置文件：
- `/Users/luopeichun/Projects/tarkov-tactical-board-server/src/main/resources/application.yml`
- 按本地环境调整 MySQL、MinIO、JWT、管理员账号配置。

4. 启动服务：

```bash
mvn spring-boot:run
```

服务默认地址：`http://localhost:8080`

## 核心接口

### 健康检查

```bash
curl http://localhost:8080/api/health
```

### 地图（公开）

```bash
curl http://localhost:8080/api/maps
```

### 文件上传/下载

```bash
curl -X POST http://localhost:8080/api/files/upload \
  -F "file=@/path/to/your-file.png"
```

```bash
curl -L "http://localhost:8080/api/files/download?objectName=YOUR_OBJECT_NAME" -o downloaded.bin
```

### 登录（JWT）

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"你的密码"}'
```

管理端接口使用请求头：
- `Authorization: Bearer <accessToken>`

### 地图管理（需登录）

```bash
curl http://localhost:8080/api/admin/maps \
  -H "Authorization: Bearer <accessToken>"
```

## 白板协作（v1.0.0）

### 1) 创建实例（必须传 `mapId`）

```bash
curl -X POST http://localhost:8080/api/whiteboard/instances \
  -H "Content-Type: application/json" \
  -d '{"mapId":1}'
```

返回示例（关键字段）：
- `instanceId`
- `mapId`
- `wsPath`（如 `/ws/whiteboard/{instanceId}`）
- `expireAt`

### 2) 前端进入实例时获取 `mapId`

```bash
curl http://localhost:8080/api/whiteboard/instances/{instanceId}
```

前端用返回里的 `mapId` 加载地图，再连接 WebSocket。

### 3) 连接白板 WebSocket

- 地址：`ws://localhost:8080/ws/whiteboard/{instanceId}`
- 同一 `instanceId` 下消息会实时广播给其他在线用户。

### 4) 读取/保存白板状态

```bash
curl http://localhost:8080/api/whiteboard/instances/{instanceId}/state
```

```bash
curl -X PUT http://localhost:8080/api/whiteboard/instances/{instanceId}/state \
  -H "Content-Type: application/json" \
  -d '{"state":{"shapes":[]}}'
```

状态保留规则：
- 至少保留 72 小时。
- 每次保存状态会将该实例过期时间延长到“当前时间 + 72 小时”。

### 5) WebSocket 状态持久化约定

服务端会对以下消息类型尝试落库：
- `snapshot`
- `state_snapshot`
- `full_state`

消息体中优先读取 `state` 字段，其次读取 `data` 字段作为快照内容。

## API 文档

- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

可直接导入 Apifox 进行团队联调。

## 建议发布流程

1. 在发布分支完成版本改动并通过编译。
2. 打 tag（例如 `v1.0.0`）。
3. 推送分支与 tag：

```bash
git push origin release/1.0
git push origin v1.0.0
```
