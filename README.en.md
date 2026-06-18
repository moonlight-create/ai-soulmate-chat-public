# AI Soulmate Chat

An intelligent companion chat application built with Spring Boot and Spring AI, allowing users to create personalized AI soulmates and engage in interactive conversations.

## Project Overview

AI Soulmate Chat is an emotionally rich AI chat application where users can create virtual companions (soulmates), customize their personalities, backgrounds, and interests, and interact through natural language. The application integrates advanced AI dialogue capabilities, a memory system, RAG knowledge enhancement, and more to deliver authentic and emotionally engaging conversations.

## Technology Stack

- **Backend Framework**: Spring Boot 3.x  
- **AI Framework**: Spring AI (Alibaba Cloud DashScope)  
- **Database**: MyBatis-Plus + MySQL  
- **Cache**: Redis  
- **Message Queue**: RocketMQ / RabbitMQ  
- **Vector Database**: Chroma (for RAG knowledge retrieval)  
- **Security**: Spring Security + Redis Session  

## Core Features

### 🤖 AI Intelligent Chat
- Supports streaming output (Server-Sent Events)  
- Multi-turn dialogue with context memory  
- AI role-playing and personality customization  
- Content safety filtering and moderation  

### 💾 Memory System
- Distributed session storage via Redis  
- Automatic persistence of historical messages to the database  
- Chat history grouped by date for easy browsing  
- Automatic context window management  

### 📚 Knowledge Enhancement (RAG)
- Semantic search via vector storage  
- Import user notes and enable intelligent Q&A  
- Personalized knowledge base construction  

### 🛠️ Tool Capabilities
- Weather query tool  
- Time query tool  
- Important scenario archiving  
- MCP tool extension support  

### 👤 User System
- Authentication and authorization via Spring Security  
- "Remember Me" functionality (Redis token storage)  
- Avatar upload and management  

## Project Structure

```
ai-soulmate-chat/
├── src/main/java/com/wj/aisoulmatechat/
│   ├── config/              # Configuration classes
│   │   ├── customadvisors/  # Custom ChatAdvisor
│   │   ├── memory/          # Memory management configuration
│   │   ├── mq/              # Message queue configuration
│   │   ├── properties/      # Configuration property classes
│   │   └── safe/            # Security filtering configuration
│   ├── controller/         # Controllers
│   ├── service/            # Business services
│   ├── mapper/             # Data access layer
│   ├── entity/             # Entity classes
│   ├── dto/                # Data transfer objects
│   ├── vo/                 # View objects
│   ├── security/           # Security-related components
│   ├── mq/                 # Message consumers
│   ├── util/               # Utility classes
│   └── enums/              # Enumerations
└── src/main/resources/
    ├── application.yml     # Main configuration
    ├── templates/           # HTML templates
    └── static/              # Static resources
```

## Quick Start

### Environment Requirements

- JDK 17+  
- Maven 3.8+  
- Redis 6.x+  
- MySQL 8.x+  
- (Optional) RocketMQ / RabbitMQ  

### Configuration Steps

1. **Clone the Project**
```bash
git clone https://gitee.com/sunmoonlight00/soulmate-public.git
cd soulmate-public/ai-soulmate-chat
```

2. **Configure Database**
```bash
# Create database
mysql -u root -p < ../sql/soulmate.sql
```

3. **Modify Configuration File**
Edit `src/main/resources/application.yml` and configure the following key settings:

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

4. **Build and Run**
```bash
./mvnw clean package -DskipTests
java -jar target/ai-soulmate-chat-*.jar
```

Or use the Spring Boot Maven plugin:
```bash
./mvnw spring-boot:run
```

## API Endpoints

### Chat Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/chat/ai-chat` | POST | Standard chat |
| `/chat/ai-chat-stream` | POST | Streaming chat (SSE) |
| `/chat/get-first-msg` | GET | Get initial greeting |
| `/chat/memory/group_by_day` | GET | Retrieve chat history by day |

### Soulmate Management

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/setting/soulmate/add` | POST | Add a soulmate |
| `/setting/soulmate/update` | POST | Update a soulmate |
| `/setting/soulmate/del/{soulmateId}` | POST | Delete a soulmate |

### Avatar Management

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/avatar/soulmate/get` | GET | Get avatar |
| `/avatar/soulmate/upload` | POST | Upload avatar |

### Note Management

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/memo/add` | POST | Add a note |
| `/memo/delete/{docId}` | POST | Delete a note |
| `/memo/list/{soulmateId}` | GET | Retrieve note list |

## Frontend Pages

The project includes simple built-in frontend pages:

- Login page: `/login` or `/toLogin`  
- Chat page: `/toChat?sid={soulmateId}`  
- Soulmate selection: `/select_soulmate` or `/`  

## Configuration Details

### Custom Prompts

Configure AI persona settings in `application.yml`:

```yaml
my:
  prompt:
    base: "You are a gentle and lovely AI companion..."
    first: "Hi there! How was your day?"
    hello: "Hey! Did you miss me~?"
    summary: "Please summarize the key points of this conversation..."
```

### Sensitive Word Filtering

```yaml
my:
  safeguard:
    sensitiveWords:
      - "Sensitive Word 1"
      - "Sensitive Word 2"
    failureResponse: "Sorry, I don't quite understand that..."
```

### Memory Configuration

```yaml
my:
  memory:
    expire-day: 30           # Expiration days for chat history
    max-context-message: 20 # Maximum context message count
    max-batch-save-message: 100 # Batch save threshold
```

## Deployment Recommendations

### Production Deployment

1. **Use Nginx as Reverse Proxy**  
   Refer to `shell/chat_nginx.conf` for configuration.

2. **Use Shell Scripts for Management**
```bash
# Start
./shell/app.sh start

# Stop
./shell/app.sh stop

# Restart
./shell/app.sh restart

# Check status
./shell/app.sh status
```

3. **Initialization Script**
```bash
# Initialize Redis, Nginx, Chroma, and other services
./shell/my_init.sh
```

## License

This project is intended solely for learning and communication purposes. Commercial use is strictly prohibited.

## Discussion

Feel free to submit Issues or Pull Requests for questions or suggestions.