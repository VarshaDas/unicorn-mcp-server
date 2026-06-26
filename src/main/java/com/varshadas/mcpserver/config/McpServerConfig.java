package com.varshadas.mcpserver.config;

import com.varshadas.mcpserver.tool.BookingTool;
import com.varshadas.mcpserver.tool.DateTimeTool;
import com.varshadas.mcpserver.tool.PricingCalculatorTool;
import com.varshadas.mcpserver.tool.UnicornPromptsAndResources;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers all @Tool-annotated classes with the MCP server.
 *
 * Note: In Spring AI 2.0, @McpTool/@McpResource/@McpPrompt classes are
 * auto-discovered and don't need registration here. In 1.1.x everything
 * goes through MethodToolCallbackProvider.
 */
@Configuration
public class McpServerConfig {

    @Bean
    public ToolCallbackProvider toolCallbackProvider(
            DateTimeTool dateTimeTool,
            BookingTool bookingTool,
            PricingCalculatorTool pricingCalculatorTool,
            UnicornPromptsAndResources unicornPromptsAndResources) {

        return MethodToolCallbackProvider.builder()
                .toolObjects(dateTimeTool, bookingTool, pricingCalculatorTool, unicornPromptsAndResources)
                .build();
    }
}
