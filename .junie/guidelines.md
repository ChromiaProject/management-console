# Development Guidelines - Postchain Management Console

This document contains essential development information for the Postchain Management Console project.

## Build/Configuration Instructions

### Prerequisites
- Java 21 (JDK)
- Maven 3.x
- Kotlin 2.0.0

### Build Configuration
This project uses Maven with the following key configurations:
- **Main Class**: `net.postchain.mc.Directory1Kt`
- **Kotlin Target**: JVM 21
- **Code Style**: Kotlin obsolete IntelliJ IDEA codestyle
- **Parent POM**: `com.chromia:chromia-parent:0.2.0`

### Key Dependencies
- **Postchain**: core blockchain functionality
- **Postchain Client**: client libraries
- **Directory Chain**: directory chain integration
- **Clikt**: CLI framework
- **Mordant**: Terminal UI framework
- **JUnit**: testing framework

### Build Commands
```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package application
mvn package

# Build Docker image (requires Docker)
mvn install -Pdocker
```

### Rell Integration
The project includes Rell compilation via `rell-maven-plugin`:
- Rell sources are compiled from `target/directory-chain/src`
- Configuration file: `target/directory-chain/chromia.yml`
- Generated client code is placed in `target/generated-sources/rell-client`

## Testing Information

### Testing Framework
The project uses **JUnit 5** with a comprehensive custom testing framework built around `ManagedRestTestApi`.

### Test Structure
- **Test Location**: `src/test/kotlin/`
- **Test Helpers**: `src/test/kotlin/net/postchain/mc/cli/test_helpers/`
- **Integration Tests**: Use `IT` suffix (e.g., `CommandGetBlockchainInfoIT.kt`)

### Key Testing Components

#### ManagedRestTestApi
The core testing utility that provides:
- Mock REST API server setup
- Blockchain model management (DC, EC, SAC, CAC chains)
- Query response mocking
- Automatic configuration file generation
- Fluent API for test setup

#### Test Patterns
```kotlin
@Test
fun testExample(@TempDir dir: Path) {
    ManagedRestTestApi(dir)
        .withDCQuery("query_name", buildResponseFunction())
        .withECQuery("another_query", responseValue)
        .afterServerBeforeTest { api ->
            // Setup that requires running server
        }
        .testCommand(CommandClass(), "-arg", "value") { result, api ->
            assertLineValue(result.stdout, "Field_Name", "expected_value")
        }
}
```
Primarily test the JSON output but also verify that the command is properly executed and returns the expected status code.
Do not try to test the formatted output.


### Running Tests

#### Single Test File
```bash
mvn test -Dtest=SimpleTestExample
```

#### All Tests
```bash
mvn test
```

#### Integration Tests Only
```bash
mvn test -Dtest="*IT"
```

### Test Example
A working test example demonstrating the framework:

```kotlin
package net.postchain.mc.cli

import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class SimpleTestExample {
    @Test
    fun `simple test framework demonstration`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
            .test { api ->
                // Verify that the API URL is properly set
                assert(api.apiUrl.startsWith("http://localhost:"))
                println("[DEBUG_LOG] Test API URL: ${api.apiUrl}")
                
                // Verify that models are properly initialized
                val dcModel = api.getDcModel()
                assert(dcModel != null)
                println("[DEBUG_LOG] Directory chain model initialized successfully")
            }
    }
}
```

### Adding New Tests
1. Create test class in appropriate package under `src/test/kotlin/`
2. Use `@Test` annotation for test methods
3. Use `@TempDir` for temporary directory injection
4. Leverage `ManagedRestTestApi` for CLI command testing
5. Use helper functions from `test_helpers` package
6. Follow naming convention: `*Test.kt` for unit tests, `*IT.kt` for integration tests

## Development Information

### Code Style and Conventions

#### General Guidelines
- **Language**: Kotlin with functional programming patterns
- **Code Style**: IntelliJ IDEA obsolete codestyle
- **Target JVM**: 21
- **Package Structure**: Follows domain-driven design (cli.blockchain, cli.cluster, cli.economy, etc.)

#### CLI Command Structure
All CLI commands follow this pattern:
```kotlin
class CommandName : CliktCommand(help = "Description") {
    
    // Options and arguments
    private val config by pmcConfigOption()
    private val blockchainRid by blockchainRidOption().required()
    
    override fun run() {
        // Implementation using PostchainReadClient and ChromiaClient
    }
}
```

#### Key Patterns
- **Command Classes**: Extend `CliktCommand` from Clikt library
- **Configuration**: Use `pmcConfigOption()` for configuration injection
- **Client Usage**: Leverage `PostchainReadClient` and `ChromiaClient` for blockchain operations
- **Error Handling**: Use `CliktError` for user-facing errors
- **Table Formatting**: Use `pmcTable()` and `prettyTable()` utilities
- **Blockchain Operations**: Separate business logic into utility functions

#### Dependencies and Imports
- Prefer specific imports over wildcard imports
- Group imports logically (stdlib, external, internal)
- Use consistent import ordering

### Directory Structure
```
src/
├── main/kotlin/net/postchain/mc/cli/
│   ├── blockchain/     # Blockchain-related commands
│   ├── cluster/        # Cluster management commands
│   ├── economy/        # Economy chain commands
│   ├── proposal/       # Proposal management commands
│   └── util/           # Utility functions and helpers
└── test/kotlin/net/postchain/mc/cli/
    ├── test_helpers/   # Test utilities and helpers
    └── [mirror main structure for tests]
```

### Configuration Management
- Configuration files are stored in `.chromia/config`
- Default configuration includes API URL, public/private keys, and blockchain RIDs
- Use `pmcConfigOption()` for consistent configuration handling across commands

### Blockchain Integration
- **Directory Chain (DC)**: Main management blockchain
- **Economy Chain (EC)**: Economic operations and proposals
- **System Anchoring Chain (SAC)**: System-level anchoring
- **Cluster Anchoring Chain (CAC)**: Cluster-specific anchoring

### Error Handling
- Use `CliktError` for user-facing command-line errors
- Implement proper exception handling for blockchain operations
- Provide meaningful error messages for troubleshooting
