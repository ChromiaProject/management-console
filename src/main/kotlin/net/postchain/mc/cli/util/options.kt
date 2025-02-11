package net.postchain.mc.cli.util

import com.chromia.build.tools.config.ChromiaConfigLoader
import com.chromia.build.tools.config.ChromiaConfigWriter
import com.chromia.cli.tools.config.OptionalChromiaModelConfigOption
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.groups.required
import com.github.ajalt.clikt.parameters.groups.single
import com.github.ajalt.clikt.parameters.options.OptionTransformContext
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.options.switch
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.long
import com.github.ajalt.clikt.parameters.types.path
import net.postchain.chain0.model.ProviderQuotaType
import net.postchain.client.config.PostchainClientConfig
import net.postchain.client.request.Endpoint
import net.postchain.common.BlockchainRid
import net.postchain.common.config.getEnvOrBooleanProperty
import net.postchain.common.config.getEnvOrStringProperty
import net.postchain.common.hexStringToByteArray
import net.postchain.crypto.PubKey
import net.postchain.d1.client.StandardChromiaClient
import net.postchain.mc.cli.base.CommandBase
import net.postchain.mc.cli.base.DIGEST_LENGTH_MAX
import net.postchain.mc.cli.base.METADATA_LENGTH_MAX
import net.postchain.mc.cli.base.NAME_LENGTH_MAX
import net.postchain.mc.cli.base.URL_LENGTH_MAX
import java.net.URI
import java.net.URISyntaxException

const val CHROMIA_CONFIG = "CHROMIA_CONFIG"
const val ECDSA_COMPRESSED_KEY_SIZE = 33
const val ECDSA_UNCOMPRESSED_KEY_SIZE = 65
const val DILITHIUM2_KEY_SIZE = 1336

fun CliktCommand.pubkeyOption(helpMsg: String = "Public key") = option("-pk", "--pubkey", help = helpMsg, envvar = "POSTCHAIN_PUBKEY")
        .convert { PubKey(it) }
        .required()
        .validate(pubkeyValidator())

fun CliktCommand.optionalPubkeyOption(helpMsg: String = "Public key") = option("-pk", "--pubkey", help = helpMsg, envvar = "POSTCHAIN_PUBKEY")
        .convert { PubKey(it) }
        .validate(pubkeyValidator())

fun CliktCommand.pubkeysOption(helpMsg: String = "Comma delimited list of public keys") = option("--pubkeys", help = helpMsg)
        .convert { PubKey(it) }.split(",")
        .required()
        .validate(pubkeysValidator())

fun pubkeyValidator(): OptionTransformContext.(PubKey) -> Unit = {
    validatePubkey(it)
}

fun pubkeysValidator(): OptionTransformContext.(List<PubKey>) -> Unit = {
    it.forEach(::validatePubkey)
}

fun OptionTransformContext.validatePubkey(pubKey: PubKey) {
    val keySize = pubKey.data.size
    require(keySize == ECDSA_COMPRESSED_KEY_SIZE || keySize == ECDSA_UNCOMPRESSED_KEY_SIZE || keySize == DILITHIUM2_KEY_SIZE) {
        "Size of public key $pubKey is not valid, must be $ECDSA_COMPRESSED_KEY_SIZE, $ECDSA_UNCOMPRESSED_KEY_SIZE or $DILITHIUM2_KEY_SIZE"
    }
}

fun CliktCommand.pmcConfigOption() = PmcClientConfigOption { msg -> echo(msg, err = true) }

class PmcClientConfigOption(logger: (String) -> Unit) : OptionalChromiaModelConfigOption(logger) {
    private val lookupBrid by option("--lookup-brid", help = "Ignore any 'brid' property in configuration file, always perform lookup")
            .flag()
    val lookupNodes by option("--lookup-nodes", help = "Lookup system cluster signer nodes for sending transactions to")
            .flag("--no-lookup-nodes", default = true, defaultForHelp = "yes")
    val network by option("--network", help = "Target network to make requests to (if chromia.yml is configured)")
    val rawConfig by lazy { ChromiaConfigLoader(logger).loadProperties(configFile) }

    val chromiaClient by lazy {
        if (network != null) {
            requireNotNull(model) { "chromia.yml not found" }
            val networkModel = model!!.deployments[network]
                    ?: throw IllegalArgumentException("Network $network not found in configuration")
            rawConfig.setProperty("api.url", networkModel.urls.joinToString(",") { Endpoint.sanitizeUrl(it) })
            rawConfig.setProperty("brid", networkModel.blockchainRid.toHex())
        }
        val configuredBrid = if (lookupBrid) null else rawConfig.getEnvOrStringProperty("POSTCHAIN_CLIENT_BLOCKCHAIN_RID", "brid")
        val useRequestCompression = rawConfig
                .getEnvOrBooleanProperty("POSTCHAIN_CLIENT_COMPRESS_REQUEST_BODIES", "compress.requests", true)

        if (!rawConfig.containsKey("api.url")) throw CliktError("No api.url specified")
        rawConfig.setProperty("brid", configuredBrid ?: BlockchainRid.ZERO_RID.toHex())
        val postchainClientConfig = PostchainClientConfig.fromConfiguration(rawConfig)

        val chromiaClient = StandardChromiaClient(postchainClientConfig.copy(compressRequestBodies = useRequestCompression))

        if (configuredBrid == null) {
            ChromiaConfigWriter.local.setBrid(chromiaClient.directoryChainRid)
        }

        chromiaClient
    }

    val client by lazy {
        if (lookupNodes)
            chromiaClient.getDirectoryChainClientForQueryReplica(addNop = true)
        else
            chromiaClient.getDirectoryChainClientForForwardingReplica(addNop = true)
    }

    val providerPubkey by lazy { rawConfig.getEnvOrStringProperty("POSTCHAIN_CLIENT_PROVIDER_PUBKEY", "provider.pubkey") }
}

fun CliktCommand.nameOption(helpMessage: String) = option("-n", "--name", help = helpMessage)

fun CliktCommand.nameOrGenerateOption(helpMessage: String) = mutuallyExclusiveOptions(
        nameOption(helpMessage).validate(entityNameValidator()),
        option(
                "-a", "--auto-generate-name",
                help = "Set if ${helpMessage.lowercase()} should be autogenerated (suppressed by -n)"
        ).flag().convert {
            if (it) {
                CommandBase.autoGenerateName()
            } else null
        }
).single().required()

fun entityNameValidator(): OptionTransformContext.(String) -> Unit = {
    require(CommandBase.isEntityNameValid(it)) { "Entity name can only contain letters, numerals, underscores and start with either a letter or numeral. Maximum allowed length is 64 characters." }
    require(it.length <= NAME_LENGTH_MAX) { "Name is too long, maximum allowed length is $NAME_LENGTH_MAX" }
    require(it.isNotEmpty()) { "Name cannot be empty" }
}

fun CliktCommand.requiredUrlOption(helpMessage: String, vararg names: String = arrayOf("--url")) = option(*names, help = helpMessage)
        .required()
        .validate(urlValidator())

fun CliktCommand.urlOption(helpMessage: String, vararg names: String = arrayOf("--url")) = option(*names, help = helpMessage)
        .validate(urlValidator())

fun CliktCommand.defaultUrlOption(default: String, helpMessage: String, vararg names: String = arrayOf("--url")) = option(*names, help = helpMessage)
        .default(default)
        .validate(urlValidator())

fun urlValidator(): OptionTransformContext.(String) -> Unit = {
    validateUrl(it)
}

fun OptionTransformContext.validateUrl(url: String) {
    val valid = try {
        URI(url)
        true
    } catch (_: URISyntaxException) {
        false
    }
    require(valid) { "Invalid URL provided: $url" }
    require(url.length <= URL_LENGTH_MAX) { "URL is too long, maximum allowed length is $URL_LENGTH_MAX" }
}

fun metadataTextValidator(): OptionTransformContext.(String) -> Unit = {
    validateMetadataText(it)
}

fun OptionTransformContext.validateMetadataText(text: String) {
    require(text.length <= METADATA_LENGTH_MAX) { "value is too long, maximum allowed length is $METADATA_LENGTH_MAX" }
}

fun digestValidator(): OptionTransformContext.(String) -> Unit = {
    require(CommandBase.isDigestValid(it)) { "Digest name can only contain letters, numerals, and colon. Maximum allowed length is 100 characters." }
    require(it.length <= DIGEST_LENGTH_MAX) { "Digest is too long, maximum allowed length is $DIGEST_LENGTH_MAX" }
    require(it.isNotEmpty()) { "Digest cannot be empty" }
}

sealed class VoterSetOrPubkeysOption(val data: String) {
    class Pubkeys(data: String) : VoterSetOrPubkeysOption(data) {
        val pubkeys get() = data.split(",").map { it.hexStringToByteArray() }
    }

    class VoterSet(data: String) : VoterSetOrPubkeysOption(data)

}

fun CliktCommand.pubkeysOrVotersetOption() = mutuallyExclusiveOptions(
        option("--voter-set", help = "Name of voter set").convert { VoterSetOrPubkeysOption.VoterSet(it) },
        option("--pubkeys", help = "Comma delimited list of public keys").convert { VoterSetOrPubkeysOption.Pubkeys(it) }
).single().required()

fun CliktCommand.maxBlockchainsOption() = option("-mb", "--max-blockchains", help = "Max number of blockchains per container").long()
fun CliktCommand.containerUnitsOption() = option("-cou", "--container-units", help = "Container Units (minimum 1)").long()
fun CliktCommand.clusterUnitsOption() = option("-clu", "--cluster-units", help = "Cluster Units (minimum 1)").long()
fun CliktCommand.extraStorageOption() = option("-es", "--extra-storage", help = "Extra Storage (MiB)").long()

fun CliktCommand.providerTierOption() = option(help = "Provider tier").switch(
        "-dp" to ProviderType.DAPP_PROVIDER,
        "-np" to ProviderType.NODE_PROVIDER,
        "-sp" to ProviderType.SYSTEM_PROVIDER
)

fun CliktCommand.providerQuotaTypeOption() = option(help = "Provider quota type").switch(
        "-ma" to ProviderQuotaType.max_actions_per_day,
        "-mc" to ProviderQuotaType.max_containers,
        "-mn" to ProviderQuotaType.max_nodes
)

fun CliktCommand.nullableProposalDescriptionOption(helpMessage: String = "Proposal description") = option("--description", help = helpMessage)
        .validate(metadataTextValidator())

fun CliktCommand.proposalDescriptionOption(helpMessage: String = "Proposal description", default: () -> String = { "" }) = option("--description", help = helpMessage)
        .defaultLazy(value = default)
        .validate(metadataTextValidator())

fun CliktCommand.configurationsFileOption() = option("--configurations-file", help = "File to import blockchain configurations from")
        .path(mustExist = true, canBeDir = false, canBeFile = true, mustBeReadable = true)

fun CliktCommand.thresholdOption() = option(
        "-t", "--threshold",
        help = """
        0: supermajority of voters, specifically  `n - (n - 1) / 3` (which is usually around 67%)
        -1: simple majority
        positive number: that many voters
    """.trimIndent()
).long()
