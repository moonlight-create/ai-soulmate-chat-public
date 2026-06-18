# AI Soulmate Chat

基于 Spring Boot 和 Spring AI 打造的智能伴侣聊天应用，让用户能够创建专属的 AI 灵魂伴侣并进行互动对话。

## 项目简介

AI Soulmate Chat 是一个充满情感价值的 AI 聊天应用，用户可以创建虚拟伴侣（灵魂伴侣），为其设定个性化的性格、背景和爱好，并通过自然语言进行深度交流。应用集成了先进的 AI 对话能力、记忆系统、RAG 知识增强等功能，为用户带来真实且富有情感的聊天体验。

## 技术栈

- **后端框架**: Spring Boot 3.x
- **AI 框架**: Spring AI (阿里云 DashScope)
- **数据库**: MyBatis-Plus + MySQL
- **缓存**: Redis
- **消息队列**: RocketMQ / RabbitMQ
- **向量数据库**: Chroma (用于 RAG 知识检索)
- **安全**: Spring Security + Redis Session

## 核心功能

### 🤖 AI 智能对话
- 支持流式输出 (Server-Sent Events)
- 多轮对话上下文记忆
- AI 角色扮演与性格定制
- 内容安全过滤与审核

### 💾 记忆系统
- Redis 分布式会话存储
- 历史消息自动持久化到数据库
- 按日期分组查看聊天记录
- 支持上下文窗口自动管理

### 📚 知识增强 (RAG)
- 向量存储实现语义检索
- 用户笔记导入与智能问答
- 个性化知识库构建

### 🛠️ 工具能力
- 天气查询工具
- 时间查询工具
- 重要场景存档
- MCP 工具扩展支持

### 👤 用户系统
- Spring Security 认证授权
- "记住我" 功能 (Redis Token 存储)
- 头像上传与管理

## 项目结构

```
ai-soulmate-chat/
├── src/main/java/com/wj/aisoulmatechat/
│   ├── config/              # 配置类
│   │   ├── customadvisors/  # 自定义 ChatAdvisor
│   │   ├── memory/          # 内存管理配置
│   │   ├── mq/              # 消息队列配置
│   │   ├── properties/      # 配置属性类
│   │   └── safe/            # 安全过滤配置
│   ├── controller/         # 控制器
│   ├── service/            # 业务服务
│   ├── mapper/             # 数据访问层
│   ├── entity/             # 实体类
│   ├── dto/                # 数据传输对象
│   ├── vo/                 # 视图对象
│   ├── security/           # 安全相关
│   ├── mq/                 # 消息消费者
│   ├── util/               # 工具类
│   └── enums/              # 枚举类
└── src/main/resources/
    ├── application.yml     # 主配置
    ├── templates/           # HTML 模板
    └── static/              # 静态资源
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- Redis 6.x+
- MySQL 8.x+
- (可选) RocketMQ / RabbitMQ

### 配置步骤

1. **克隆项目**
```bash
git clone https://gitee.com/sunmoonlight00/soulmate-public.git
cd soulmate-public/ai-soulmate-chat
```

2. **配置数据库**
```bash
# 创建数据库
mysql -u root -p < ../sql/soulmate.sql
```

3. **修改配置文件**
编辑 `src/main/resources/application.yml`，配置以下关键项：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/soulmate
    username: your_username
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
  ai:
    dashscope:
      api-key: your_api_key

my:
  memory:
    expire-day: 30
    max-context-message: 20
  mcp:
    mijia:
      path: /path/to/mcp/server
      mijiaApiUrl: http://localhost:8080
```

4. **编译运行**
```bash
./mvnw clean package -DskipTests
java -jar target/ai-soulmate-chat-*.jar
```

或使用 Spring Boot Maven 插件：
```bash
./mvnw spring-boot:run
```

## API 接口

### 聊天接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/chat/ai-chat` | POST | 普通对话 |
| `/chat/ai-chat-stream` | POST | 流式对话 (SSE) |
| `/chat/get-first-msg` | GET | 获取开场白 |
| `/chat/memory/group_by_day` | GET | 获取历史记录 |

### 伴侣管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/setting/soulmate/add` | POST | 添加伴侣 |
| `/setting/soulmate/update` | POST | 更新伴侣 |
| `/setting/soulmate/del/{soulmateId}` | POST | 删除伴侣 |

### 头像管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/avatar/soulmate/get` | GET | 获取头像 |
| `/avatar/soulmate/upload` | POST | 上传头像 |

### 笔记管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/memo/add` | POST | 添加笔记 |
| `/memo/delete/{docId}` | POST | 删除笔记 |
| `/memo/list/{soulmateId}` | GET | 获取笔记列表 |

## 前端页面

项目内置了简单的前端页面：

- 登录页面: `/login` 或 `/toLogin`
- 聊天页面: `/toChat?sid={soulmateId}`
- 伴侣选择: `/select_soulmate` 或 `/`

## 配置说明

### 自定义 Prompt

在 `application.yml` 中配置 AI 角色设定：

```yaml
my:
  prompt:
    base: "你是一个温柔可爱的AI伴侣..."
    first: "你好呀！今天过得怎么样？"
    hello: "嗨！想我了么~"
    summary: "请总结一下对话要点..."
```

### 敏感词过滤

```yaml
my:
  safeguard:
    sensitiveWords:
      - "敏感词1"
      - "敏感词2"
    failureResponse: "抱歉，我不太懂这个..."
```

### 内存配置

```yaml
my:
  memory:
    expire-day: 30           # 聊天记录过期天数
    max-context-message: 20 # 最大上下文消息数
    max-batch-save-message: 100 # 批量保存阈值
```

## 部署建议

### 生产环境部署

1. **使用 Nginx 反向代理**
参考 `shell/chat_nginx.conf` 配置

2. **使用 Shell 脚本管理**
```bash
# 启动
./shell/app.sh start

# 停止
./shell/app.sh stop

# 重启
./shell/app.sh restart

# 查看状态
./shell/app.sh status
```

3. **初始化脚本**
```bash
# 初始化 Redis、Nginx、Chroma 等服务
./shell/my_init.sh
```

## 许可证

本项目仅供学习交流使用，请勿用于商业用途。

## 交流讨论

如有问题或建议，欢迎提交 Issue 或 Pull Request。