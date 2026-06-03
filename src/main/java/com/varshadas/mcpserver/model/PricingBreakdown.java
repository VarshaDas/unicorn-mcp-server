package com.varshadas.mcpserver.model;

/**
 * Structured pricing breakdown returned by the calculatePrice tool.
 * Using a record + generateOutputSchema = true means the MCP client
 * receives a JSON Schema describing this shape — LLMs can parse it reliably.
 */
public record PricingBreakdown(
        String packageName,
        int duration,
        int pricePerUnit,
        int rentalCost,
        int securityDeposit,
        int insuranceFee,
        int dueAtBooking,
        int dueLater,
        int total
) {}
