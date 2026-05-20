# 🦄 Unicorn MCP Server

A Spring AI MCP (Model Context Protocol) server that exposes unicorn rental tools — pricing calculator, booking management, and date/time utilities — over **Streamable HTTP** transport.

## Tech Stack

- Java 21, Spring Boot 3.5.0, Spring AI 1.1.2
- MCP Server (WebMVC / Streamable HTTP)
- H2 database (file-based) + Spring Data JPA

## Tools Exposed

| Tool | Description |
|------|-------------|
| `calculatePrice` | Returns a full cost breakdown for a unicorn rental |
| `createBooking` | Books a unicorn rental and persists it to H2 |
| `getBookings` | Retrieves all bookings for a customer |
| `getCurrentDateTime` | Gets current date/time in a given timezone |
| `hoursUntil` | Calculates hours between now and a future date |
| `resolveRelativeDate` | Resolves "tomorrow", "Saturday", etc. to a date |

## Prerequisites

- Java 21+
- Maven 3.9+

## Build & Run

```bash
./mvnw clean package
./mvnw spring-boot:run
```

Server starts on **http://localhost:8083**. MCP endpoint: `http://localhost:8083/mcp`

---

## How to Build an MCP Server (or Convert an Existing Codebase)

> If you're here to turn your existing Spring Boot app into an MCP server — you're 3 steps away. You don't rewrite your code — you expose it.

### Step 1: Add the MCP Server dependency

Swap in (or add) the Spring AI MCP Server starter. This gives your app a `/mcp` endpoint that speaks **JSON-RPC 2.0** over **Streamable HTTP** — the wire protocol MCP uses under the hood.

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

That's the entire infrastructure change. No custom controllers, no manual JSON-RPC handling.

### Step 2: Annotate your methods with `@Tool`

Take any existing Spring bean method and add `@Tool` + `@ToolParam`. This is what makes your logic discoverable by any MCP client.

```java
@Component
public class PricingCalculatorTool {

    @Tool(description = "Calculate the total cost for a unicorn rental")
    public String calculatePrice(
            @ToolParam(description = "Name of the rental package") String packageName,
            @ToolParam(description = "Price per unit") int pricePerUnit,
            @ToolParam(description = "Number of hours or days") int duration,
            @ToolParam(description = "Include optional insurance") boolean includeInsurance) {
        // your existing business logic — unchanged
    }
}
```

Already have service classes with business logic? Just add the annotations. The method signatures become the tool's input schema — MCP clients see parameter names, types, and descriptions automatically.

### Step 3: Register tools with a `ToolCallbackProvider`

Tell Spring AI which beans to expose over MCP:

```java
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
```

Done. Spring Boot auto-configures the MCP endpoint. Any MCP client can now connect, discover your tools, and call them.

### What happens under the hood

MCP uses **JSON-RPC 2.0** as its wire format — every message is a JSON object with `method`, `params`, and `id`:

| Client sends | Server responds with |
|---|---|
| `"method": "initialize"` | Session ID + server capabilities |
| `"method": "tools/list"` | All registered tools with their schemas |
| `"method": "tools/call"` | Tool execution result |

The session is stateful — the server returns an `Mcp-Session-Id` header on initialize, and the client passes it on every subsequent request. Spring AI handles all of this plumbing; you never write JSON-RPC code yourself.

### The key idea

You're not building a new app. You're **exposing existing logic** through a standard protocol. The same `@Tool` methods that worked locally with `ChatClient.defaultTools()` now work remotely over MCP — accessible to any client in any language.

---

## Testing the MCP Server

### 1. curl — Raw MCP Protocol (Streamable HTTP)

The MCP protocol uses JSON-RPC 2.0 over HTTP.

**Initialize the session:**

```bash
curl -s -D - http://localhost:8083/mcp \
  -H "Content-Type: application/json" \
  -H "Accept: application/json, text/event-stream" \
  -d '{
    "jsonrpc": "2.0",
    "id": 1,
    "method": "initialize",
    "params": {
      "protocolVersion": "2025-03-26",
      "capabilities": {},
      "clientInfo": { "name": "curl-test", "version": "1.0.0" }
    }
  }'
```

Save the `Mcp-Session-Id` header from the response, then use it in subsequent calls.

**List available tools:**

```bash
curl -s http://localhost:8083/mcp \
  -H "Content-Type: application/json" \
  -H "Accept: application/json, text/event-stream" \
  -H "Mcp-Session-Id: <SESSION_ID>" \
  -d '{
    "jsonrpc": "2.0",
    "id": 2,
    "method": "tools/list",
    "params": {}
  }'
```

**Call a tool (e.g. calculatePrice):**

```bash
curl -s http://localhost:8083/mcp \
  -H "Content-Type: application/json" \
  -H "Accept: application/json, text/event-stream" \
  -H "Mcp-Session-Id: <SESSION_ID>" \
  -d '{
    "jsonrpc": "2.0",
    "id": 3,
    "method": "tools/call",
    "params": {
      "name": "calculatePrice",
      "arguments": {
        "packageName": "SPARKLE",
        "pricePerUnit": 150,
        "duration": 3,
        "includeInsurance": true
      }
    }
  }'
```

**Call createBooking:**

```bash
curl -s http://localhost:8083/mcp \
  -H "Content-Type: application/json" \
  -H "Accept: application/json, text/event-stream" \
  -H "Mcp-Session-Id: <SESSION_ID>" \
  -d '{
    "jsonrpc": "2.0",
    "id": 4,
    "method": "tools/call",
    "params": {
      "name": "createBooking",
      "arguments": {
        "customerName": "Alice",
        "packageName": "RAINBOW",
        "bookingDate": "2025-08-15",
        "duration": 2,
        "includeInsurance": false
      }
    }
  }'
```

**Call getBookings:**

```bash
curl -s http://localhost:8083/mcp \
  -H "Content-Type: application/json" \
  -H "Accept: application/json, text/event-stream" \
  -H "Mcp-Session-Id: <SESSION_ID>" \
  -d '{
    "jsonrpc": "2.0",
    "id": 5,
    "method": "tools/call",
    "params": {
      "name": "getBookings",
      "arguments": { "customerName": "Alice" }
    }
  }'
```

**Call getCurrentDateTime:**

```bash
curl -s http://localhost:8083/mcp \
  -H "Content-Type: application/json" \
  -H "Accept: application/json, text/event-stream" \
  -H "Mcp-Session-Id: <SESSION_ID>" \
  -d '{
    "jsonrpc": "2.0",
    "id": 6,
    "method": "tools/call",
    "params": {
      "name": "getCurrentDateTime",
      "arguments": { "timezone": "America/New_York" }
    }
  }'
```

**Call resolveRelativeDate:**

```bash
curl -s http://localhost:8083/mcp \
  -H "Content-Type: application/json" \
  -H "Accept: application/json, text/event-stream" \
  -H "Mcp-Session-Id: <SESSION_ID>" \
  -d '{
    "jsonrpc": "2.0",
    "id": 7,
    "method": "tools/call",
    "params": {
      "name": "resolveRelativeDate",
      "arguments": { "dayReference": "Saturday" }
    }
  }'
```

---

### 2. MCP Inspector (Official GUI Tool)

The [MCP Inspector](https://github.com/modelcontextprotocol/inspector) is a web-based debugging tool for MCP servers.

```bash
npx @modelcontextprotocol/inspector
```

1. Open the Inspector UI (usually at http://localhost:6274)
2. Select transport type: **Streamable HTTP**
3. Enter URL: `http://localhost:8083/mcp`
4. Click **Connect** → you'll see all 6 tools listed
5. Click any tool → fill in the parameters → **Run** to test

---

### 3. Claude Desktop

Add this to your Claude Desktop config (`~/Library/Application Support/Claude/claude_desktop_config.json` on macOS):

```json
{
  "mcpServers": {
    "unicorn-mcp-server": {
      "url": "http://localhost:8083/mcp"
    }
  }
}
```

Restart Claude Desktop. You can then ask things like:
- *"How much would a 3-hour SPARKLE unicorn rental cost with insurance?"*
- *"Book a RAINBOW package for Alice on August 15th for 2 hours"*
- *"What's the date for this Saturday?"*

---

### 4. Amazon Q Developer CLI

Add the server to `~/.aws/amazonq/mcp.json`:

```json
{
  "mcpServers": {
    "unicorn-mcp-server": {
      "url": "http://localhost:8083/mcp"
    }
  }
}
```

Then use `q chat` to interact with the tools conversationally.

---

### 5. VS Code with Copilot (MCP Support)

Add to `.vscode/mcp.json` in your workspace:

```json
{
  "servers": {
    "unicorn-mcp-server": {
      "type": "http",
      "url": "http://localhost:8083/mcp"
    }
  }
}
```

Use Copilot Chat in Agent mode to interact with the tools.

---

### 6. Cursor IDE

Add to Cursor's MCP settings (`.cursor/mcp.json`):

```json
{
  "mcpServers": {
    "unicorn-mcp-server": {
      "url": "http://localhost:8083/mcp"
    }
  }
}
```

Use Cursor's Agent mode to invoke the unicorn rental tools.

---

### 7. H2 Console (Verify Booking Persistence)

After creating bookings, verify the data directly:

1. Open http://localhost:8083/h2-console
2. JDBC URL: `jdbc:h2:file:/tmp/unicorn-bookings`
3. Username: `sa`, Password: *(empty)*
4. Run: `SELECT * FROM UNICORN_BOOKINGS;`

---

## Quick Reference — All Tool Parameters

| Tool | Parameters |
|------|-----------|
| `calculatePrice` | `packageName` (string), `pricePerUnit` (int), `duration` (int), `includeInsurance` (boolean) |
| `createBooking` | `customerName` (string), `packageName` (string), `bookingDate` (string, YYYY-MM-DD), `duration` (int), `includeInsurance` (boolean) |
| `getBookings` | `customerName` (string) |
| `getCurrentDateTime` | `timezone` (string, e.g. "America/New_York") |
| `hoursUntil` | `futureDateTime` (string, ISO format e.g. "2025-07-20T14:00") |
| `resolveRelativeDate` | `dayReference` (string, e.g. "Saturday", "tomorrow") |
