Purchase Transaction & Currency Conversion Service
This is a Spring Boot REST API built with Java 21 that allows you to store purchase transactions in USD and retrieve them converted into target foreign currencies using live data from the U.S. Treasury Reporting Rates of Exchange API.

Technical Design & Constraints
Precision Financial Math: Uses BigDecimal for all monetary tracking to eliminate floating-point calculation errors. Transaction amounts are automatically scaled using banking rounding (RoundingMode.HALF_UP).

Conversion Rules: Queries the U.S. Fiscal Data API for historical records, sorting by date descending to find the closest valid exchange rate that is less than or equal to the purchase date within a rolling 6-month historical boundary.

Local Setup: Uses an embedded, in-memory H2 database instance and WireMock for isolated unit/integration tests. There are no external databases, Docker containers, or global API keys needed to run or evaluate the project.

Project Structure
Plaintext
my-purchase-app/
├── pom.xml                        # Maven dependencies
├── mvnw                           # Linux/Mac build wrapper
├── mvnw.cmd                       # Windows build wrapper
└── src/
    ├── main/
    │   ├── java/com/example/purchase/
    │   │   ├── PurchaseApplication.java          # Application bootstrapper
    │   │   ├── client/TreasuryApiClient.java      # U.S. Fiscal Data API client
    │   │   ├── controller/TransactionController.java  # REST API endpoints
    │   │   ├── domain/PurchaseTransaction.java    # Database entity model
    │   │   ├── dto/                               # Request/Response payloads
    │   │   │   ├── CreateTransactionRequest.java
    │   │   │   └── ConvertedTransactionResponse.java
    │   │   ├── exception/                         # Error handling logic
    │   │   │   ├── CurrencyConversionException.java
    │   │   │   ├── TransactionNotFoundException.java
    │   │   │   └── GlobalExceptionHandler.java     # JSON error formatter
    │   │   └── service/TransactionService.java    # Core business logic
    │   └── resources/
    │       └── application.yml            # Local settings
    └── test/
        └── java/com/example/purchase/
            └── TransactionIntegrationTest.java   # Mock-isolated integration test
How to Run the Application Locally
You will need Java 21 installed on your system.

1. Build and Test
Open your terminal in the root directory of the project and run the build command to ensure all automated test suites pass:

Bash
./mvnw clean verify
(On Windows, use .\mvnw.cmd clean verify instead)

2. Start the Server
Launch the local development server on port 8080:

Bash
./mvnw spring-boot:run
(On Windows, use .\mvnw.cmd spring-boot:run instead)

Testing the Endpoints (Examples)
You can verify the API behavior using standard curl commands in a separate terminal window.

1. Store a Transaction (Requirement #1)
Submit a new purchase. The amount below uses three decimal places (149.995) to show that the system rounds to the nearest cent:

Bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d "{
    \"description\": \"Premium Wireless Headphones\",
    \"transactionDate\": \"2026-05-15\",
    \"purchaseAmount\": 149.995
  }"
Expected Response (201 Created):

JSON
{
  "id": "7b8a2c1d-4e9f-4321-a1b2-c3d4e5f6a7b8",
  "description": "Premium Wireless Headphones",
  "transactionDate": "2026-05-15",
  "purchaseAmountUsd": 150.00
}
(Copy the id value from your actual terminal response to use in the next step).

2. Convert to a Target Currency (Requirement #2)
Replace {UUID} with the actual transaction ID generated in the step above:

Bash
curl "http://localhost:8080/api/transactions/{UUID}/convert?targetCurrency=Euro"
Expected Response (200 OK):

JSON
{
  "id": "7b8a2c1d-4e9f-4321-a1b2-c3d4e5f6a7b8",
  "description": "Premium Wireless Headphones",
  "transactionDate": "2026-05-15",
  "originalAmountUsd": 150.00,
  "targetCurrency": "Euro",
  "exchangeRate": 0.92,
  "convertedAmount": 138.00
}
3. Error Handling and Edge Cases
Validation Failure: Submitting a description longer than 50 characters or a negative amount will be blocked automatically:

Bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d "{\"description\":\"This description is far too long to meet the fifty character boundary limit constraint request\", \"transactionDate\":\"2026-05-15\", \"purchaseAmount\":-5.00}"
Expected Result: 400 Bad Request with a JSON payload explaining the fields that failed validation rules.

6-Month Boundary Failure: Requesting a conversion for a currency with no recorded exchange rates within 6 months prior to the purchase date:

Bash
curl "http://localhost:8080/api/transactions/{UUID}/convert?targetCurrency=FictionalCoin"
Expected Result: 400 Bad Request displaying the exact required message:
"The purchase cannot be converted to the target currency. No conversion rate available within 6 months prior to the purchase date."
