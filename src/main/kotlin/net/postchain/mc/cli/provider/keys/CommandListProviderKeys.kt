package net.postchain.mc.cli.provider.keys

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import net.postchain.chain0.common.queries.getAllProviders
import net.postchain.chain0.common.queries.getProviderKeys
import net.postchain.common.toHex
import net.postchain.crypto.PubKey
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.util.optionalPubkeyOption
import net.postchain.mc.cli.util.pmcTable

class CommandListProviderKeys : DCBaseCommand(
        name = "list",
        help = "List all keys used by provider to sign transactions",
        requiresVersion = 65,
        printHelpOnEmptyArgs = false
) {
    private val provider by optionalPubkeyOption()
    private val allProviders by option("-a", "--all", help = "List all providers keys").flag()

    private val singleProviderHeader = listOf("Role", "Threshold", "Key")
    private val multiProviderHeader = listOf("Provider", "Role", "Threshold", "Key")

    override fun runDC() {

        val providers = getProviders()
        val multipleProviders = providers.size > 1

        val table = mutableListOf<List<String>>()
        providers.forEach {
            val providerKeys = client.getProviderKeys(it)
            providerKeys.keys.forEach { role ->
                role.keys.forEach { key ->
                    table.add(listOf(
                            if (multipleProviders) providerKeys.providerPubkey.toHex() else null,
                            role.role.name,
                            role.threshold.toString(),
                            key.data.toHex()
                    ).filterNotNull())
                }
            }
        }

        echo(pmcTable(
                if (multipleProviders) "keys for all providers" else "keys for provider ${providers[0].data.toHex()}",
                if (multipleProviders) multiProviderHeader else singleProviderHeader,
                table,
        ))
    }

    private fun getProviders(): List<PubKey> {
        return if (allProviders) {
            client.getAllProviders().map { PubKey(it.pubkey) }
        } else if (provider != null) {
            listOf(provider!!)
        } else {
            listOf(PubKey(getProviderByClientKeys()
                    ?: throw CliktError("Cannot identify provider based on configured keys")))
        }
    }
}
