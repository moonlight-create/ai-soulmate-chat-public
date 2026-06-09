package com.wj.aisoulmatechat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;

@Configuration
public class McpConfig {
//    private static final String MCP_SERVER_URL = "http://39.96.33.73:5000/api/devices";
    // windows
    String pythonPath = "D:\\anaconda3\\python.exe";
    // Linux
    // String pythonPath = "/root/mijia-control/venv/bin/python";
    @Bean
    public ToolCallbackProvider mijiaMcpTools() {
        // 1. 构建启动参数（Windows 优先用完整路径，避免命令找不到）
//        ServerParameters params = ServerParameters.builder("C:\\Users\\moonlight\\.local\\bin\\uvx.exe")
//                .args(
//                        "--from",
//                        "git+https://github.com/handsomejustin/mijia-control.git",
//                        "mcp_server.py"
//                )
//                .build();

        ServerParameters params = ServerParameters.builder(pythonPath)
                // 启动 mcp_server 模块，不是 run.py
                .args("-m", "mcp_server")
                // 环境变量注入，替代 .env 文件
                .env(Map.of(
                        "MIJIA_API_URL", "http://127.0.0.1:5000/api",
                        "MIJIA_TOKEN", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJmcmVzaCI6ZmFsc2UsImlhdCI6MTc4MDkzNTU4MSwianRpIjoiNmVkYmVmOTItZmNiMC00MTI5LTkzOWItOTM2NDg1M2U4ODk3IiwidHlwZSI6ImFjY2VzcyIsInN1YiI6IjEiLCJuYmYiOjE3ODA5MzU1ODEsImNzcmYiOiJjMzcyMzA1YS02YzBlLTRjMDEtOWEwMy1iN2FkZTU4MDNhYmMiLCJleHAiOjE3ODEwMjE5ODF9.f9lOcjryGzjMpldLrxL2WEEx2FXXMJhm3FTtr-WFreA"
                ))
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        McpJsonMapper jsonMapper = new JacksonMcpJsonMapper(objectMapper);

        // 2. 构造 Stdio 传输
        StdioClientTransport transport = new StdioClientTransport(params, jsonMapper);

//        // HTTP 传输，对接远程 MCP 服务
//        HttpClientSseClientTransport transport = HttpClientSseClientTransport
//                .builder(MCP_SERVER_URL)
//                .connectTimeout(Duration.ofSeconds(30))
//                .build();

        // 3. 创建 MCP 同步客户端
        McpSyncClient syncClient = McpClient.sync(transport)
                .initializationTimeout(Duration.ofSeconds(60))
                .requestTimeout(Duration.ofSeconds(30))        // 单次请求超时 30 秒
                .enableCallToolSchemaCaching(true)              // 开启缓存
                .loggingConsumer(log -> System.out.println("MCP Server Log: " + log)) // 打印服务端日志
                .build();

        // 4. 直接使用构造方法创建 ToolCallbackProvider（核心）
        return new SyncMcpToolCallbackProvider(syncClient);
    }
}
