# Chromia Management Console Development Guide

## Build Commands
- Build: `mvn clean package -DskipTests`
- Run tests: `mvn test`
- Run single test: `mvn test -Dtest=TestClassName` (or `mvn test -Dtest=ClassName#methodName`)
- Run integration tests: `mvn verify`
- Run single integration test: `mvn verify -Dit.test=ITClassName`

## Code Style
- Kotlin style: Follow Kotlin conventions with obsolete IntelliJ IDEA codestyle
- Imports: Organize imports alphabetically, no wildcard imports
- Naming: camelCase for variables/functions, PascalCase for classes
- Error handling: Use Result pattern or exceptions with meaningful messages
- Tests: Unit tests end with "Test.kt", integration tests end with "IT.kt"
- Testing: Use JUnit 5 with assertk for assertions
- Documentation: Add KDoc comments for public APIs
- Line length: Keep under 120 characters

## Project Structure
Directory chain management console for Chromia Network