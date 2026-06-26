package com.varshadas.mcpserver.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Demonstrates MCP Resources and Prompts using @Tool (Spring AI 1.1.x compatible).
 *
 * In Spring AI 1.1.x, Resources and Prompts are exposed as tools that return
 * structured content. In Spring AI 2.0 these become proper @McpResource and
 * @McpPrompt primitives visible as separate tabs in MCP Inspector.
 *
 * Demo talking point:
 *   "In 1.1.x everything is a tool. In 2.0, Spring AI adds first-class support
 *    for the other two MCP primitives — Resources and Prompts. You can see them
 *    as separate tabs in MCP Inspector."
 */
@Component
public class UnicornPromptsAndResources {

    // ── Resource-style tools ──────────────────────────────────────────────────
    // These are read-only — no side effects, just return context data.
    // In MCP 2.0 these would be @McpResource — discoverable in Resources tab.

    @Tool(description = """
            Returns the complete unicorn rental package catalogue with pricing.
            Call this tool when the user asks about available packages, prices,
            what unicorn options exist, or anything about package details.
            This is read-only — no side effects.
            """)
    public String getPackageCatalogue() {
        return """
                UNICORN RENTAL — PACKAGE CATALOGUE
                ====================================
                SPARKLE           $150/hour  — Standard unicorn. Perfect for personal celebrations.
                RAINBOW           $300/hour  — Premium unicorn with rainbow mane and dedicated handler.
                MAGICAL_KINGDOM   $500/hour  — Two unicorns with full magical effects package.
                CORPORATE         $1000/day  — Three unicorns for full-day corporate events.
                
                ADD-ONS
                Insurance:        +$50 flat fee
                Security deposit: $500 (always required, fully refundable)
                
                PAYMENT SCHEDULE
                50% due at booking / 50% due 24 hours before event
                
                CANCELLATION POLICY
                48+ hours: full refund | 24-48 hours: 50% refund | Under 24 hours: no refund
                """;
    }

    @Tool(description = """
            Returns the pricing rules and formula for unicorn rental cost calculations.
            Call this before calculating any prices or explaining payment structure.
            This is read-only — no side effects.
            """)
    public String getPricingRules() {
        return """
                PRICING RULES
                =============
                Security deposit:  $500 (always required, fully refundable)
                Insurance fee:     $50  (optional, flat fee)
                Payment split:     50% at booking / 50% due 24hrs before event
                
                FORMULA
                rental_cost    = price_per_unit × duration
                due_at_booking = (rental_cost / 2) + security_deposit + insurance
                due_before     = rental_cost / 2
                total          = rental_cost + security_deposit + insurance
                """;
    }

    // ── Prompt-style tools ────────────────────────────────────────────────────
    // These return system prompt templates for specific workflows.
    // In MCP 2.0 these would be @McpPrompt — discoverable in Prompts tab.

    @Tool(description = """
            Returns the standard booking assistant system prompt for starting a
            new unicorn booking conversation. Use this to set the assistant persona
            and workflow at the start of a booking session.
            Optionally pass customerName for a personalised greeting.
            """)
    public String getBookingAssistantPrompt(
            @ToolParam(description = "Customer name for personalised greeting. Optional — leave blank if unknown.")
            String customerName) {

        String greeting = (customerName != null && !customerName.isBlank())
                ? "Hello %s! ".formatted(customerName)
                : "Hello! ";

        return """
                %sYou are a friendly unicorn rental concierge for Unicorn Rentals Co.
                
                WORKFLOW:
                1. Understand what the customer needs (occasion, group size, budget)
                2. Use getPackageCatalogue to recommend the right package
                3. Use calculatePrice to give a full cost breakdown
                4. Confirm all details before creating the booking with createBooking
                5. Share the booking confirmation ID
                
                ALWAYS mention the $500 security deposit and 50/50 payment split upfront.
                """.formatted(greeting);
    }

    @Tool(description = """
            Returns the cancellation handling system prompt. Use this when a customer
            wants to cancel a booking or asks about refunds.
            """)
    public String getCancellationHelperPrompt() {
        return """
                You are handling a unicorn booking cancellation request.
                
                WORKFLOW:
                1. Look up the booking using getBookings (customer name or booking ID)
                2. Use hoursUntil to calculate time remaining until the booking date
                3. Apply cancellation policy:
                   - 48+ hours remaining → full refund
                   - 24-48 hours remaining → 50% refund
                   - Under 24 hours → no refund
                4. Note: $500 security deposit is ALWAYS fully refunded
                5. Explain the refund amount clearly and empathetically
                """;
    }
}
