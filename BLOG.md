# Securing Your MCP Server with OAuth2 — Because AI Agents Shouldn't Have Unlimited Access

## The Problem

You've built an MCP Server. AI models can now call your tools — book unicorn rides, calculate pricing, check availability. It works beautifully.

But here's the thing: **anyone** can call those tools right now. No login. No identity check. No protection.

Your `BookingTool` creates real records in a database. Your `PricingCalculatorTool` exposes business logic. Without security, any random MCP client can connect and start booking unicorns, racking up charges, or scraping your pricing strategy.

That's not a demo problem. That's a production problem.

## What We're Building

We're taking a working Unicorn Rental MCP Server and locking it down with OAuth2. By the end:

- Every tool call requires a valid JWT token
- Unauthorized callers get rejected with a `401 Unauthorized`
- We know exactly **who** is calling our tools
- We can later enforce fine-grained permissions (e.g., everyone can view pricing, only authenticated users can create bookings)

## Why OAuth2?

It's the same pattern your bank uses when you log into their app — just applied to AI tool calling.

OAuth2 gives us:
- **Industry-standard authentication** — no reinventing the wheel
- **Token-based access** — stateless, scalable, well-understood
- **Separation of concerns** — the auth logic doesn't pollute your tool code
- **Ecosystem support** — every language and framework speaks OAuth2

## The Architecture

```
┌─────────────┐         ┌─────────────────────┐
│  MCP Client │──token──▶│  Unicorn MCP Server  │
│  (AI Agent) │         │  (Resource Server +   │
└──────┬──────┘         │   Authorization Server)│
       │                └─────────────────────┘
       │
       ▼
  1. Client connects
  2. Gets redirected to login
  3. Authenticates → receives JWT token
  4. Calls tools with token attached
  5. Server validates token → executes tool ✅
  6. No token → 401 Unauthorized ❌
```

Our MCP Server plays a dual role:
- **Authorization Server** — issues tokens to clients that authenticate
- **Resource Server** — validates tokens on incoming tool calls

This is the self-contained approach: everything lives in one Spring Boot application.

## The Stack

- **Spring Boot 3.5**
- **Spring AI MCP Server** (Streamable HTTP transport)
- **Spring Security OAuth2 Authorization Server**
- **Spring Security OAuth2 Resource Server**
- **H2 Database** for booking persistence

## What Changes in Our Code

The beauty of Spring Boot: securing the server is mostly configuration, not code.

1. Add the OAuth2 dependencies to `pom.xml`
2. Configure the Authorization Server (client registrations, grant types)
3. Configure the Resource Server (JWT validation)
4. That's it — Spring Security protects all endpoints automatically

Our existing tools (`BookingTool`, `PricingCalculatorTool`, `DateTimeTool`) remain untouched. Security is a cross-cutting concern handled by the framework.

## The End Result

**Before:**
```
MCP Client → calls BookingTool → booking created (no questions asked)
```

**After:**
```
MCP Client → calls BookingTool → 401 Unauthorized ❌
MCP Client → authenticates → gets token → calls BookingTool with token → booking created ✅
```

## What's Next

Once the basic OAuth2 flow is working, we can layer on:
- **Fine-grained permissions** — `PricingCalculatorTool` is public, `BookingTool` requires `ROLE_USER`
- **Scopes** — limit what a token can do (read-only vs. read-write)
- **External Authorization Server** — delegate to Keycloak or AWS Cognito for enterprise scenarios
- **Audit logging** — know exactly which user booked which unicorn

## Try It Yourself

The full source code is available in this repository. Run the server, connect an MCP client, and see OAuth2 in action.

---

*This post accompanies a video walkthrough where we implement the security step by step. If you prefer watching over reading, check out the video linked above.*
