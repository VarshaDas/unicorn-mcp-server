"""
Streamlit version of:
"MCP in Production: What Nobody Tells You About the Model Context Protocol"
by Tian Pan — https://tianpan.co/blog/2026-03-05-model-context-protocol-production-guide

Content rephrased for compliance with licensing restrictions.
"""

import streamlit as st

# ── Page config ───────────────────────────────────────────────────────────────
st.set_page_config(
    page_title="MCP in Production",
    page_icon="⚡",
    layout="wide",
    initial_sidebar_state="expanded",
)

# ── Custom CSS ────────────────────────────────────────────────────────────────
st.markdown("""
<style>
  /* Card-style containers */
  .card {
      background: #1e2130;
      border-radius: 10px;
      padding: 1.2rem 1.5rem;
      margin-bottom: 1rem;
      border-left: 4px solid #4a9eff;
  }
  .card-warn {
      background: #2a1f1a;
      border-left: 4px solid #ff7043;
  }
  .card-good {
      background: #1a2a1f;
      border-left: 4px solid #4caf50;
  }
  .card-tip {
      background: #1e1e2a;
      border-left: 4px solid #9c27b0;
  }
  .stat-big {
      font-size: 2.4rem;
      font-weight: 800;
      color: #4a9eff;
  }
  .stat-label {
      font-size: 0.85rem;
      color: #888;
      margin-top: -8px;
  }
  h3 { color: #e0e0e0; }
  code { background: #2d2d3a !important; }
</style>
""", unsafe_allow_html=True)

# ── Sidebar navigation ────────────────────────────────────────────────────────
SECTIONS = [
    "🏠 Overview",
    "🔌 What MCP Is (and Isn't)",
    "💥 Architectural Failure Modes",
    "🔒 Security",
    "🛠️ Tool Design Best Practices",
    "⚡ Transport & Performance",
    "🤔 MCP vs. Alternatives",
    "📊 Production Readiness",
]

with st.sidebar:
    st.markdown("## 📖 Navigation")
    selected = st.radio("Jump to section:", SECTIONS, label_visibility="collapsed")
    st.markdown("---")
    st.caption("Source: [tianpan.co](https://tianpan.co/blog/2026-03-05-model-context-protocol-production-guide)")
    st.caption("*Content rephrased for compliance with licensing restrictions.*")

# ── Helper: render a styled card ─────────────────────────────────────────────
def card(body: str, kind: str = "default"):
    css_class = {"warn": "card card-warn", "good": "card card-good",
                 "tip": "card card-tip"}.get(kind, "card")
    st.markdown(f'<div class="{css_class}">{body}</div>', unsafe_allow_html=True)


# ══════════════════════════════════════════════════════════════════════════════
# SECTION: Overview
# ══════════════════════════════════════════════════════════════════════════════
if selected == "🏠 Overview":
    st.title("⚡ MCP in Production")
    st.subheader("What Nobody Tells You About the Model Context Protocol")
    st.caption("Based on the article by Tian Pan · March 5, 2026 · 10 min read")

    st.markdown("---")

    st.markdown("""
The "USB-C for AI" analogy is catchy — but it glosses over the gap between
*"it works in the demo"* and *"it handles Monday-morning traffic without leaking data or
melting your latency budget."*

This guide walks through the real-world gotchas: architectural failure modes, security pitfalls,
tool design principles, and when MCP is the right choice versus the wrong one.
""")

    st.markdown("### 📈 Adoption by the numbers")
    col1, col2, col3 = st.columns(3)
    with col1:
        st.markdown('<div class="stat-big">8,000%</div>', unsafe_allow_html=True)
        st.markdown('<div class="stat-label">growth in server downloads<br>5 months after Nov 2024 launch</div>', unsafe_allow_html=True)
    with col2:
        st.markdown('<div class="stat-big">97M</div>', unsafe_allow_html=True)
        st.markdown('<div class="stat-label">monthly SDK downloads<br>by April 2025</div>', unsafe_allow_html=True)
    with col3:
        st.markdown('<div class="stat-big">28%</div>', unsafe_allow_html=True)
        st.markdown('<div class="stat-label">of Fortune 500 companies had<br>MCP servers in production by Q1 2025</div>', unsafe_allow_html=True)

    st.markdown("---")

    card("""
<b>⚠️ The core tension</b><br>
Most of those 97 million downloads went into production without teams fully understanding
what they were building on. Speed of adoption ≠ depth of understanding.
""", "warn")

    st.markdown("### 🗺️ What's covered in this guide")
    topics = [
        ("🔌", "What MCP Is (and Isn't)", "The three primitives — tools, resources, prompts — and the sampling pattern"),
        ("💥", "Architectural Failure Modes", "The universal router trap, kitchen-sink servers, real-time delusion"),
        ("🔒", "Security", "492 exposed servers, CVEs, prompt injection, path traversal"),
        ("🛠️", "Tool Design", "Idempotency, pagination, single-responsibility, structured errors"),
        ("⚡", "Transport & Performance", "Streamable HTTP, warm-up costs, batching, geo-distribution"),
        ("🤔", "MCP vs. Alternatives", "When to use direct function calling or OpenAPI tooling instead"),
        ("📊", "Production Readiness", "Where the ecosystem stands and what the remaining barriers are"),
    ]
    for icon, title, desc in topics:
        st.markdown(f"**{icon} {title}** — {desc}")


# ══════════════════════════════════════════════════════════════════════════════
# SECTION: What MCP Is
# ══════════════════════════════════════════════════════════════════════════════
elif selected == "🔌 What MCP Is (and Isn't)":
    st.title("🔌 What MCP Actually Is (and Isn't)")

    st.markdown("""
MCP is a standardized **JSON-RPC 2.0 protocol** connecting AI *hosts* to capability
*servers* through lightweight *clients*. Each client manages a **1:1 connection** to
one server. A host might run a dozen clients simultaneously — one for filesystem,
one for a database, one for a remote SaaS API.
""")

    st.markdown("### The three server-side primitives")

    col1, col2, col3 = st.columns(3)
    with col1:
        card("""
<b>🔧 Tools</b><br><br>
Executable functions the AI can invoke: file reads, API calls, database queries.<br><br>
<em>The workhorse — most integrations live here.</em>
""")
    with col2:
        card("""
<b>📄 Resources</b><br><br>
Static context data the AI reads: schemas, docs, file contents.<br><br>
<em>For loading context into the LLM window — not real-time streaming.</em>
""")
    with col3:
        card("""
<b>💬 Prompts</b><br><br>
Predefined instruction templates for recurring tasks.<br><br>
<em>Underused by most teams, but valuable for standardizing model behavior.</em>
""")

    st.markdown("### The fourth primitive — Sampling")
    card("""
<b>🧠 Sampling (client-side)</b><br><br>
A server can request that the host's LLM complete a reasoning step.
This lets your MCP server offload AI reasoning <em>without needing its own model API keys</em>
— useful for tools that need dynamic judgment without tight coupling to a specific vendor.
""", "tip")

    st.markdown("### Transport options")

    col1, col2 = st.columns(2)
    with col1:
        card("""
<b>📦 Stdio (local)</b><br><br>
Standard input/output — for processes on the same machine.<br>
Zero network overhead. Ideal for development and local tool use.
""", "good")
    with col2:
        card("""
<b>🌐 Streamable HTTP (remote)</b><br><br>
For production remote servers. As of March 2025, requires
<b>OAuth 2.1</b> authentication.<br>
The auth-vacuum that existed earlier was a genuine security hazard.
""")

    st.info("**Key insight:** The spec is JSON-RPC 2.0. The transport is either local stdio or remote HTTP with OAuth. Everything else builds on these two layers.")


# ══════════════════════════════════════════════════════════════════════════════
# SECTION: Architectural Failure Modes
# ══════════════════════════════════════════════════════════════════════════════
elif selected == "💥 Architectural Failure Modes":
    st.title("💥 Architectural Failure Modes")
    st.markdown("Most MCP problems aren't bugs — they're **category errors**: slotting MCP into roles it wasn't designed for.")

    st.markdown("---")

    # Failure mode 1
    st.markdown("### ❌ Failure Mode 1 — The Universal Router Trap")
    card("""
MCP introduces <b>300–800 ms of overhead</b> per call (protocol handshake + serialization
+ transport round-trip). If you route every API call through an MCP layer —
treating it like an API gateway — that cost accumulates everywhere.
<br><br>
Customer-facing features expecting sub-100 ms responses will feel sluggish.
""", "warn")
    card("""
<b>✅ Fix:</b> MCP belongs in the <em>orchestration layer</em>, not in the request-response
path of your production API.
""", "good")

    st.markdown("---")

    # Failure mode 2
    st.markdown("### ❌ Failure Mode 2 — The Kitchen-Sink Server")
    card("""
Monolithic MCP servers that expose 40 tools across five unrelated domains are a
<b>maintenance and security nightmare</b>. When the server redeploys, everything goes down.
One tool has a permission issue, you audit all 40.
""", "warn")
    card("""
<b>✅ Fix:</b> Use a microservice-style approach — <em>one server per domain</em>
(filesystem, CRM, billing…), each scoped to exactly the tools that belong together.
Scale, restart, and lock down each surface independently.
""", "good")

    st.markdown("---")

    # Failure mode 3
    st.markdown("### ❌ Failure Mode 3 — Real-Time Context Delusion")
    card("""
Resources are designed for <b>context loading, not streaming</b>. Using them to feed a live
dashboard or rapidly-changing state means fighting the protocol. There's no built-in
invalidation — cached resources go stale and agents will happily work with outdated data.
""", "warn")
    card("""
<b>✅ Fix:</b> Real-time event streaming still belongs to <b>WebSockets, SSE, or a message queue</b>.
MCP is an orchestration layer, not a pub/sub system.
""", "good")


# ══════════════════════════════════════════════════════════════════════════════
# SECTION: Security
# ══════════════════════════════════════════════════════════════════════════════
elif selected == "🔒 Security":
    st.title("🔒 Security — The Problem Nobody Wants to Talk About")

    st.markdown("### By the numbers")
    col1, col2, col3 = st.columns(3)
    with col1:
        card('<div class="stat-big" style="color:#ff7043">492</div><div class="stat-label">publicly exposed MCP servers identified as vulnerable to basic abuse</div>')
    with col2:
        card('<div class="stat-big" style="color:#ff7043">437K+</div><div class="stat-label">downloads affected by CVE-2025-6514 (command injection in a popular npm package)</div>')
    with col3:
        card('<div class="stat-big" style="color:#ff7043">2025</div><div class="stat-label">GitHub MCP vuln allowed injected text in issues to exfiltrate data from private repos</div>')

    st.markdown("---")
    st.markdown("### Attack surfaces")

    with st.expander("🎯 Prompt injection through tool results", expanded=True):
        st.markdown("""
When an MCP tool fetches content from an untrusted source — a web page, a GitHub issue,
a support ticket — that content can contain instructions directed at the AI. The model
has **no reliable way to distinguish data from commands**.

An attacker controlling content that gets fetched can potentially hijack the agent's behavior.
""")

    with st.expander("📂 Path traversal in filesystem servers"):
        st.markdown("""
Several filesystem MCP implementations used naive **prefix string checks** to scope
access (e.g., only allow reads under `/data/project/`). These were trivially bypassed
with `../` sequences.

**Fix:** Use normalized, canonical path comparison — not string prefix matching.
""")

    with st.expander("📦 Supply chain compromise"):
        st.markdown("""
The MCP ecosystem is young. Many servers are third-party packages with minimal
security review. A compromised MCP library in your dependency tree has access to
whatever your MCP client is authorized to do.
""")

    st.markdown("---")
    st.markdown("### Practical mitigations")

    mitigations = [
        ("🔐", "Least privilege at tool level",
         "Don't authorize write access if a tool only needs read. Don't give filesystem access if it only needs DB queries. Scope per tool, not per server."),
        ("✅", "Input validation before execution",
         "Treat all tool inputs as untrusted. Validate against JSON schema AND semantic content — a filename resolving outside the expected directory should be rejected before any filesystem call."),
        ("📦", "Sandbox your servers",
         "Run MCP servers in containers with minimal system capabilities. Network-isolated servers can't exfiltrate data. Filesystem servers should run chrooted."),
        ("🛡️", "Treat tool results as untrusted data",
         "Before returning content from external sources to the LLM, consider whether it could contain adversarial instructions. In high-stakes contexts, a validation step is worth the latency cost."),
    ]

    for icon, title, desc in mitigations:
        card(f"<b>{icon} {title}</b><br>{desc}", "good")


# ══════════════════════════════════════════════════════════════════════════════
# SECTION: Tool Design
# ══════════════════════════════════════════════════════════════════════════════
elif selected == "🛠️ Tool Design Best Practices":
    st.title("🛠️ Designing Tools That Actually Work")

    card("""
<b>The most common mistake:</b> mapping tools 1:1 with underlying API operations.<br><br>
If your CRM has <code>createContact</code>, <code>updateContact</code>, <code>deleteContact</code>,
<code>addContactNote</code>, and <code>setContactStatus</code>, you might expose all five.
<b>Don't.</b><br><br>
LLMs are good at describing <em>intent</em>, not at orchestrating low-level API calls.
""", "warn")

    st.markdown("""
**Better approach:** Map tools to user-level workflows.
- `manage_contact` — handles create/update/delete with an `action` parameter
- `add_contact_note` — for logging interactions

This reduces the LLM's decision space and makes your tool surface easier to audit.
""")

    st.markdown("---")
    st.markdown("### The four rules that matter in practice")

    rules = [
        ("🔁", "Make tools idempotent",
         "Agents retry. Network requests fail and get retried. If calling the same tool twice creates two records, you'll have a bad time. Accept client-generated request IDs and use them to deduplicate.",
         "tip"),
        ("📄", "Paginate list operations",
         "A tool returning 'all documents' works fine with 50 records. It fails with 50,000. Cursor-based pagination with explicit size limits is not optional.",
         "warn"),
        ("🔗", "Don't chain tools inside your server",
         "If a tool internally calls other tools, you've hidden dependencies and made the server harder to test and debug. Let the LLM compose tools through the orchestration layer. Each tool = one thing.",
         "tip"),
        ("🚨", "Return structured errors, not generic failures",
         "'An error occurred' tells the LLM nothing. 'Resource not found: contact ID 8823 does not exist in the CRM' lets the model make a recovery decision. Error messages are part of your API contract.",
         "good"),
    ]

    for icon, title, desc, kind in rules:
        card(f"<b>{icon} {title}</b><br><br>{desc}", kind)

    st.markdown("---")
    st.markdown("### Quick reference")
    st.code("""
# ✅ Good tool design
@mcp.tool()
def manage_contact(action: str, contact_id: str, data: dict | None = None) -> str:
    \"\"\"Create, update, or delete a CRM contact. action = create|update|delete\"\"\"
    ...

# ❌ Bad — too granular, LLM has to orchestrate 5 API operations
@mcp.tool()
def create_contact(data: dict) -> str: ...

@mcp.tool()
def update_contact(contact_id: str, data: dict) -> str: ...
""", language="python")


# ══════════════════════════════════════════════════════════════════════════════
# SECTION: Transport & Performance
# ══════════════════════════════════════════════════════════════════════════════
elif selected == "⚡ Transport & Performance":
    st.title("⚡ Transport and Performance in Production")

    st.markdown("For any production deployment accessed remotely, use **Streamable HTTP** — not stdio.")

    col1, col2 = st.columns(2)
    with col1:
        card("""
<b>Streamable HTTP advantages</b><br><br>
✅ Handles streaming results via SSE<br>
✅ Integrates with load balancers & proxies<br>
✅ OAuth 2.1 authentication<br>
✅ Scalable beyond single-host setups
""", "good")
    with col2:
        card("""
<b>Tradeoffs to know</b><br><br>
⚠️ First call to a cold server: ~<b>2.5 s</b> warm-up<br>
&nbsp;&nbsp;&nbsp;&nbsp;(connection + capability negotiation)<br>
⚠️ Protocol overhead: 300–800 ms per call<br>
✅ Subsequent calls hit cached channel at <b>sub-ms overhead</b>
""", "warn")

    st.markdown("---")
    st.markdown("### Performance optimisation checklist")

    opts = [
        ("🔥", "Keep servers warm", "Use synthetic health-check calls to prevent cold-start latency on the first real request."),
        ("📦", "Batch independent operations", "A single batched request for 10–25 operations significantly cuts round-trip overhead."),
        ("🌊", "Stream large responses", "Don't wait for complete computation — stream results incrementally for tools that produce large outputs."),
        ("🌍", "Geo-distribute", "US-hosted MCP servers see 100–300 ms lower latency than EU/Asia for US-originating agent traffic. Match deployment to your agent's geography."),
    ]
    for icon, title, desc in opts:
        card(f"<b>{icon} {title}</b> — {desc}")

    st.markdown("---")
    st.markdown("### Observability — three metric categories to track")

    col1, col2, col3 = st.columns(3)
    with col1:
        card("""
<b>🖥️ System Health</b><br><br>
• Memory & CPU usage<br>
• Uptime<br>
• Restart frequency
""")
    with col2:
        card("""
<b>📡 Protocol Metrics</b><br><br>
• Request rate<br>
• Per-tool latency (p50/p95/p99)<br>
• Error rate by tool & error type
""")
    with col3:
        card("""
<b>📊 Business Metrics</b><br><br>
• Which tools are actually used<br>
• Data freshness age for resources<br>
• Tool call success/failure ratio
""")

    card("""
<b>⚠️ Watch-out:</b> Error rate spikes on a <em>specific</em> tool are usually the first
signal of a bad deployment or an external API change.
Without per-tool metrics, you're flying blind.
""", "warn")


# ══════════════════════════════════════════════════════════════════════════════
# SECTION: MCP vs. Alternatives
# ══════════════════════════════════════════════════════════════════════════════
elif selected == "🤔 MCP vs. Alternatives":
    st.title("🤔 When to Use MCP vs. Alternatives")

    card("""
MCP is not a replacement for function calling or OpenAPI tooling —
it's a <b>complement with a specific use case</b>.
""", "tip")

    st.markdown("---")

    col1, col2, col3 = st.columns(3)
    with col1:
        card("""
<b>⚡ Direct Function Calling</b><br><br>
<b>Use when:</b><br>
• Tightly coupled functionality inside a single app<br>
• Latency is critical (<100 ms)<br>
• Integration will never be reused elsewhere<br><br>
<em>Near-zero overhead, maximum flexibility within a single host.</em>
""", "good")
    with col2:
        card("""
<b>🔌 MCP</b><br><br>
<b>Use when:</b><br>
• Tool must be reusable across multiple AI hosts<br>
• Dynamic capability discovery matters at runtime<br>
• Building capabilities that work across multiple models without rewriting the integration layer<br><br>
<em>The right layer for cross-cutting, reusable capabilities.</em>
""")
    with col3:
        card("""
<b>📋 OpenAPI Tooling</b><br><br>
<b>Use when:</b><br>
• You have an existing, mature API with rich documentation<br>
• Integration consumers are primarily humans or traditional software (not just AI agents)<br><br>
<em>Best for existing ecosystems where the API already exists.</em>
""", "tip")

    st.markdown("---")
    st.markdown("### Common production pattern")
    card("""
Use <b>MCP</b> for cross-cutting capabilities — filesystem access, company knowledge base,
standard internal tools.<br><br>
Use <b>direct function calling</b> for application-specific logic that doesn't need to
be shared across hosts or models.<br><br>
The decision isn't binary — most mature teams use both.
""", "good")


# ══════════════════════════════════════════════════════════════════════════════
# SECTION: Production Readiness
# ══════════════════════════════════════════════════════════════════════════════
elif selected == "📊 Production Readiness":
    st.title("📊 The State of Production Readiness")

    st.markdown("### What changed in November 2025")
    card("""
The Nov 2025 MCP spec update addressed the most pressing enterprise blockers:<br>
✅ <b>Mandatory OAuth 2.1</b> for HTTP transports<br>
✅ <b>Improved error signaling</b><br>
✅ <b>Clearer lifecycle management</b><br><br>
The protocol is no longer the barrier to production deployment it was in early 2025.
""", "good")

    st.markdown("---")
    st.markdown("### What barriers remain (and they're organizational, not technical)")

    col1, col2 = st.columns(2)
    with col1:
        card("""
<b>🔄 Retrofit problem</b><br><br>
Teams that adopted early without security reviews are now retrofitting
authentication and input validation onto servers that shipped without them.
""", "warn")
    with col2:
        card("""
<b>🏗️ Monolith tech debt</b><br><br>
Teams that built kitchen-sink monoliths are discovering that operational
complexity grows with the number of tools in a single process.
""", "warn")

    st.markdown("---")
    st.markdown("### The bottom line")
    card("""
<b>The teams running MCP reliably in production aren't doing anything exotic.</b><br><br>
They're applying the same operational hygiene to MCP that good engineers apply
to any distributed system:<br><br>
🔹 <b>Single-purpose servers</b><br>
🔹 <b>Least-privilege authorization</b><br>
🔹 <b>Validated inputs</b><br>
🔹 <b>Treat tool results as untrusted data</b><br><br>
Framework choice matters less than discipline in the design.
""", "good")

    st.markdown("---")
    st.markdown("### Adoption snapshot")

    col1, col2 = st.columns(2)
    with col1:
        st.metric("Fortune 500 with MCP in production (Q1 2025)", "28%")
        st.progress(0.28)
    with col2:
        st.metric("Still watching / evaluating", "72%")
        st.progress(0.72)

    st.caption("""
What the 72% are waiting for: evidence that production deployments at scale are
survivable without heroic operational effort. That evidence is accumulating — but slowly.
""")

    st.markdown("---")
    st.markdown("### References")
    refs = [
        ("MCP Architecture Docs", "https://modelcontextprotocol.io/docs/learn/architecture"),
        ("How MCP Servers Work — WorkOS", "https://workos.com/blog/how-mcp-servers-work"),
        ("15 Best Practices for Building MCP Servers — The New Stack", "https://thenewstack.io/15-best-practices-for-building-mcp-servers-in-production/"),
        ("MCP Security: Current Situation — Red Hat", "https://www.redhat.com/en/blog/mcp-security-current-situation"),
        ("MCP vs. Function Calling vs. OpenAPI — Rajeev Barnwal", "https://rajeevbarnwal.medium.com/model-context-protocol-mcp-vs-function-calling-vs-openapi-tools-when-to-use-each-547f3d59c5da"),
        ("MCP Adoption Statistics", "https://mcpmanager.ai/blog/mcp-adoption-statistics/"),
        ("Original article — tianpan.co", "https://tianpan.co/blog/2026-03-05-model-context-protocol-production-guide"),
    ]
    for title, url in refs:
        st.markdown(f"- [{title}]({url})")
