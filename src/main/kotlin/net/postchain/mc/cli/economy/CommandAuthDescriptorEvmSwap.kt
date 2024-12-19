package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.CoreCliktCommand
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.mordant.rendering.TextStyles.Companion.hyperlink
import com.google.gson.Gson
import com.google.gson.JsonArray
import net.postchain.client.core.PostchainClient
import net.postchain.common.hexStringToByteArray
import net.postchain.common.toHex
import net.postchain.common.wrap
import net.postchain.crypto.sha256Digest
import net.postchain.economy.economy_chain.getProviderAccountId
import net.postchain.economy.lib.ft4.core.accounts.AuthDescriptor
import net.postchain.economy.lib.ft4.core.accounts.AuthType
import net.postchain.economy.lib.ft4.core.auth.Signature
import net.postchain.economy.lib.ft4.external.accounts.UPDATE_MAIN_AUTH_DESCRIPTOR
import net.postchain.economy.lib.ft4.external.accounts.getAccountMainAuthDescriptor
import net.postchain.economy.lib.ft4.external.accounts.getAuthDescriptorCounter
import net.postchain.economy.lib.ft4.external.accounts.updateMainAuthDescriptorOperation
import net.postchain.economy.lib.ft4.external.auth.evmSignaturesOperation
import net.postchain.economy.lib.ft4.external.auth.ftAuthOperation
import net.postchain.economy.lib.ft4.external.auth.getAuthMessageTemplate
import net.postchain.economy.lib.hbridge.LINK_EVM_EOA_ACCOUNT
import net.postchain.economy.lib.hbridge.linkEvmEoaAccountOperation
import net.postchain.gtv.Gtv
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.GtvNull
import net.postchain.gtv.merkle.GtvMerkleHashCalculator
import net.postchain.gtv.merkleHash
import net.postchain.mc.cli.base.printResult
import org.apache.commons.text.StringEscapeUtils
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.webJars
import org.http4k.server.Netty
import org.http4k.server.ServerConfig.StopMode.Immediate
import org.http4k.server.asServer
import java.awt.Desktop
import java.io.IOException
import java.net.URI
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

class CommandAuthDescriptorEvmSwap : ECBaseCommand(
        name = "auth-descriptor-evm-swap",
        help = "This command will swap the main auth descriptor signer of an ft4 provider account with the provided " +
                "EVM address and set the auth flags to both T (Transfer) and A (Account). " +
                "It will also link the account to an EOA (external owned account) using the EVM address"
) {

    val accountIdOption by option("--account-id", help = "Account id of the account to be updated.")
            .convert { it.hexStringToByteArray() }.validate { require(it.isNotEmpty()) { "Account id cannot be empty." } }

    val evmAddress by option(help = "EVM address", metavar = "address")
            .convert {
                (if (it.startsWith("0x")) it.drop(2) else it).hexStringToByteArray()
            }
            .required()
            .validate { require(it.size == 20) { "EVM address must be 20 bytes" } }

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {
        val providerPubkey = economyChainClient.config.signers.firstOrNull()?.pubKey?.data
                ?: throw CliktError("No provider")
        val accountId = accountIdOption
                ?: economyChainClient.getProviderAccountId(providerPubkey).also { if (it != null) echo("Using account id ${it.toHex()}") }
                ?: throw CliktError("No account id found for provider")

        val accountMainAuthDescriptor = economyChainClient.getAccountMainAuthDescriptor(accountId)

        val authDescriptor = gtv(
                gtv(AuthType.S.ordinal.toLong()),
                gtv(gtv(gtv("A"), gtv("T")), gtv(evmAddress)),
                GtvNull)
        val (linkEvmEoaAccountSignature, updateMainAuthDescriptorSignature) = fetchEvmSignatures(
                economyChainClient,
                listOf(
                        LINK_EVM_EOA_ACCOUNT to listOf(gtv(evmAddress)),
                        UPDATE_MAIN_AUTH_DESCRIPTOR to listOf(authDescriptor)
                ),
                evmAddress, accountId, accountMainAuthDescriptor.id.data)

        economyChainClient.transactionBuilder()
                .evmSignaturesOperation(listOf(evmAddress), listOf(linkEvmEoaAccountSignature))
                .ftAuthOperation(accountId, accountMainAuthDescriptor.id.data)
                .linkEvmEoaAccountOperation(evmAddress)
                .evmSignaturesOperation(listOf(evmAddress), listOf(updateMainAuthDescriptorSignature))
                .ftAuthOperation(accountId, accountMainAuthDescriptor.id.data)
                .updateMainAuthDescriptorOperation(AuthDescriptor(AuthType.S, listOf(gtv(gtv("A"), gtv("T")), gtv(evmAddress)), GtvNull))
                .postAwaitConfirmation()
                .printResult(
                        "Link EVM account to EOA account and update auth description signer with EVM address: 0x${evmAddress.toHex()}",
                        "Failed to link and update auth descriptor signer to EVM address. "
                )
    }
}


// TODO move to chromia-cli-tools

fun CoreCliktCommand.fetchEvmSignatures(client: PostchainClient,
                                        operations: List<Pair<String, List<Gtv>>>,
                                        evmAddress: ByteArray,
                                        accountId: ByteArray, authDescriptorId: ByteArray,
                                        launchWebBrowser: Boolean = true, urlNotifier: (String) -> Unit = {}): List<Signature> {
    val authMessages = operations.map { (opName, opArgs) ->
        val authMessageTemplate = client.getAuthMessageTemplate(opName, gtv(opArgs))
        val counter = client.getAuthDescriptorCounter(accountId, authDescriptorId)
        if (counter == null) throw CliktError("Invalid auth descriptor counter. Was the auth descriptor too close to expiration?")
        val nonce = gtv(listOf(
                gtv(client.config.blockchainRid),
                gtv(opName),
                gtv(opArgs),
                gtv(counter),
        )).merkleHash(GtvMerkleHashCalculator(::sha256Digest))
        authMessageTemplate
                .replace("{blockchain_rid}", client.config.blockchainRid.toHex().uppercase())
                .replace("{nonce}", nonce.toHex().uppercase())
                .replace("{account_id}", accountId.toHex().uppercase())
                .replace("{auth_descriptor_id}", authDescriptorId.toHex().uppercase())
    }

    val html = this::class.java.getResource("/com/chromia/cli/evm_auth/index.html")!!.readText()
            .replace("{{address}}", "0x${evmAddress.toHex()}")
            .replace("{{messages}}", authMessages.joinToString(separator = "") { "\"${StringEscapeUtils.escapeEcmaScript(it)}\",\n" })
    val signaturesFuture = CompletableFuture<String>()
    val server = routes(
            "/" bind GET to { Response(OK).header("Content-Type", "text/html").body(html) },
            "/signatures" bind POST to { request ->
                signaturesFuture.complete(request.bodyString())
                Response(OK)
            },
            "/error" bind POST to { request ->
                signaturesFuture.completeExceptionally(CliktError(request.bodyString()))
                Response(OK)
            },
            webJars()
    ).asServer(Netty(port = 0, stopMode = Immediate)).start()
    val url = "http://localhost:${server.port()}"
    if (launchWebBrowser) {
        openWebLink(url)
    }
    urlNotifier(url)
    val rawSignatures = try {
        signaturesFuture.get()
    } catch (e: ExecutionException) {
        throw (e.cause ?: e)
    } finally {
        server.stop()
    }
    val signatures = Gson().fromJson(rawSignatures, JsonArray::class.java)
    return signatures.asList().map {
        Signature(
                r = it.asJsonObject.get("r").asString.drop(2).hexStringToByteArray().wrap(),
                s = it.asJsonObject.get("s").asString.drop(2).hexStringToByteArray().wrap(),
                v = it.asJsonObject.get("v").asLong)
    }
}

fun CoreCliktCommand.openWebLink(url: String) {
    val os = System.getProperty("os.name")
    try {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(URI(url))
        } else if (os.contains("mac")) {
            Runtime.getRuntime().exec(arrayOf("open", url))
        } else if (os.contains("nix") || os.contains("nux")) {
            Runtime.getRuntime().exec(arrayOf("xdg-open", url))
        } else {
            terminalWebLink(url)
        }
    } catch (_: IOException) {
        terminalWebLink(url)
    }
}

fun CoreCliktCommand.terminalWebLink(url: String) {
    echo("Open ${hyperlink(url)(url)} in your web browser to continue")
}
