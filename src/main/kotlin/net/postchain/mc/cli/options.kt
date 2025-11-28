package net.postchain.mc.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.ParameterHolder
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.options.OptionTransformContext
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import com.github.ajalt.mordant.terminal.ConversionResult
import com.github.ajalt.mordant.terminal.prompt
import net.postchain.chain0.cm_api.cmGetSystemAnchoringChain
import net.postchain.chain0.economy_chain_in_directory_chain.getEconomyChainRid
import net.postchain.chain0.token_chain_in_directory_chain.getTokenChainRid
import net.postchain.client.config.PostchainClientConfig
import net.postchain.client.core.PostchainReadClient
import net.postchain.common.BlockchainRid
import net.postchain.common.hexStringToByteArray
import net.postchain.mc.cli.base.HOST_NAME_LENGTH_MAX
import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun CliktCommand.includeInactiveOption(helpMessage: String) = option(
        "-ii", "--includeinactive",
        help = helpMessage
).flag()

enum class SystemBlockchain {
    chain0, economy_chain, token_chain, system_anchoring_chain, directory_chain, system_anchoring;
}

sealed interface BlockchainOption {
    data class Rid(val rid: BlockchainRid) : BlockchainOption
    data class Name(val name: SystemBlockchain) : BlockchainOption
}

fun ParameterHolder.blockchainOption() = mutuallyExclusiveOptions(
        blockchainRidOption().convert { BlockchainOption.Rid(it) },
        blockchainNameOption().convert { BlockchainOption.Name(it) }
)

fun ParameterHolder.blockchainRidOption() =
        option("-brid", "--blockchain-rid", help = "Blockchain RID", envvar = "POSTCHAIN_BRID", metavar = "RID")
                .convert { BlockchainRid.buildFromHex(it) }

fun ParameterHolder.blockchainNameOption() =
        option("-chain", "--blockchain-name", help = "Blockchain name", metavar = "NAME").enum<SystemBlockchain>()

fun CliktCommand.heightOption() = option("--height", envvar = "POSTCHAIN_HEIGHT").long()
fun OptionGroup.heightOption() = option("--height", envvar = "POSTCHAIN_HEIGHT").long()

fun CliktCommand.forceOption() = option("-f", "--force").flag()
        .convert { if (it) AlreadyExistMode.FORCE else AlreadyExistMode.ERROR }

fun CliktCommand.blockchainConfigOption() = option(
        "-bc",
        "--blockchain-config",
        help = "Configuration file of blockchain (GtvML (*.xml) or Gtv (*.gtv))",
        envvar = "POSTCHAIN_BLOCKCHAIN_CONFIG"
).file(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true)

fun CliktCommand.requiredHostOption() = option("--host", help = "Host", envvar = "POSTCHAIN_HOST")
        .required()
        .validate(hostValidator())

fun CliktCommand.hostOption() = option("--host", help = "Host", envvar = "POSTCHAIN_HOST")
        .validate(hostValidator())

fun CliktCommand.defaultHostOption(default: String) = option("--host", help = "Host", envvar = "POSTCHAIN_HOST")
        .default(default)
        .validate(hostValidator())

fun hostValidator(): OptionTransformContext.(String) -> Unit = {
    require(it.length <= HOST_NAME_LENGTH_MAX) { "Host name is too long, maximum allowed length is $HOST_NAME_LENGTH_MAX" }
}

fun CliktCommand.portOption() = option("-p", "--port", help = "Port").int()

fun CliktCommand.dateToTimestampOption(helpMessage: String, default: Long = 0, defaultString: String = "1970-01-01", daysOffset: Long = 0) =
        option(help = helpMessage).convert {
            val date = try {
                LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE).plusDays(daysOffset).atStartOfDay(ZoneOffset.systemDefault())
            } catch (_: Exception) {
                fail("$it is not a date on the valid format YYYY-MM-DD")
            }
            Instant.from(date).toEpochMilli()
        }.default(default, defaultString)

fun CliktCommand.interactiveOption() =
        option("-i", "--interactive", help = "Prompt for item to show details for").flag()


fun CliktCommand.promptForIndex(items: List<*>) =
        currentContext.terminal.prompt("Show details for #") { s ->
            s.toIntOrNull()
                    ?.let { if (it in items.indices) ConversionResult.Valid(it) else ConversionResult.Invalid("No such item: $it") }
                    ?: ConversionResult.Invalid("$s is not a valid integer")
        }

fun ParameterHolder.evmAddressOption(help: String = "EVM address") = option("--evm-address", help = help, metavar = "address")
        .convert {
            (if (it.startsWith("0x")) it.drop(2) else it).hexStringToByteArray()
        }
        .required()
        .validate { require(it.size == 20) { "EVM address must be 20 bytes" } }

fun ParameterHolder.optionalEvmAddressOption(help: String = "EVM address") = option("--evm-address", help = help, metavar = "address")
        .convert {
            (if (it.startsWith("0x")) it.drop(2) else it).hexStringToByteArray()
        }
        .validate { require(it.size == 20) { "EVM address must be 20 bytes" } }

fun ParameterHolder.accountIdOption(help: String = "Account id") = option("--account-id", help = help, metavar = "id")
        .convert {
            it.hexStringToByteArray()
        }
        .validate { require(it.isNotEmpty()) { "Account id must not be empty" } }

fun ParameterHolder.outputFolderOption() = option("--target", help = "Path where transaction file should be saved")
        .file()
        .default(Paths.get("").toAbsolutePath().toFile())

fun resolveBlockchain(dcClientConfig: PostchainClientConfig, dcClient: PostchainReadClient, blockchainOption: BlockchainOption): BlockchainRid = when (blockchainOption) {
    is BlockchainOption.Name -> resolveSystemBlockchain(dcClientConfig, dcClient, blockchainOption.name)
    is BlockchainOption.Rid -> blockchainOption.rid
}

fun resolveSystemBlockchain(dcClientConfig: PostchainClientConfig, dcClient: PostchainReadClient, systemBlockchain: SystemBlockchain): BlockchainRid = when (systemBlockchain) {
    SystemBlockchain.chain0, SystemBlockchain.directory_chain -> dcClientConfig.blockchainRid
    SystemBlockchain.economy_chain -> BlockchainRid(dcClient.getEconomyChainRid()
            ?: throw CliktError("Economy chain is not installed"))

    SystemBlockchain.token_chain -> {
        val brid = dcClient.getTokenChainRid()
        if (brid.isEmpty()) throw CliktError("Token chain is not installed")
        BlockchainRid(brid)
    }

    SystemBlockchain.system_anchoring_chain, SystemBlockchain.system_anchoring -> BlockchainRid(dcClient.cmGetSystemAnchoringChain()
            ?: throw CliktError("System anchoring chain is not installed"))
}
