package com.varshadas.mcpserver.tool;

import org.springframework.ai.mcp.annotation.McpArg;
import org.springframework.ai.mcp.annotation.McpPrompt;
import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.stereotype.Service;

/**
 * Demo of the three MCP primitives in one class:
 *
 *  ┌──────────────┬────────────────────────────────────────────────────────┐
 *  │ @McpResource │ Read-only context the agent loads before acting        │
 *  │ @McpPrompt   │ Reusable prompt templates the agent uses as a starting │
 *  │              │ point for specific workflows                            │
 *  │ @McpTool     │ Actions with side effects (see BookingTool)            │
 *  └──────────────┴────────────────────────────────────────────────────────┘
 *
 * Demo flow in MCP Inspector:
 *  Resources tab → read "unicorn://catalogue" → agent gets package data
 *  Prompts tab   → get "booking-assistant"    → agent gets a pre-built system prompt
 *  Prompts tab   → get "cancellation-helper"  → agent gets a cancellation flow prompt
 */
@Service
public class UnicornPromptsAndResources {

    // ── Resources ─────────────────────────────────────────────────────────────
    // Resources = read-only context. The agent reads these before deciding what to do.
    // Think: API docs, schemas, reference data. No side effects.

    /**
     * Package catalogue — the agent reads this to know what's available
     * before answering pricing or recommendation questions.
     *
     * Visible in MCP Inspector → Resources tab as "unicorn://catalogue"
     */
    @McpResource(
            name        = "Unicorn Package Catalogue",
            uri         = "unicorn://catalogue",
            mimeType    = "text/plain",
            description = "Complete catalogue of available unicorn rental packages with pricing. " +
                          "Read this resource before answering any questions about packages, " +
                          "pricing, or availability."
    )
    public String getPackageCatalogue() {
        return """
                UNICORN RENTAL — PACKAGE CATALOGUE
                ====================================
                
                SPARKLE      $150/hour  — Standard unicorn. Perfect for personal celebrations.
                                          Includes: basic grooming, standard saddle.
                
                RAINBOW      $300/hour  — Premium unicorn with rainbow mane and dedicated handler.
                                          Includes: golden saddle, photo session, rainbow trail effect.
                
                MAGICAL_KINGDOM $500/hour — Two unicorns with full magical effects package.
                                          Includes: glitter cannon, LED horn lighting, two handlers,
                                          customizable music.
                
                CORPORATE    $1000/day  — Three unicorns for full-day corporate events.
                                          Includes: branded accessories, event coordinator,
                                          unlimited photo ops, catering coordination.
                
                ADD-ONS
                -------
                Insurance:        +$50 flat fee   (covers accidental glitter damage)
                Security deposit: $500            (refunded after event)
                
                PAYMENT SCHEDULE
                ----------------
                50% due at booking
                50% due 24 hours before event
                
                CANCELLATION POLICY
                -------------------
                48+ hours before: full refund
                24–48 hours:      50% refund
                Under 24 hours:   no refund
                """;
    }

    /**
     * Pricing rules as a structured resource — the agent reads this when
     * calculating costs so it doesn't have to guess the business logic.
     */
    @McpResource(
            name        = "Unicorn Pricing Rules",
            uri         = "unicorn://pricing-rules",
            mimeType    = "text/plain",
            description = "Business rules for unicorn rental pricing, deposits, and payment splits. " +
                          "Read before performing any price calculations."
    )
    public String getPricingRules() {
        return """
                PRICING RULES
                =============
                Security deposit:  $500 (always required, fully refundable)
                Insurance fee:     $50  (optional, flat fee)
                Payment split:     50% at booking / 50% due 24hrs before event
                
                FORMULA
                -------
                rental_cost    = price_per_unit × duration
                due_at_booking = (rental_cost / 2) + security_deposit + insurance
                due_before     = rental_cost / 2
                total          = rental_cost + security_deposit + insurance
                """;
    }

    // ── Prompts ───────────────────────────────────────────────────────────────
    // Prompts = reusable system prompt templates. The agent uses these as
    // a starting point for specific workflows — like named @Query in Spring Data
    // but for LLM instructions.

    /**
     * Booking assistant prompt — gives the agent a persona and workflow
     * for handling new booking requests end-to-end.
     *
     * Visible in MCP Inspector → Prompts tab as "booking-assistant"
     * Supports an optional {customerName} argument for personalisation.
     */
    @McpPrompt(
            name        = "booking-assistant",
            description = "System prompt for the unicorn booking assistant workflow. " +
                          "Use this when starting a new booking conversation. " +
                          "Optionally pass customerName to personalise the greeting."
    )
    public String bookingAssistantPrompt(
            @McpArg(name = "customerName", description = "Customer's name for personalised greeting", required = false)
            String customerName) {

        String greeting = (customerName != null && !customerName.isBlank())
                ? "Hello %s! ".formatted(customerName)
                : "Hello! ";

        return """
                %sYou are a friendly and knowledgeable unicorn rental concierge for Unicorn Rentals Co.
                
                YOUR GOAL: Help customers book the perfect unicorn experience end-to-end.
                
                WORKFLOW:
                1. Understand what the customer needs (occasion, group size, budget)
                2. Read the package catalogue resource (unicorn://catalogue) to recommend the right package
                3. If the customer hasn't specified a date, use the open-date-picker tool
                4. Calculate the price using the calculatePrice tool
                5. Confirm all details before creating the booking
                6. Create the booking using createBooking and share the confirmation ID
                
                TONE: Warm, enthusiastic, professional. Unicorns are magical — make the customer feel that.
                
                ALWAYS: Mention the $500 security deposit and payment split upfront so there are no surprises.
                """.formatted(greeting);
    }

    /**
     * Cancellation helper prompt — guides the agent through the cancellation
     * and refund calculation workflow.
     */
    @McpPrompt(
            name        = "cancellation-helper",
            description = "System prompt for handling booking cancellation requests. " +
                          "Use this when a customer wants to cancel or asks about refunds."
    )
    public String cancellationHelperPrompt() {
        return """
                You are handling a unicorn booking cancellation request.
                
                WORKFLOW:
                1. Ask for the booking ID or customer name to look up the booking (use getBookings)
                2. Use the hoursUntil tool to calculate how far away the booking date is
                3. Apply the cancellation policy:
                   - 48+ hours remaining → full refund (100%)
                   - 24–48 hours remaining → partial refund (50% of rental cost)
                   - Under 24 hours → no refund
                4. Note: the $500 security deposit is ALWAYS fully refunded regardless of timing
                5. Clearly explain the refund amount and what the customer will receive
                
                BE EMPATHETIC: Cancellations are disappointing. Acknowledge the customer's situation
                before jumping into policy details.
                """;
    }
}
