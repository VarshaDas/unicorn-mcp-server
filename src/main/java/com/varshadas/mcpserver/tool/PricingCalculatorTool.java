package com.varshadas.mcpserver.tool;

import com.varshadas.mcpserver.model.PricingBreakdown;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * PHASE 1 DEMO: Rich Tool Annotations
 *
 * Key points for the talk:
 * - @McpTool replaces @Tool — adds MCP-specific metadata
 * - annotations: readOnlyHint tells clients this tool has no side effects
 * - generateOutputSchema: client receives JSON Schema of the return type
 * - @McpToolParam: gives LLMs precise formatting instructions
 * - Returns a record (not a String) — structured, parseable output
 */
@Component
public class PricingCalculatorTool {

    private static final int SECURITY_DEPOSIT = 500;
    private static final int INSURANCE_FEE = 50;

    @Tool(description = "Calculate the total cost for a unicorn rental given the per-unit price and duration. Returns a full cost breakdown including deposit, security deposit, and insurance.")
    public PricingBreakdown calculatePrice(
            @ToolParam(description = "Rental package name: SPARKLE, RAINBOW, MAGICAL_KINGDOM, or CORPORATE")
            String packageName,
            @ToolParam(description = "Price per unit (hour or day) in whole dollars, e.g. 100")
            int pricePerUnit,
            @ToolParam(description = "Number of hours (or days for CORPORATE package)")
            int duration,
            @ToolParam(description = "Whether to include optional damage insurance ($50 flat fee)")
            boolean includeInsurance) {

        int rentalCost = pricePerUnit * duration;
        int deposit = rentalCost / 2;
        int insurance = includeInsurance ? INSURANCE_FEE : 0;
        int dueAtBooking = deposit + SECURITY_DEPOSIT + insurance;
        int dueLater = rentalCost - deposit;

        return new PricingBreakdown(
                packageName,
                duration,
                pricePerUnit,
                rentalCost,
                SECURITY_DEPOSIT,
                insurance,
                dueAtBooking,
                dueLater,
                rentalCost + SECURITY_DEPOSIT + insurance
        );
    }
}
