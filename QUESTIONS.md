# Questions

Here are 2 questions related to the codebase. There's no right or wrong answer - we want to understand your reasoning.

## Question 1: API Specification Approaches

When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded everything directly. 

What are your thoughts on the pros and cons of each approach? Which would you choose and why?

**Answer:**
```txt
OPENAPI-FIRST APPROACH (Code Generation from YAML):

Pros:
- Contract-first design ensures API consistency and acts as single source of truth
- Automatic code generation reduces boilerplate and manual errors
- Strong type safety through generated classes
- API documentation is always in sync with implementation
- Client code generation is straightforward from the same spec
- Great for cross-team coordination and API versioning
- Easier to validate compliance with API specifications
- Good for REST API design best practices enforcement

Cons:
- Learning curve for build pipeline and code generation tooling
- Initial setup overhead and configuration complexity
- Generated code can be verbose or opinionated
- Harder to customize generated code without breaking regeneration
- Slower iteration during early development/exploration
- Requires discipline to keep spec in sync if manual changes are made

HAND-CODED APPROACH (Direct Implementation):

Pros:
- Maximum flexibility and control over implementation details
- Faster for simple endpoints or rapid prototyping
- No build-time code generation dependencies
- Easier to add custom logic, middleware, or special handling
- More direct correlation between written and running code
- Lower initial friction for quick implementations
- Better for highly customized or complex business logic

Cons:
- API documentation easily gets out of sync with implementation
- No automatic consistency checking across endpoints
- Manual type safety - easier to introduce inconsistencies
- No single source of truth for API contract
- Harder to generate client code or keep documentation current
- More prone to human error in implementing API patterns
- Difficult to enforce organizational API standards

MY RECOMMENDATION:

I would choose the OpenAPI-first approach for this system, for several reasons:

1. This is a production-grade fulfillment system where API consistency is critical. The Warehouse API having contracts enforces reliability across the system.

2. For the Store and Product APIs, I would recommend gradually transitioning them to OpenAPI generation. While they started hand-coded, as the system grows, having all APIs spec-driven reduces technical debt.

3. Hybrid approach: Use OpenAPI generation for all public APIs and core business domains (like Warehouse). Use hand-coding only for internal/admin endpoints that don't need strict contract guarantees.

4. The upfront cost of OpenAPI setup pays dividends in maintainability, consistency, and team productivity as the system scales.

5. In a hackathon context, I understand the pragmatic choice to hand-code simple endpoints. But for long-term maintenance, spec-first is superior.

The key insight: OpenAPI generation is most valuable for complex systems with multiple consumers (internal APIs, mobile apps, third parties). For a monolithic fulfillment system, this absolutely applies.
```

---

## Question 2: Testing Strategy

Given the need to balance thorough testing with time and resource constraints, how would you prioritize tests for this project? 

Which types of tests (unit, integration, parameterized, etc.) would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
TESTING PRIORITY FRAMEWORK:

For this fulfillment system, I would use a risk-based prioritization strategy:

TIER 1 - CRITICAL (MUST HAVE):
1. Business Logic Unit Tests (Use Cases)
   - Archive, Replace, Create warehouse operations
   - Each business rule has a dedicated test case
   - Fast execution, high value, easy to maintain
   - Example: ArchiveWarehouseUseCaseTest, CreateWarehouseUseCaseTest

2. Concurrency/Optimistic Locking Tests
   - Prevent data loss in concurrent scenarios
   - Test transaction boundaries and optimistic locking conflicts
   - Critical for correctness: ArchiveWarehouseUseCaseTest#testConcurrentArchiveAndStockUpdateCausesOptimisticLockException
   - Value: Prevents customer-impacting bugs in production

3. Integration Tests for Transaction Boundaries
   - Event publishing only on successful commits (StoreTransactionIntegrationTest)
   - Ensures legacy system never gets invalid data
   - Critical for system reliability

TIER 2 - HIGH VALUE:
1. Parameterized Tests for Validation Rules
   - Test boundary conditions with different parameter combinations
   - Example: capacity > location max, stock > capacity, invalid locations
   - Good ROI: one test method covers many scenarios

2. Database Integration Tests
   - Repository layer correctness (WarehouseTestcontainersIT)
   - Schema validation and constraints
   - Run less frequently but critical for persistence layer

TIER 3 - NICE TO HAVE (IF TIME PERMITS):
1. REST Endpoint Integration Tests
   - End-to-end API testing (WarehouseEndpointIT)
   - Response status codes, error handling
   - Run in separate CI pipeline since they require infrastructure

2. Performance/Load Tests
   - Concurrent user load testing
   - Database connection pool saturation scenarios
   - Run nightly, not on every commit

TIER 4 - OPTIONAL:
1. API Contract Tests (Consumer-Driven)
2. Mutation Testing (verify test quality)
3. Stress Testing under edge cases

RECOMMENDED COVERAGE LEVELS:
- Business logic (use cases): 100% line coverage
- Repository/DAL: 80%+ coverage for critical paths
- REST endpoints: 70%+ coverage (redundant with unit tests)
- Overall target: 80-85% code coverage

MAINTAINING TEST EFFECTIVENESS OVER TIME:

1. Test Organization:
   - Separate unit tests (*Test.java) from integration tests (*IT.java)
   - Run unit tests on every commit (< 2 minutes total)
   - Run integration tests in separate CI stage (can take longer)

2. Test Maintenance:
   - Review failing tests first before production code changes
   - Refactor flaky tests immediately - they lose value if unreliable
   - Remove obsolete tests when features are removed

3. Documentation:
   - Each test class should have clear Javadoc explaining scenario
   - Use descriptive test method names: testConcurrentArchiveAndStockUpdateCausesOptimisticLockException
   - Document why edge cases are tested (business requirement context)

4. CI/CD Integration:
   - Unit tests: Required to pass before merge to main
   - Integration tests: Required to pass, but can run in parallel
   - Coverage gates: Fail builds if coverage drops below baseline (80%)
   - Test trend analysis: Track which tests fail most often (signals design issues)

5. Test Data Strategy:
   - Use test fixtures (shared setup) for common scenarios
   - Use factory patterns for test data creation
   - Keep test data minimal and focused on what's being tested

6. Continuous Improvement:
   - Monthly review of test flakiness and execution time
   - Identify and eliminate redundant tests
   - Add tests for bugs found in production
   - Adjust priorities based on actual production incidents

SPECIFIC TO THIS PROJECT:

Given the existing test suite, I would:

1. Keep all existing unit tests - they're fast and catch real issues (as evidenced by the
   optimistic locking test failure we fixed)

2. Enhance with parameterized tests for warehouse capacity validation scenarios:
   - Combinations of location max capacity vs warehouse capacity vs stock
   - Invalid location codes
   - Boundary values

3. Reduce flakiness in concurrent tests:
   - Increase timeouts in CI environment where resources are constrained
   - Add detailed logging for debugging failures

4. Document test-to-requirement mapping:
   - Each test should reference the business rule it validates
   - Makes it clear what scenarios are covered

5. Skip WarehouseEndpointIT in standard CI (requires external DB):
   - Run it separately in a Docker environment or nightly pipeline
   - Prevents false failures from infrastructure issues

BOTTOM LINE:
Test the business logic thoroughly (100% for use cases). Test concurrency and transaction
boundaries religiously (this is where real production bugs hide). Test REST endpoints as
necessary for contract validation. Automate everything. Keep tests fast so developers run them
locally. Use coverage metrics to guide but don't obsess over hitting arbitrary percentages.
```

