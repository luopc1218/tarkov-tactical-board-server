# Tarkov Tactical Board Server

Spring Boot 后端基础结构，已集成：

- WebSocket (`/ws/chat`)
- MySQL (JPA)
- MinIO 文件存储

## 1. 环境要求

- JDK 17+
- Maven 3.9+
- MySQL 8+
- Docker (用于启动 MinIO)

## 2. 创建 MySQL 数据库

```sql
CREATE DATABASE IF NOT EXISTS tarkov_board DEFAULT CHARACTER SET utf8mb4;
```

默认账号已配置到 `src/main/resources/application.yml`：

- username: `root`
- password: `Peichun@92755`

## 3. 启动 MinIO（你本机未安装时推荐用 Docker）

```bash
docker run -d \
  --name minio \
  -p 9000:9000 \
  -p 9001:9001 \
  -e "MINIO_ROOT_USER=minioadmin" \
  -e "MINIO_ROOT_PASSWORD=minioadmin" \
  quay.io/minio/minio server /data --console-address ":9001"
```

- MinIO API: `http://localhost:9000`
- MinIO Console: `http://localhost:9001`
- 用户名/密码: `minioadmin` / `minioadmin`

## 4. 启动项目

```bash
mvn spring-boot:run
```

## 5. 验证接口

### 健康检查

```bash
curl http://localhost:8080/api/health
```

### 上传文件到 MinIO

```bash
curl -X POST http://localhost:8080/api/files/upload \
  -F "file=@/path/to/your-file.txt"
```

返回值即 `objectName`。

### 下载文件

```bash
curl -L "http://localhost:8080/api/files/download?objectName=YOUR_OBJECT_NAME" -o downloaded.bin
```

### WebSocket

连接地址：`ws://localhost:8080/ws/chat`

发送文本消息后，服务器会广播给当前所有连接客户端。

### 获取地图列表（公开）

```bash
curl http://localhost:8080/api/maps
```

### 管理端登录（JWT）

默认管理账号配置在 `/Users/luopeichun/Projects/tarkov-tactical-board-server/src/main/resources/application.yml`：

- username: `admin`
- passwordHash: `app.auth.adminPasswordHash`（bcrypt，不存明文）

可用以下命令生成新 bcrypt 哈希：

```bash
htpasswd -bnBC 10 "" "你的新密码" | tr -d ':\n'
```

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"你的明文登录密码"}'
```

返回 `accessToken` 后，管理端接口需要携带：

`Authorization: Bearer <accessToken>`

### 管理端地图接口（需登录）

```bash
# 列表
curl http://localhost:8080/api/admin/maps -H "Authorization: Bearer <accessToken>"

# 新增
curl -X POST http://localhost:8080/api/admin/maps \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"code":"terminal","nameZh":"航站楼","nameEn":"Terminal","bannerObjectName":"maps/banners/terminal.png","mapObjectName":"maps/bodies/terminal.png"}'

# 更新
curl -X PUT http://localhost:8080/api/admin/maps/1 \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"code":"ground-zero","nameZh":"零地","nameEn":"Ground Zero","bannerObjectName":"maps/banners/ground-zero.png","mapObjectName":"maps/bodies/ground-zero.png"}'

# 删除
curl -X DELETE http://localhost:8080/api/admin/maps/10 \
  -H "Authorization: Bearer <accessToken>"
```

资产约定：

- `bannerObjectName`：地图 banner（`.png`）
- `mapObjectName`：地图本体（`.png`）
- 推荐路径：`maps/banners/{code}.png`、`maps/bodies/{code}.png`

## 6. 接入 Apifox

后端启动后，OpenAPI 地址：

- JSON: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

Apifox 导入步骤：

1. 打开 Apifox，新建项目。
2. 选择 `导入数据 -> OpenAPI/Swagger -> URL 导入`。
3. 填入 `http://localhost:8080/v3/api-docs` 并导入。
4. 在 Apifox 环境变量中新增 `baseUrl=http://localhost:8080`。
5. 先调用 `POST /api/auth/login` 获取 token。
6. 在 Apifox 的 `Auth` 设置中选择 `Bearer Token`，填入 token。
7. 之后即可直接调试 `/api/admin/**` 管理端接口。

## 7. 前端如何看文档

有两种常用方式：

1. 直接看在线文档（本机）  
访问 `http://localhost:8080/swagger-ui/index.html`，前端可以直接看接口结构和示例。

2. 在 Apifox 团队共享  
你把导入后的 Apifox 项目分享到团队，前端在 Apifox Web/客户端打开该项目即可查看接口文档、示例参数、返回结构和调试记录。
