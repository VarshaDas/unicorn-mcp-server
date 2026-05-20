package com.varshadas.mcpserver.config;

import com.varshadas.mcpserver.tool.BookingTool;
import com.varshadas.mcpserver.tool.DateTimeTool;
import com.varshadas.mcpserver.tool.PricingCalculatorTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpServerConfig {

    @Bean
    public ToolCallbackProvider toolCallbackProvider(DateTimeTool dateTimeTool,
                                                     PricingCalculatorTool pricingCalculatorTool,
                                                     BookingTool bookingTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(dateTimeTool, pricingCalculatorTool, bookingTool)
                .build();
    }
}
