package net.postchain.mc.cli.provider

import net.postchain.mc.cli.PmcCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.chain0.common.queries.getProviderQuotas
import net.postchain.chain0.model.ProviderQuotaType
import net.postchain.chain0.model.ProviderTier.DAPP_PROVIDER
import net.postchain.chain0.model.ProviderTier.NODE_PROVIDER
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable

class CommandListProviderQuotas : PmcCommand(
        name = "quotas",
        help = "List provider quotas"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    override fun run() {
        val quotas = client.getProviderQuotas().associate {
            (it.providerQuotaType to it.tier) to it.value
        }

        echo(pmcTable(
                "provider quotas",
                listOf("Quotas", NODE_PROVIDER.name, DAPP_PROVIDER.name),
                ProviderQuotaType.values().map {
                    listOf(
                            it.name,
                            quotas[it to NODE_PROVIDER]?.toString() ?: "n/a",
                            quotas[it to DAPP_PROVIDER]?.toString() ?: "n/a")
                }
        ))
    }
}
