# Tutor-Link 家教直连平台

打破中介信息费，连接家长与大学生家教。

## 环境要求

- Java 17+（开发机 Java 26 需 Lombok 1.18.38+）
- Maven 3.8+
- MySQL 8.x
- Redis 7.x
- Apache Kafka 3.x（KRaft 模式）
- Node.js 18+（前端开发）

## 一、启动基础设施

确保本地 MySQL、Redis、Kafka 已启动：

```bash
# 方式一：Docker Compose（需安装 Docker）
docker-compose up -d mysql redis kafka

# 方式二：本地服务
# 确保 MySQL(3306)、Redis(6379)、Kafka(9092) 端口可用
```

## 二、配置数据库连接

编辑 `tutor-link-web/src/main/resources/application.yml`，确认数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tutor_link?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
    username: root
    password:              # 按实际情况填写，无密码留空即可
```

Flyway 会在首次启动时自动执行 `db/migration/V1-V7__*.sql` 创建表结构，无需手动建表。

## 三、启动后端

```bash
# 构建所有模块
mvn clean package -DskipTests

# 启动应用
mvn spring-boot:run -pl tutor-link-web
```

启动成功后访问：
- API 文档：http://localhost:8080/doc.html
- 后端端口：8080

## 四、启动前端

### 微信小程序（家长端）

```bash
cd tutor-link-miniapp
npm install
npm run dev:mp-weixin    # 微信开发者工具导入 dist/dev/mp-weixin
npm run dev:h5           # 或浏览器直接预览 H5
```

### 管理后台

```bash
cd tutor-link-admin
npm install
npm run dev              # 默认 http://localhost:5173
```

## 五、Docker 一键部署（生产环境）

```bash
docker-compose up --build
```

包含 Nginx 反向代理，端口映射：80(HTTP)、443(HTTPS)、8080(应用)。

## 可选环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `MYSQL_HOST` | MySQL 地址 | localhost |
| `MYSQL_PORT` | MySQL 端口 | 3306 |
| `MYSQL_DB` | 数据库名 | tutor_link |
| `MYSQL_USER` | 数据库用户 | root |
| `MYSQL_PASSWORD` | 数据库密码 | (空) |
| `REDIS_HOST` | Redis 地址 | localhost |
| `REDIS_PORT` | Redis 端口 | 6379 |
| `REDIS_PASSWORD` | Redis 密码 | (空) |
| `KAFKA_SERVERS` | Kafka 地址 | localhost:9092 |
| `JWT_SECRET` | JWT 签名密钥 | (内置开发密钥) |
| `WX_APPID` / `WX_SECRET` | 微信小程序凭证 | - |
| `OSS_*` | 阿里云 OSS 配置 | - |
| `ANTHROPIC_API_KEY` | AI 顾问 API Key | - |
