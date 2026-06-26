# Demo Prompts — Unicorn MCP Server

## ⚠️ Why "list all packages" doesn't call a tool
The LLM already knows about packages from conversation context — it answers from memory.
Use prompts that FORCE a tool call by requiring live data the LLM can't know:
- Bookings in the database (only the tool knows)
- Current date/time (only the tool knows)
- A price calculation (only the tool knows)

---

## Tools That WILL Fire (forces a tool call)

### createBooking
```
Book a SPARKLE unicorn for Varsha on 2026-07-20, 2 hours, with insurance
```
```
I want to reserve the CORPORATE package for TechCorp on 2026-08-01 for 3 days, no insurance
```

### getBookings
```
What bookings does Varsha have?
```
```
Show me all existing bookings for TechCorp
```

### calculatePrice
```
How much will a RAINBOW package cost for 3 hours with insurance?
```
```
Give me a full price breakdown for the MAGICAL_KINGDOM package, 4 hours, with insurance
```

### getCurrentDateTime
```
What time is it right now in New York?
```
```
What is the current date and time in Asia/Kolkata timezone?
```

### hoursUntil
```
My booking is on 2026-07-20T10:00. How many hours until then and what is the refund policy?
```
```
Is it too late to get a full refund on a booking for 2026-06-27T15:00?
```

### resolveRelativeDate
```
What date is next Saturday?
```
```
What is the date for this coming Friday?
```

### open-date-picker (MCP App — Claude Desktop / MCP Jam only)
```
I want to book a RAINBOW unicorn for Varsha, 2 hours, with insurance — but let me pick the date
```
```
Book me a unicorn but I haven't decided the date yet
```

---

## Resources (read from MCP Inspector → Resources tab)

### unicorn://catalogue
Click → Read → shows full package list with pricing.
Demo line: "This is context the agent loads before acting — no tool call, no side effect"

### unicorn://pricing-rules
Click → Read → shows deposit formula and payment split.
Demo line: "Business rules served as a resource — agent reads this before calculating prices"

---

## Prompts (call from MCP Inspector → Prompts tab)

### booking-assistant
Arguments: `customerName = Varsha`
Returns: full system prompt with booking workflow.
Demo line: "Pre-built workflow — the agent gets a persona and step-by-step instructions"

### cancellation-helper
No arguments needed.
Returns: cancellation + refund workflow prompt.
Demo line: "Standardised org knowledge served over the protocol"

---

## Full End-to-End Demo Flow (Claude Desktop)

```
Step 1 — trigger date picker:
I want to book the RAINBOW package for Varsha, 2 hours, with insurance — but let me pick the date

Step 2 — after booking is confirmed, verify:
What bookings does Varsha have?

Step 3 — price check:
How much would the MAGICAL_KINGDOM package cost for 4 hours with insurance?

Step 4 — cancellation check:
Varsha's booking is on 2026-07-20T10:00. Can she get a full refund if she cancels now?
```

---

## Prompts That Force Multiple Tool Calls (good for showing the dance)

```
What time is it in Bengaluru right now, and if I book a unicorn for next Friday,
how many hours until then? Also give me a price breakdown for RAINBOW 2 hours with insurance.
```
This forces: getCurrentDateTime + resolveRelativeDate + hoursUntil + calculatePrice — four tools in one prompt.
