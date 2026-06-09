package com.wj.aisoulmatechat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wj.aisoulmatechat.config.properties.MyMcpServerConfigProperties;
import com.wj.aisoulmatechat.config.properties.MyServerConfigProperties;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class McpConfig {

    private final MyMcpServerConfigProperties myMcpServerConfigProperties;
//    String pythonPath = "D:\\anaconda3\\python.exe";

    @Bean
    public ToolCallbackProvider mijiaMcpTools() {
        MyMcpServerConfigProperties.MijiaProps mijiaProps = myMcpServerConfigProperties.getMijia();
        // 1. 构建启动参数（Windows 优先用完整路径，避免命令找不到）
        ServerParameters params = ServerParameters.builder(mijiaProps.getPath())
                // 启动 mcp_server 模块，不是 run.py
                .args("-m", "mcp_server")
                // 环境变量注入，替代 .env 文件
                .env(Map.of(
                        "MIJIA_API_URL", mijiaProps.getMijiaApiUrl(),
                        "MIJIA_TOKEN", mijiaProps.getMijiaToken()
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
                .initializationTimeout(Duration.ofSeconds(mijiaProps.getMijiaInitTimeoutS()))
                .requestTimeout(Duration.ofSeconds(mijiaProps.getMijiaTimeoutS()))
                .enableCallToolSchemaCaching(mijiaProps.getMijiaSchemaCache())
//                .loggingConsumer(log -> System.out.println("MCP Server Log: " + log)) // 打印服务端日志
                .build();

        // 4. 直接使用构造方法创建 ToolCallbackProvider（核心）
        return new SyncMcpToolCallbackProvider(syncClient);
    }
}
