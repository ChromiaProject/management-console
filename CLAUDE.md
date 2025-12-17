# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

The Management Console (`pmc`) is a CLI tool for interacting with the directory chain (management chain) on a Chromia network. It provides commands to manage nodes, providers, clusters, containers, blockchains, and other network resources.

## Build Commands

### Basic Build Commands
- Build and package: `mvn clean package -DskipTests`
- Build with tests: `mvn clean package`
- Build with local directory chain: `mvn package -Dlocal` (uses `../directory-chain/src`)
- Build with custom directory chain path: `mvn package -Dlocal -Ddirectory.source=/path/to/directory-chain/src`
- Build Docker image: `mvn package -Pdocker`
- Build and push multi-arch Docker image: `mvn package -Pdocker-push`
- Suppress Rell warnings in output: `mvn clean package -DskipTests -Dorg.slf4j.simpleLogger.log.net.postchain.rell=warn`

**Note**: During build, the Rell compiler may output many `[ERROR]` messages that are actually warnings from the directory-chain Rell code (e.g., "Invalid comment tag", "Expression cannot be null"). These can be safely ignored as they don't affect the build. The `warningsAsErrors=false` configuration in pom.xml ensures they don't fail the build.

### Testing
- Run all tests: `mvn test`
- Run single test: `mvn test -Dtest=TestClassName`
- Run specific test method: `mvn test -Dtest=ClassName#methodName`
- Run integration tests: `mvn verify`
- Run integration tests on Windows: `mvn verify -Pwindows`
- Run single integration test: `mvn verify -Dit.test=ITClassName`
- Run only integration tests: `mvn test -Dtest="*IT"`

**Note**: On Windows, you must use the `-Pwindows` profile when running integration tests (`mvn verify -Pwindows`). This ensures the tests use the `pmc.bat` script instead of the Unix `pmc` shell script. Mac and Linux users should run `mvn verify` without the profile.

## Architecture

### CLI Command Structure

The CLI is built using Clikt framework with a hierarchical command structure:

- **Entry Point**: `ManagementConsole` class (extends `CliLauncher`) in `ManagementConsole.kt`
- **Main Function**: `directory1.kt` initializes `ManagementConsole` and can add extra provider commands
- **Command Categories**: blockchain, cluster, container, economy, image, lease, node, proposal, provider, voterset, network

### Command Base Classes

Commands inherit from one of three base classes depending on their needs:

1. **PmcCommand**: Base class for query-only commands (no transaction signing)
2. **DCBaseCommand**: For commands that send transactions to the directory chain (requires keys/signing)
   - Provides access to `client`, `dcVersion`, `clientProviderPubkey`
   - Handles provider key resolution and time-bound transactions
3. **ECBaseCommand**: For commands that interact with the economy chain
   - Extends `DCBaseCommand`
   - Provides access to both directory chain and economy chain clients

### Blockchain Integration

The CLI interacts with multiple blockchain types:

- **Directory Chain (DC)**: Main management blockchain for the network
- **Economy Chain (EC)**: Handles economic operations and proposals
- **System Anchoring Chain (SAC)**: System-level anchoring
- **Cluster Anchoring Chain (CAC)**: Cluster-specific anchoring
- **Token Chain**: Token management operations

### API Compatibility Layer

The `compatibility/` package contains versioned API compatibility classes (`ApiCompatV2`, `ApiCompatV22`, etc.) that handle different directory chain versions. This ensures backward compatibility with deployed networks.

### Configuration

Configuration is hierarchical with three levels (in order of precedence):
1. Environment variable: `CHROMIA_CONFIG`
2. Local config: `.chromia/config`
3. Global config: `~/.chromia/config`

Key configuration properties:
- `api.url`: Node API URL (can be comma-delimited list)
- `brid`: Blockchain RID of directory chain
- `pubkey`/`privkey`: Key pair for signing transactions
- `provider.pubkey`: Default provider for commands (auto-looked up if not set)

### Rell Client Generation

The build process generates Kotlin client code from Rell modules using `rell-maven-plugin`:
- Source: `target/directory-chain/src` (or custom path with `-Ddirectory.source`)
- Generated output: `target/generated-sources/rell-client/`
- Modules: `management_chain_mainnet`, `direct_cluster`, `direct_container`, `anchoring_chain_cluster`, `economy_chain_test`, `token_chain`

## Code Style

- Language: Kotlin 2.0.0 targeting JVM 21
- Code style: IntelliJ IDEA obsolete codestyle (`kotlin.code.style=obsolete` in pom.xml)
- Line length: Keep under 120 characters
- Imports: Organize alphabetically, no wildcard imports
- Naming: camelCase for variables/functions, PascalCase for classes
- Error handling: Use `CliktError` for user-facing errors, Result pattern or exceptions with meaningful messages
- Testing: JUnit 5 with assertk for assertions
- Test naming: `*Test.kt` for unit tests, `*IT.kt` for integration tests

## Testing Framework

### Test Structure

Tests use `ManagedRestTestApi` from `test_helpers/` package, which provides:
- Mock REST API server setup
- Blockchain model management
- Query response mocking
- Automatic configuration file generation
- Fluent test API

### Test Pattern Example

```kotlin
@Test
fun testExample(@TempDir dir: Path) {
    ManagedRestTestApi(dir)
        .withDCQuery("query_name", buildResponseFunction())
        .withECQuery("another_query", responseValue)
        .testCommand(CommandClass(), "-arg", "value") { result, api ->
            assertLineValue(result.stdout, "Field_Name", "expected_value")
        }
}
```

**Important**: Test JSON output, not formatted table output. Focus on command execution and expected status codes.

## Development Patterns

### Adding New Commands

1. Create command class extending appropriate base (`PmcCommand`, `DCBaseCommand`, or `ECBaseCommand`)
2. Define options/arguments using Clikt parameters
3. Implement `run()` (for `PmcCommand`) or `runDC()`/`runEC()` (for base command subclasses)
4. Register command in the appropriate command builder function (e.g., `blockchainCommands()`)
5. Add tests in mirror package structure under `src/test/kotlin/`

### Transaction Building

For commands that send transactions, use the transaction builder pattern:

```kotlin
transactionBuilder()
    .addOperation("operation_name", params...)
    .sign(config.config.signers)
    .buildAndSendOrPrint()
```

### Table Formatting

Use `pmcTable()` utility for consistent table output. The codebase provides specialized table builders in `util/table.kt` and `base/table.kt`.

### Version Compatibility

- Check directory chain version with `dcVersion` in `DCBaseCommand`
- Use `requireApiVersion()` to enforce minimum versions
- Add compatibility layer in `compatibility/` package for version-specific APIs
- The tool maintains backward compatibility with publicly deployed networks

## Releases

After the dev branch is built in CI, create releases by starting either `release-patch` or `release-minor` stage in the pipeline:
- **Minor release**: New command added/removed or changed behavior
- **Patch release**: Bug fixes or minor aesthetic improvements
