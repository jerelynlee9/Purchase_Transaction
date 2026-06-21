Purchase Transaction & Currency Conversion Service

This is a Spring Boot REST API built with Java 21 that stores purchase 
transactions in USD and retrieves them converted into target foreign currencies 
using live data from the U.S. Treasury Reporting Rates of Exchange API.

-------------------------------------------------------------------------------
1. TECHNICAL DESIGN & CONSTRAINTS
-------------------------------------------------------------------------------

* Precision Financial Math: 
  Uses BigDecimal for all monetary tracking to eliminate floating-point 
  calculation errors. Transaction amounts are automatically scaled using 
  standard banking rounding (RoundingMode.HALF_UP).

* Conversion Rules: 
  Queries the U.S. Fiscal Data API for historical records, sorting by date 
  descending to find the closest valid exchange rate that is less than or 
  equal to the purchase date within a rolling 6-month historical boundary.

* Local Setup: 
  Uses an embedded, in-memory H2 database instance and WireMock for isolated 
  unit/integration tests. There are no external databases, Docker containers, 
  or global API keys needed to run or evaluate the project.

-------------------------------------------------------------------------------
2. HOW TO RUN THE APPLICATION LOCALLY
-------------------------------------------------------------------------------

You will need Java 21 installed on your system.

Step 1: Build and Test
Open your terminal in the root directory of the project and run:
    ./mvnw clean verify
    
    * Note for Windows users: Use ".\mvnw.cmd clean verify" instead.

Step 2: Start the Server
Launch the local development server on port 8080:
    ./mvnw spring-boot:run
    
    * Note for Windows users: Use ".\mvnw.cmd spring-boot:run" instead.

-------------------------------------------------------------------------------
3. TESTING THE ENDPOINTS (EXAMPLES)
-------------------------------------------------------------------------------

You can verify the API behavior using standard curl commands in a separate 
terminal window.

Scenario 1: Store a Transaction (Requirement #1)
Submit a new purchase. The amount below uses three decimal places (149.995) 
to verify that the system rounds to the nearest cent:

    curl -X POST http://localhost:8080/api/transactions \
      -H "Content-Type: application/json" \
      -d "{ \"description\": \"Premium Wireless Headphones\", \"transactionDate\": \"2026-05-15\", \"purchaseAmount\": 149.995 }"

Expected Response (201 Created):
    {
      "id": "7b8a2c1d-4e9f-4321-a1b2-c3d4e5f6a7b8",
      "description": "Premium Wireless Headphones",
      "transactionDate": "2026-05-15",
      "purchaseAmountUsd": 150.00
    }

* Note: Copy the "id" value from your actual terminal response to use in the 
  conversion step below.

Scenario 2: Convert to a Target Currency (Requirement #2)
Replace {UUID} with the actual transaction ID generated in the step above:

    curl "http://localhost:8080/api/transactions/{UUID}/convert?targetCurrency=Euro"

Expected Response (200 OK):
    {
      "id": "7b8a2c1d-4e9f-4321-a1b2-c3d4e5f6a7b8",
      "description": "Premium Wireless Headphones",
      "transactionDate": "2026-05-15",
      "originalAmountUsd": 150.00,
      "targetCurrency": "Euro",
      "exchangeRate": 0.92,
      "convertedAmount": 138.00
    }

-------------------------------------------------------------------------------
4. ERROR HANDLING & EDGE CASES
-------------------------------------------------------------------------------

Error Scenario: Validation Failure (Description > 50 characters)
- Test Input: "This description is far too long to meet the fifty character boundary limit..."
- Expected Behavior: 400 Bad Request with a JSON payload explaining which fields failed validation rules.

Error Scenario: Validation Failure (Negative amount)
- Test Input: "purchaseAmount": -5.00
- Expected Behavior: 400 Bad Request explaining that the amount must be a positive value.

Error Scenario: 6-Month Boundary Failure
- Test Input: ?targetCurrency=FictionalCoin
- Expected Behavior: 400 Bad Request returning the custom error message below.

Sample Boundary Failure Response:
    {
      "timestamp": "2026-06-21T10:11:34",
      "status": 400,
      "error": "The purchase cannot be converted to the target currency. No conversion rate available within 6 months prior to the purchase date."
    }
