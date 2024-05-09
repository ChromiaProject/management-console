package net.postchain.mc.cli.util

import net.postchain.chain0.model.ProviderTier

enum class ProviderType {
    DAPP_PROVIDER,
    NODE_PROVIDER,
    SYSTEM_PROVIDER;

    fun toTier() = when (this) {
        DAPP_PROVIDER -> ProviderTier.DAPP_PROVIDER
        else -> ProviderTier.NODE_PROVIDER
    }

    fun isSystem() = this == SYSTEM_PROVIDER

    fun shouldEnable(enable: Boolean) = enable && this == NODE_PROVIDER
}
