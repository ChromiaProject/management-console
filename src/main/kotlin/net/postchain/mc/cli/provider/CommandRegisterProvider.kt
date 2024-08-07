package net.postchain.mc.cli.provider

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.cooccurring
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.groups.required
import com.github.ajalt.clikt.parameters.options.OptionTransformContext
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import net.postchain.chain0.common.operations.registerProviderOperation
import net.postchain.chain0.model.ProviderInfo
import net.postchain.chain0.proposal_provider.proposeProviderIsSystemOperation
import net.postchain.chain0.proposal_provider.proposeProviderStateOperation
import net.postchain.chain0.proposal_provider.proposeProvidersOperation
import net.postchain.crypto.PubKey
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.gtv.parse.GtvParser
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.PropertiesConfigurationValueSource
import net.postchain.mc.cli.util.ProviderType
import net.postchain.mc.cli.util.nullableProposalDescriptionOption
import net.postchain.mc.cli.util.optionalPubkeyOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.validateMetadataText
import net.postchain.mc.cli.util.validatePubkey
import net.postchain.mc.cli.util.validateUrl


class BatchOptions : OptionGroup() {

    val batch by option(help = "Allows to add a batch of providers with --provider (see examples)").flag()
    val provider by option(
            help = "Multiple objects to register as providers in --batch mode (comma delimited list of objects, see examples)",
            valueSourceKey = "provider"
    ).convert {
        val pi = GtvParser.parse(it).asDict().toMutableMap()
        pi.putIfAbsent("name", gtv(""))
        pi.putIfAbsent("url", gtv(""))
        GtvObjectMapper.fromGtv(gtv(pi), ProviderInfo::class)
    }.multiple(required = true).validate(validateProviderBatch())

    private fun validateProviderBatch(): OptionTransformContext.(List<ProviderInfo>) -> Unit = { providers ->
        providers.forEach {
            validatePubkey(PubKey(it.pubkey))
            validateMetadataText(it.name)
            if (it.url.isNotBlank()) validateUrl(it.url)
        }
    }
}

class CommandRegisterProvider : CliktCommand(
        name = "register",
        help = """
            Register new provider with given pubkey
            
            There are three tiers of providers:
            ```
            - Dapp Provider:           Basic provider, can deploy dapps and add nodes that replicates blockchains (replica) (default)
            - Node Provider:           Can add block builder nodes
            - System Provider:         System level permissions and can add node to the system cluster
            ```
            
            Examples:
            ```
            (1): pmc provider register -dp --enable --pubkey aa...
            ```
            ```
            (2): pmc provider register --batch -dp --enable --provider '["pubkey": x"aa...", "name": "foo", "url": "http://foo/api"]' --provider '["pubkey": x"bb...", "name": "bar"]'
            ```
            ```
            (3): pmc provider register --batch -dp --enable, where providers will be load from `providers.properties` file:
                    provider=["pubkey": x"aa...", "name": "foo", "url": "http://foo/api"];["pubkey": x"bb...", "name": "bar", "url": "http://bar/api"]
                    provider=["pubkey": x"cc...", "url": "http://foobar/api"]
            ```
    """.trimIndent()
) {
    init {
        context {
            valueSource = PropertiesConfigurationValueSource.from("providers.properties")
        }
    }

    private val config by pmcConfigOption()
    private val client get() = config.client

    private val pubkey by optionalPubkeyOption("Public key to register as provider")

    private val batchOptions by BatchOptions().cooccurring()

    private val providerTier by mutuallyExclusiveOptions(
            option("-dp", help = "dapp provider").flag().convert { ProviderType.DAPP_PROVIDER },
            option("-np", help = "node provider").flag().convert { ProviderType.NODE_PROVIDER },
            option("-sp", help = "system provider").flag().convert { ProviderType.SYSTEM_PROVIDER },
            name = "Provider tier",
    ).required()

    private val enable by mutuallyExclusiveOptions(
            option("--enable", "-e", help = "enable provider").flag().convert { true },
            option("--disable", "-d", help = "disable provider").flag().convert { false },
            name = "Provider state",
    ).required()

    private val description by nullableProposalDescriptionOption()

    private fun description() = description ?: run {
        if (pubkey != null) {
            "Register provider $pubkey - provider-tier: $providerTier, enable: $enable"
        } else if (batchOptions != null) {
            val providers = batchOptions?.provider?.joinToString(", ") { "[${it.pubkey} / ${it.name} / ${it.url}]" }.orEmpty()
            "Register providers - provider-tier: $providerTier, enable: $enable, providers: $providers"
        } else ""
    }

    override fun run() {
        if (batchOptions != null) {
            if (pubkey != null) throw CliktError("use --provider instead of --pubkey in a batch mode")
            client.transactionBuilder()
                    .proposeProvidersOperation(
                            client.pubkey, batchOptions!!.provider, providerTier.toTier(), providerTier.isSystem(), enable, description()
                    )
                    .postAwaitConfirmation()
                    .printResult(
                            "Provider batch has been proposed",
                            "Failed to propose provider batch"
                    )
        } else {
            if (pubkey == null) throw CliktError("--pubkey must be provided")
            client.transactionBuilder()
                    .registerProviderOperation(client.pubkey, pubkey!!, providerTier.toTier())
                    .apply {
                        if (providerTier.shouldEnable(enable)) proposeProviderStateOperation(client.pubkey, pubkey!!.data, enable, description())
                        if (providerTier == ProviderType.SYSTEM_PROVIDER) proposeProviderIsSystemOperation(client.pubkey, pubkey!!.data, true, description())
                    }
                    .postAwaitConfirmation()
                    .printResult(
                            "Provider has been added ${enable.let { if (it) "and proposed for enabling " else "" }}",
                            "Failed to add provider"
                    )
        }
    }
}
