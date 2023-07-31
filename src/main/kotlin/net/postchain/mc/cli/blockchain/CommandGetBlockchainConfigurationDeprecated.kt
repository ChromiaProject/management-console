package net.postchain.mc.cli.blockchain

@Deprecated("Use CommandGetBlockchainConfiguration")
open class CommandGetBlockchainConfigurationDeprecated(
        name: String = "get",
        help: String = "Get blockchain configuration. Deprecated: Use get-configuration command."
) : CommandGetBlockchainConfiguration(name, help)
