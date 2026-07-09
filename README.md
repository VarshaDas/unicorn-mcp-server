# 🦄 Unicorn MCP Server

A Spring AI MCP (Model Context Protocol) server that exposes unicorn rental tools — pricing
calculator, booking management, date/time utilities, package catalogue, and workflow prompts —
over **Streamable HTTP** transport.

## Tech Stack

- Java 21, Spring Boot 3.5.0, Spring AI 1.1.7
- MCP Server (WebMVC / Streamable HTTP)
- H2 in-memory database + Spring Data JPA

## Architecture — One Server, Multiple Clients

```
┌─────────────────────────────────────────────────────────────────┐
│               UNICORN MCP SERVER (port 8083)                    │
│               Transport: Streamable HTTP at /mcp                │
│               Runs locally on your machine                      │
└─────────────────────────────────────────────────────────────────┘
                          ▲           ▲
                          │           │
              Streamable HTTP    Streamable HTTP
                          │           │
            ┌─────────────┘           └─────────────┐
            │                                       │
┌───────────────────────┐           ┌───────────────────────────┐
│  MCP CLIENT #1        │           │  MCP CLIENT #2            │
│  Spring AI Agent      │           │  MCP Inspector            │
│  (localhost:8081)     │           │  (localhost:6274)         │
│  Has an LLM attached  │           │  No LLM — manual calls   │
└───────────────────────┘           └───────────────────────────┘
```

One MCP server. Multiple clients connecting simultaneously — a human using Inspector,
an AI agent doing bookings. Same protocol, same `/mcp` endpoint, different session IDs.
That's the power of a standard.

## Branch Guide

| Branch | Purpose |
|---|---|
| `feature/mcp-apps-boot4` | Main demo branch — all tools, Boot 3.5, Spring AI 1.1.7 |
| `demo/rest-api-screen-recording` | Pure REST API, no MCP — Act 1 screen recording |
| `feature/oauth2-security` | OAuth2 layer added on top of MCP |

## Tools Exposed (10 total)

| Tool | Description |
|---|---|
| `createBooking` | Books a unicorn rental and persists it to H2 |
| `getBookings` | Retrieves all bookings for a customer |
| `calculatePrice` | Returns a full cost breakdown for a rental |
| `getCurrentDateTime` | Gets current date/time in a given timezone |
| `hoursUntil` | Calculates hours until a future date + cancellation policy |
| `resolveRelativeDate` | Resolves "tomorrow", "Saturday" etc. to YYYY-MM-DD |
| `getPackageCatalogue` | Returns the full package catalogue with pricing |
| `getPricingRules` | Returns pricing formula and payment split rules |
| `getBookingAssistantPrompt` | Returns a system prompt for booking workflow |
| `getCancellationHelperPrompt` | Returns a system prompt for cancellation handling |

## Prerequisites

- Java 21+
- Maven 3.9+
- Node.js (for MCP Inspector)

## Quick Start

```bash
# 1. Clone and start the server
git clone https://github.com/VarshaDas/unicorn-mcp-server.git
cd unicorn-mcp-server
git checkout feature/mcp-apps-boot4
./mvnw spring-boot:run
```

Wait for `Started McpServerApplication` in the logs.
- Server: `http://localhost:8083`
- MCP endpoint: `http://localhost:8083/mcp`
- H2 console: `http://localhost:8083/h2-console`

```bash
# 2. In a separate terminal — launch MCP Inspector
npx @modelcontextprotocol/inspector
```

3. Open http://localhost:6274
4. Transport: **Streamable HTTP**, URL: `http://localhost:8083/mcp`
5. Click **Connect** → all 10 tools visible in Tools tab

---

## How to Add MCP to an Existing Spring Boot App (3 Steps)

> You don't rewrite your code. You expose it.

### Step 1 — Add the dependency

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>
```

```properties
spring.ai.mcp.server.name=my-mcp-server
spring.ai.mcp.server.version=1.0.0
spring.ai.mcp.server.protocol=STREAMABLE
```

### Step 2 — Annotate your service methods

```java
@Component
public class BookingTool {

    @Tool(description = "Book a unicorn rental. Stores the booking and returns a confirmation ID.")
    public String createBooking(
            @ToolParam(description = "Customer name") String customerName,
            @ToolParam(description = "Package: SPARKLE, RAINBOW, MAGICAL_KINGDOM, or CORPORATE") String packageName,
            @ToolParam(description = "Booking date in YYYY-MM-DD format") String bookingDate,
            @ToolParam(description = "Duration in hours (or days for CORPORATE)") int duration,
            @ToolParam(description = "Whether to include optional insurance") boolean includeInsurance) {
        // your existing business logic — unchanged
    }
}
```

### Step 3 — Register with ToolCallbackProvider

```java
@Configuration
public class McpServerConfig {

    @Bean
    public ToolCallbackProvider toolCallbackProvider(BookingTool bookingTool,
                                                     DateTimeTool dateTimeTool,
                                                     PricingCalculatorTool pricingCalculatorTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(bookingTool, dateTimeTool, pricingCalculatorTool)
                .build();
    }
}
```

Done. Your existing REST endpoints keep working. MCP is additive — nothing breaks.

---

## H2 Console

```
URL:      http://localhost:8083/h2-console
JDBC URL: jdbc:h2:mem:testdb
Username: sa
Password: (blank)
```

Run `SELECT * FROM UNICORN_BOOKINGS;` to see bookings written by tools.
**Note:** In-memory DB — resets on server restart.

---

## Testing with MCP Inspector

Connect to `http://localhost:8083/mcp` via Streamable HTTP. Use these prompts to trigger tool calls:

```
# Forces createBooking
Book a SPARKLE unicorn for Varsha on 2026-07-20, 2 hours, with insurance

# Forces getBookings
What bookings does Varsha have?

# Forces calculatePrice
How much will a RAINBOW package cost for 3 hours with insurance?

# Forces getCurrentDateTime
What time is it in New York right now?

# Forces resolveRelativeDate
What date is next Saturday?

# Forces hoursUntil + cancellation policy
My booking is on 2026-07-20T10:00. Can I get a full refund?

# Forces 4 tools in one prompt
What time is it in Bengaluru, what date is next Friday, and how much
for the MAGICAL_KINGDOM package for 4 hours with insurance?
```

> **Note:** "List all packages" does NOT call a tool — the LLM answers from context.
> Use prompts that require live data (DB state, time, calculations).

---

## Testing with Claude Desktop

Add to `~/Library/Application Support/Claude/claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "unicorn": {
      "command": "npx",
      "args": ["-y", "mcp-remote", "http://localhost:8083/mcp"]
    }
  }
}
```

Claude Desktop doesn't support Streamable HTTP natively — `mcp-remote` proxies it.
Restart Claude Desktop after saving. Look for the 🔨 hammer icon in the chat input.

---

## Testing with Amazon Q Developer CLI

```json
// ~/.aws/amazonq/mcp.json
{
  "mcpServers": {
    "unicorn-mcp-server": {
      "url": "http://localhost:8083/mcp"
    }
  }
}
```

Then use `q chat` to talk to the tools.

---

## Debug Logging

The following log levels are enabled for demo purposes in `application.properties`:

```properties
# JSON-RPC message exchange
logging.level.io.modelcontextprotocol=DEBUG

# Tool execution — shows which tool fired and result
logging.level.org.springframework.ai.tool=DEBUG

# MCP server internals — tool registration, dispatch
logging.level.org.springframework.ai.mcp=DEBUG

# HTTP transport lifecycle
logging.level.org.springframework.ai.mcp.server.transport=DEBUG
```

Watch for these key log lines during a tool call:
```
Received JSON message: {"method":"tools/call","params":{"name":"createBooking",...}}
Starting execution of tool: createBooking
Successful execution of tool: createBooking
```

---

## Tool Parameter Reference

| Tool | Parameters |
|---|---|
| `createBooking` | `customerName`, `packageName` (SPARKLE/RAINBOW/MAGICAL_KINGDOM/CORPORATE), `bookingDate` (YYYY-MM-DD), `duration` (int), `includeInsurance` (boolean) |
| `getBookings` | `customerName` |
| `calculatePrice` | `packageName`, `pricePerUnit` (int), `duration` (int), `includeInsurance` (boolean) |
| `getCurrentDateTime` | `timezone` (e.g. "America/New_York", "Asia/Kolkata") |
| `hoursUntil` | `futureDateTime` (ISO format e.g. "2026-07-20T14:00") |
| `resolveRelativeDate` | `dayReference` (e.g. "Saturday", "tomorrow", "today") |
| `getPackageCatalogue` | none |
| `getPricingRules` | none |
| `getBookingAssistantPrompt` | `customerName` (optional) |
| `getCancellationHelperPrompt` | none |

---

## Known Issues

See `KNOWN_ISSUES.md` for full details on:
- MCP Apps (date picker) requiring Spring Boot 4 + Spring AI 2.0
- H2 console 404 on Spring Boot 4.x
- `@McpResource`/`@McpPrompt` availability only in Spring AI 2.0
- Why "list all packages" doesn't trigger a tool call
