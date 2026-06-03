package com.varshadas.mcpserver.config;

import com.varshadas.mcpserver.tool.BookingTool;
import com.varshadas.mcpserver.tool.DateTimeTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers tools that use the @Tool annotation (BookingTool, DateTimeTool).
 * DatePickerApp uses @McpTool/@McpResource and is auto-discovered by Spring AI — no registration needed here.
 */
@Configuration
public class McpServerConfig {

    @Bean
    public ToolCallbackProvider toolCallbackProvider(DateTimeTool dateTimeTool,
                                                     BookingTool bookingTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(dateTimeTool, bookingTool)
                .build();
    }
}
