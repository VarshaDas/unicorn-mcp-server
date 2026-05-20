package com.varshadas.mcpserver.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class PricingCalculatorTool {

    private static final int SECURITY_DEPOSIT = 500;
    private static final int INSURANCE_FEE = 50;

    @Tool(description = """
            Calculate the total cost for a unicorn rental given the per-unit price and duration.
            Use the pricing information from the knowledge base context to determine the per-unit price.
            Returns a full cost breakdown including deposit, security deposit, and insurance.""")
    public String calculatePrice(
            @ToolParam(description = "Name of the rental package") String packageName,
            @ToolParam(description = "Price per unit (hour or day) as found in the rental guide") int pricePerUnit,
            @ToolParam(description = "Number of hours or days") int duration,
            @ToolParam(description = "Whether to include optional insurance ($50)") boolean includeInsurance) {

        int rentalCost = pricePerUnit * duration;
        int deposit = rentalCost / 2;
        int insurance = includeInsurance ? INSURANCE_FEE : 0;
        int totalDueNow = deposit + SECURITY_DEPOSIT + insurance;
        int totalDueLater = rentalCost - deposit;

        return """
                Pricing Breakdown — %s (%d × $%d):
                  Rental cost:        $%d
                  Security deposit:   $%d (refundable)
                  Insurance:          $%d%s
                
                  Due at booking (50%%): $%d
                  Due 24hrs before:     $%d
                  Total:                $%d""".formatted(
                packageName, duration, pricePerUnit,
                rentalCost,
                SECURITY_DEPOSIT,
                insurance, includeInsurance ? "" : " (not selected)",
                totalDueNow,
                totalDueLater,
                rentalCost + SECURITY_DEPOSIT + insurance);
    }
}
