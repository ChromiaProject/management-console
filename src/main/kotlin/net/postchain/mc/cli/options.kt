package net.postchain.mc.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ParameterHolder
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.OptionTransformContext
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import com.github.ajalt.mordant.terminal.ConversionResult
import com.github.ajalt.mordant.terminal.prompt
import net.postchain.common.BlockchainRid
import net.postchain.common.hexStringToByteArray
import net.postchain.mc.cli.base.HOST_NAME_LENGTH_MAX
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun CliktCommand.includeInactiveOption(helpMessage: String) = option(
        "-ii", "--includeinactive",
        help = helpMessage
).flag()

fun CliktCommand.blockchainRidOption() =
        option("-brid", "--blockchain-rid", help = "Blockchain RID", envvar = "POSTCHAIN_BRID")
                .convert { BlockchainRid.buildFromHex(it) }

fun OptionGroup.blockchainRidOption() =
        option("-brid", "--blockchain-rid", help = "Blockchain RID", envvar = "POSTCHAIN_BRID")
                .convert { BlockchainRid.buildFromHex(it) }

fun CliktCommand.heightOption() = option("-h", "--height", envvar = "POSTCHAIN_HEIGHT").long()
fun OptionGroup.heightOption() = option("-h", "--height", envvar = "POSTCHAIN_HEIGHT").long()

fun CliktCommand.forceOption() = option("-f", "--force").flag()
        .convert { if (it) AlreadyExistMode.FORCE else AlreadyExistMode.ERROR }

fun CliktCommand.blockchainConfigOption() = option(
        "-bc",
        "--blockchain-config",
        help = "Configuration file of blockchain (GtvML (*.xml) or Gtv (*.gtv))",
        envvar = "POSTCHAIN_BLOCKCHAIN_CONFIG"
).file(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true)

fun CliktCommand.requiredHostOption() = option("-h", "--host", help = "Host", envvar = "POSTCHAIN_HOST")
        .required()
        .validate(hostValidator())

fun CliktCommand.hostOption() = option("-h", "--host", help = "Host", envvar = "POSTCHAIN_HOST")
        .validate(hostValidator())

fun CliktCommand.defaultHostOption(default: String) = option("-h", "--host", help = "Host", envvar = "POSTCHAIN_HOST")
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
