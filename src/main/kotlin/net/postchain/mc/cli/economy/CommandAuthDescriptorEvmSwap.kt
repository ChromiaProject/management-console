package net.postchain.mc.cli.economy

import com.chromia.cli.tools.ft.openWebLink
import com.chromia.directory1.lib.ft4.core.auth.Signature
import com.chromia.directory1.lib.ft4.external.auth.evmSignaturesOperation
import com.chromia.directory1.lib.ft4.external.auth.ftAuthOperation
import com.chromia.directory1.lib.ft4.external.auth.getAuthMessageTemplate
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.CoreCliktCommand
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
import net.postchain.economy.lib.ft4.external.accounts.UPDATE_MAIN_AUTH_DESCRIPTOR
import net.postchain.economy.lib.ft4.external.accounts.getAccountMainAuthDescriptor
import net.postchain.economy.lib.ft4.external.accounts.updateMainAuthDescriptorOperation
import net.postchain.economy.lib.hbridge.LINK_EVM_EOA_ACCOUNT
import net.postchain.economy.lib.hbridge.linkEvmEoaAccountOperation
import net.postchain.gtv.Gtv
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.GtvNull
import net.postchain.gtv.merkle.GtvMerkleHashCalculatorV1
import net.postchain.gtv.merkleHash
import net.postchain.mc.cli.ECBaseCommand
import net.postchain.mc.cli.accountIdOption
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.evmAddressOption
import org.apache.commons.text.StringEscapeUtils
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.webJars
import org.http4k.server.Netty
import org.http4k.server.asServer
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

class CommandAuthDescriptorEvmSwap : ECBaseCommand(
        name = "auth-descriptor-evm-swap",
        help = "This command will swap the main auth descriptor signer of an ft4 provider account with the provided " +
                "EVM address and set the auth flags to both T (Transfer) and A (Account). " +
                "It will also link the account to an EOA (external owned account) using the EVM address"
) {

    val accountIdOption by accountIdOption(help = "Account id of the account to be updated.")

    val evmAddress by evmAddressOption()

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
        val (linkEvmEoaAccountSignature, updateMainAuthDescriptorSignature) = fetchEvmSignaturesForEvmSignaturesOperation(
                economyChainClient,
                listOf(
                        LINK_EVM_EOA_ACCOUNT to listOf(gtv(evmAddress)),
                        UPDATE_MAIN_AUTH_DESCRIPTOR to listOf(authDescriptor)
                ),
                evmAddress, accountId, accountMainAuthDescriptor.id.data)
        echo("Signing done, posting transaction...")
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

// TODO add this to chromia-cli-tools
fun CoreCliktCommand.fetchEvmSignaturesForEvmSignaturesOperation(client: PostchainClient,
                                                                 operations: List<Pair<String, List<Gtv>>>,
                                                                 evmAddress: ByteArray,
                                                                 accountId: ByteArray, authDescriptorId: ByteArray,
                                                                 launchWebBrowser: Boolean = true, urlNotifier: (String) -> Unit = {}): List<Signature> {
    val authMessages = operations.map { (opName, opArgs) ->
        val authMessageTemplate = client.getAuthMessageTemplate(opName, gtv(opArgs))
        // TODO [use-new-algo] use new hash version here
        val nonce = gtv(listOf(
                gtv(client.config.blockchainRid),
                gtv(opName),
                gtv(opArgs),
                gtv(0),
        )).merkleHash(GtvMerkleHashCalculatorV1(::sha256Digest))
        authMessageTemplate
                .replace("{blockchain_rid}", client.config.blockchainRid.toHex().uppercase())
                .replace("{nonce}", nonce.toHex().uppercase())
                .replace("{account_id}", accountId.toHex().uppercase())
                .replace("{auth_descriptor_id}", authDescriptorId.toHex().uppercase())
    }

    val html = this::class.java.getResource("/com/chromia/cli/tools/evm_auth/index.html")!!.readText()
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
    ).asServer(Netty(port = 0)).start()
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
