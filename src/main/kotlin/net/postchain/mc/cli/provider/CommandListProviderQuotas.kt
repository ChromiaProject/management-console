package net.postchain.mc.cli.provider

import com.chromia.cli.tools.formatter.defaultTable
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.chain0.common.queries.getProviderQuotas
import net.postchain.chain0.model.ProviderQuotaType
import net.postchain.chain0.model.ProviderTier.COMMUNITY_NODE_PROVIDER
import net.postchain.chain0.model.ProviderTier.NODE_PROVIDER
import net.postchain.mc.cli.util.pmcConfigOption


class CommandListProviderQuotas : CliktCommand(
        name = "quotas",
        help = "List provider quotas"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    override fun run() {
        val quotas = client.getProviderQuotas().associate {
            (it.providerQuotaType to it.tier) to it.value
        }

        if (quotas.isEmpty()) {
            echo("No provider quotas")
        } else {
            echo(defaultTable {
                header { row("Quotas", NODE_PROVIDER.name, COMMUNITY_NODE_PROVIDER.name) }
                body {
                    ProviderQuotaType.values().forEach {
                        row(
                                it.name,
                                quotas[it to NODE_PROVIDER]?.toString() ?: "n/a",
                                quotas[it to COMMUNITY_NODE_PROVIDER]?.toString() ?: "n/a")
                    }
                }
            })
        }
    }
}
