# Unicorn MCP Server — Project Steering

## Project Overview

This is a **Spring AI MCP (Model Context Protocol) Server** that exposes unicorn rental tools to AI agents. It runs on port `8083` and uses the **Streamable HTTP transport** protocol.

- **Group ID:** `com.varshadas`
- **Artifact:** `unicorn-mcp-server`
- **Java:** 21
- **Spring Boot:** 3.5.0
- **Spring AI:** 1.1.2

## Tech Stack

- **Spring AI MCP Server** (`spring-ai-starter-mcp-server-webmvc`) — tool registration and MCP protocol handling
- **Spring Data JPA** — booking persistence via `BookingRepository`
- **H2 Database** — file-based at `/tmp/unicorn-bookings`, H2 console at `/h2-console`
- **OAuth2 Authorization Server + Resource Server** — JWT-based security
- **Maven** — build tool, use `./mvnw` wrapper

## Package Structure

```
com.varshadas.mcpserver
├── config/       # Spring configuration beans (McpServerConfig)
├── model/        # JPA entities (UnicornBooking)
├── repository/   # Spring Data repositories (BookingRepository)
└── tool/         # MCP tool classes (BookingTool, DateTimeTool, PricingCalculatorTool)
```

## MCP Tool Conventions

- Tool classes live in the `tool/` package and are annotated with `@Component`
- Each tool method is annotated with `@Tool(description = "...")` — descriptions must be clear and descriptive for AI agents
- Each parameter uses `@ToolParam(description = "...")` — always include format hints (e.g., `YYYY-MM-DD`, enum values)
- All new tool classes **must be registered** in `McpServerConfig` via `MethodToolCallbackProvider.builder().toolObjects(...)`
- Tool methods return plain `String` — format responses as human-readable text with relevant details

## Domain Model

The core entity is `UnicornBooking` with fields:
- `customerName`, `packageName` (SPARKLE, RAINBOW, MAGICAL_KINGDOM, CORPORATE)
- `bookingDate` (LocalDate), `duration` (hours, or days for CORPORATE)
- `includeInsurance` (boolean), `createdAt` (auto-set)

Pricing constants: `SECURITY_DEPOSIT = $500`, `INSURANCE_FEE = $50`. Payment split: 50% due at booking, remainder 24hrs before.

## Coding Conventions

- Use **constructor injection** (no `@Autowired` field injection)
- JPA entities use standard getters, no Lombok — keep this pattern unless Lombok is explicitly added
- Use `String.formatted(...)` for string interpolation (Java 15+ style)
- Use `var` for local variable type inference where the type is obvious
- Keep tool descriptions in `@Tool` concise but complete — AI agents rely on them for tool selection
- Use `@Column(nullable = false)` on required entity fields
- Repository method names follow Spring Data conventions (e.g., `findByCustomerNameIgnoreCaseOrderByCreatedAtDesc`)

## Build & Run

```bash
# Build
./mvnw clean package

# Run
./mvnw spring-boot:run

# Server starts on http://localhost:8083
# MCP endpoint: http://localhost:8083/mcp
# H2 Console:   http://localhost:8083/h2-console
```

## Configuration

Key properties in `application.properties`:
- `spring.ai.mcp.server.protocol=STREAMABLE` — do not change transport without updating client config
- `spring.jpa.hibernate.ddl-auto=update` — schema auto-updates on startup
- `logging.level.org.springframework.ai=DEBUG` — verbose AI logging enabled by default

## Security

The server uses OAuth2. Refer to `TEST_BEFORE_OAUTH2.md` for testing without auth. When adding new endpoints or tools, ensure they are covered by the resource server security configuration.
