# Known Issues & Decisions Log — Unicorn MCP Server

---

## 1. MCP Apps (Date Picker) — Spring Boot Version Conflict

**Status:** Deferred to `feature/mcp-apps-boot4` branch

**Problem:**
The MCP Apps feature (`@McpTool`, `@McpResource`, `@McpPrompt`, `MetaProvider`) requires
Spring AI 2.0.0-SNAPSHOT. Spring AI 2.0 uses Jackson 3.x (`tools.jackson`), but Spring Boot
3.x ships with Jackson 2.x (`com.fasterxml.jackson`). These two cannot coexist on the classpath.

**Error seen:**
```
NoSuchFieldError: Class com.fasterxml.jackson.annotation.JsonFormat$Shape does not have
member field 'com.fasterxml.jackson.annotation.JsonFormat$Shape POJO'
```
and
```
NoClassDefFoundError: com/fasterxml/jackson/annotation/JsonInclude$Value
```

**Root cause:**
Spring AI 2.0-M3/M4/SNAPSHOT uses `tools.jackson:jackson-databind:3.0.x` via the MCP SDK
(`mcp-json-jackson3`). Spring Boot 3.x autoconfigure classes (e.g. `McpServerJsonMapperAutoConfiguration`)
reference `com.fasterxml.jackson` at runtime. Excluding Jackson 2.x breaks Spring Boot's own
autoconfiguration.

**Fix:**
Upgrade to Spring Boot 4.0.3 which ships with Jackson 3.x natively — no conflict.
The official Spring AI examples repo (`spring-ai-examples/mcp-apps-server`) uses
**Spring Boot 4.0.3 + Spring AI 2.0.0-SNAPSHOT + Java 25**.

**Current state:**
- `feature/mcp-apps-boot4` → Boot 4.0.3, Spring AI 2.0-SNAPSHOT, MCP Apps working
- `feature/mcp-apps-boot4` (this branch after simplification) → Boot 3.5, Spring AI 1.1.7,
  MCP Apps commented out, all other tools working

---

## 2. H2 Console 404 on Spring Boot 4.x

**Status:** Known Boot 4 issue, workaround available

**Problem:**
`http://localhost:8083/h2-console` returns 404 on Spring Boot 4.0.3. The H2 console
autoconfiguration behavior changed in Boot 4 due to modular autoconfiguration splitting.

**Reference:**
> "Spring Boot 4 introduces modular auto-configuration, which is why features like the H2
> console might stop working after you upgrade from Spring Boot 3." — danvega.dev

**Workaround options:**
1. Use Spring Boot 3.5 (H2 console works out of the box) ← current approach
2. Register H2 console servlet manually in Boot 4
3. Use an external DB viewer (DBeaver, TablePlus) connecting to `jdbc:h2:mem:testdb`

**Demo impact:**
H2 console only needed for Demo 2 (MCP branch). Since we reverted to Boot 3.5 on this branch,
H2 console works at `http://localhost:8083/h2-console`.

---

## 3. @McpResource / @McpPrompt Not Available in Spring AI 1.1.x

**Status:** By design — 2.0 feature

**Problem:**
`@McpResource` and `@McpPrompt` annotations (and the Prompts/Resources tabs in MCP Inspector)
are Spring AI 2.0 features only. In 1.1.x, everything must be registered as `@Tool` via
`MethodToolCallbackProvider`.

**Current workaround:**
`UnicornPromptsAndResources.java` exposes catalogue, pricing rules, booking assistant prompt,
and cancellation helper as `@Tool` methods. They appear in the Tools tab, not separate tabs.

**Demo talking point:**
> "In Spring AI 1.1.x everything is a tool. In 2.0, Spring AI adds first-class support for
> the other two MCP primitives — Resources and Prompts appear as separate tabs in MCP Inspector."

---

## 4. "List all packages" Doesn't Trigger a Tool Call

**Status:** Expected LLM behavior — not a bug

**Problem:**
Prompting "list all packages" or "what packages do you have?" does not trigger a `tools/call`
in the server logs. The agent answers from LLM knowledge without calling any tool.

**Root cause:**
The LLM has seen the package names in conversation context and answers from memory.
It only calls tools when it needs live data it cannot know (DB state, current time, calculations).

**Fix:**
Use prompts that force a tool call by requiring live data. See `DEMO_PROMPTS.md` for
tested prompts that reliably trigger tool calls.

**Reliable tool-forcing prompts:**
- `What bookings does Varsha have?` → triggers `getBookings`
- `How much for RAINBOW 3 hours with insurance?` → triggers `calculatePrice`
- `What time is it in New York?` → triggers `getCurrentDateTime`
- `What date is next Friday?` → triggers `resolveRelativeDate`

---

## 5. OAuth2 Removed

**Status:** Intentional for demo — add back for production

**What was removed:**
- `spring-boot-starter-oauth2-authorization-server` dependency
- `spring-boot-starter-oauth2-resource-server` dependency
- `OAuth2Config.java`
- OAuth2 properties from `application.properties`

**Why:**
Simplifies demo flow — no token required for MCP Inspector or agent connections.

**To restore:**
Switch to `feature/oauth2-security` branch which has the full OAuth2 implementation,
or re-add the dependencies and `OAuth2Config.java` (backed up in that branch).

---

## Branch Summary

| Branch | Boot | Spring AI | MCP Apps | H2 Console | OAuth2 | Purpose |
|---|---|---|---|---|---|---|
| `demo/rest-api-screen-recording` | 3.5 | none | ❌ | ✅ | ❌ | Act 1 screen recording |
| `feature/mcp-apps-boot4` (current) | 3.5 | 1.1.7 | ❌ (commented) | ✅ | ❌ | Acts 2-5 live demo |
| `feature/mcp-apps-boot4` (original) | 4.0.3 | 2.0-SNAPSHOT | ✅ | ❌ | ❌ | Full MCP Apps demo |
| `feature/oauth2-security` | 3.4.5 | 1.1.2 | ❌ | ✅ | ✅ | Security layer demo |
