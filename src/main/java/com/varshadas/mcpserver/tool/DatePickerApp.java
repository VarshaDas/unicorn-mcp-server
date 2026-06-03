package com.varshadas.mcpserver.tool;

import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.context.MetaProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * MCP App — date picker embedded inline in the chat client.
 *
 * Pattern (Spring AI 2.0 MCP Apps):
 *  1. Agent calls "open-date-picker" tool when user needs to pick a booking date.
 *  2. MCP client (MCP Jam, Claude Desktop via mcp-remote) renders the HTML
 *     resource inline in chat — no browser tab needed.
 *  3. User clicks a date and hits Confirm.
 *  4. JavaScript calls app.updateModelContext() injecting "booking date: YYYY-MM-DD"
 *     back into the conversation — agent reads it and calls BookingTool.createBooking().
 *
 * Testing with MCP Inspector:
 *  - Inspector shows both the "open-date-picker" tool and the MCP resource listing.
 *  - Call the tool → inspect the response metadata (ui.resourceUri).
 *  - To see the UI itself, use MCP Jam (mcpjam.dev) or Claude Desktop + mcp-remote.
 */
@Service
public class DatePickerApp {

    @Value("classpath:/app/date-picker-app.html")
    private Resource datePickerHtml;

    // ── Resource: serves the HTML calendar UI ─────────────────────────────────

    @McpResource(
            name         = "Date Picker App Resource",
            uri          = "ui://unicorn/date-picker-app.html",
            mimeType     = "text/html;profile=mcp-app",
            metaProvider = DatePickerApp.CspMetaProvider.class
    )
    public String getDatePickerResource() throws IOException {
        return datePickerHtml.getContentAsString(StandardCharsets.UTF_8);
    }

    /** Whitelists unpkg.com so the HTML can load ext-apps.ts */
    public static final class CspMetaProvider implements MetaProvider {
        @Override
        public Map<String, Object> getMeta() {
            return Map.of("ui",
                    Map.of("csp",
                            Map.of("resourceDomains", List.of("https://unpkg.com"))));
        }
    }

    // ── Tool: agent entry point — opens the calendar in chat ─────────────────

    @McpTool(
            title        = "Open Date Picker",
            name         = "open-date-picker",
            description  = """
                    Opens an interactive calendar UI so the user can visually select a unicorn
                    booking date. Call this tool when the user has not specified a date, says
                    'show me a calendar', 'let me choose the date', or 'I'm not sure which date'.
                    After the user picks a date the chosen YYYY-MM-DD value is injected into the
                    conversation automatically — then proceed with createBooking.
                    """,
            metaProvider = DatePickerApp.AppMetaProvider.class
    )
    public String openDatePicker() {
        return "Opening the unicorn booking date picker. Please select a date from the calendar.";
    }

    /** Links this tool to the HTML resource so the client renders it inline */
    public static final class AppMetaProvider implements MetaProvider {
        @Override
        public Map<String, Object> getMeta() {
            return Map.of("ui",
                    Map.of("resourceUri", "ui://unicorn/date-picker-app.html"));
        }
    }
}
