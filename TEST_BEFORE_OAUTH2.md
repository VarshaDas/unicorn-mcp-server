# Testing Unicorn MCP Server - BEFORE OAuth2

## Current State: NO SECURITY
Anyone can call any tool without authentication.

## Available Tools:
1. **BookingTool**
   - `createBooking(customerName, packageName, bookingDate, duration, includeInsurance)`
   - `getBookings(customerName)`

2. **PricingCalculatorTool**
   - `calculatePrice(packageName, pricePerUnit, duration, includeInsurance)`

3. **DateTimeTool**
   - `getCurrentDateTime(timezone)`
   - `hoursUntil(futureDateTime)`
   - `resolveRelativeDate(dayReference)`

## Test Commands (Server running on port 8083):

### 1. Test MCP Server Info
```bash
curl -X GET http://localhost:8083/mcp/server/info
```

### 2. List Available Tools
```bash
curl -X GET http://localhost:8083/mcp/tools/list
```

### 3. Test Pricing Calculator (Safe - Read Only)
```bash
curl -X POST http://localhost:8083/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{
    "name": "calculatePrice",
    "arguments": {
      "packageName": "SPARKLE",
      "pricePerUnit": 100,
      "duration": 3,
      "includeInsurance": true
    }
  }'
```

### 4. Test Date Tool (Safe - Read Only)
```bash
curl -X POST http://localhost:8083/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{
    "name": "getCurrentDateTime",
    "arguments": {
      "timezone": "America/New_York"
    }
  }'
```

### 5. Test Booking Creation (DANGEROUS - Writes to Database!)
```bash
curl -X POST http://localhost:8083/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{
    "name": "createBooking",
    "arguments": {
      "customerName": "Anonymous Hacker",
      "packageName": "MAGICAL_KINGDOM",
      "bookingDate": "2025-12-25",
      "duration": 8,
      "includeInsurance": false
    }
  }'
```

### 6. Verify Booking Was Created
```bash
curl -X POST http://localhost:8083/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{
    "name": "getBookings",
    "arguments": {
      "customerName": "Anonymous Hacker"
    }
  }'
```

## Expected Results:
- ✅ All requests succeed (200 OK)
- ✅ Tools execute without any authentication
- ✅ Database gets modified by anonymous users
- ❌ **SECURITY PROBLEM**: Anyone can create bookings!

## Next: Implement OAuth2 Security
After testing this unsecured state, we'll create a new branch and add OAuth2 protection.