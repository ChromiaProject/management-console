package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.chain0.economy_chain_in_directory_chain.getEconomyChainRid
import net.postchain.client.core.PostchainClient
import net.postchain.common.hexStringToByteArray
import net.postchain.common.toHex
import net.postchain.common.wrap
import net.postchain.crypto.Secp256K1CryptoSystem
import net.postchain.economy.lib.ft4.core.accounts.AuthDescriptor
import net.postchain.economy.lib.ft4.core.accounts.AuthType
import net.postchain.economy.lib.ft4.core.auth.Signature
import net.postchain.economy.lib.ft4.external.accounts.Ft4GetAccountMainAuthDescriptorResult
import net.postchain.economy.lib.ft4.external.accounts.getAccountMainAuthDescriptor
import net.postchain.economy.lib.ft4.external.accounts.updateMainAuthDescriptorOperation
import net.postchain.economy.lib.ft4.external.auth.evmSignaturesOperation
import net.postchain.economy.lib.ft4.external.auth.ftAuthOperation
import net.postchain.economy.lib.ft4.external.auth.getAuthMessageTemplate
import net.postchain.economy.lib.hbridge.linkEvmEoaAccountOperation
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.GtvNull
import net.postchain.gtv.merkle.GtvMerkleHashCalculator
import net.postchain.gtv.merkleHash
import net.postchain.mc.cli.base.printResult
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys
import org.web3j.crypto.Sign
import java.math.BigInteger
import java.nio.charset.StandardCharsets

class CommandAuthDescriptorEvmSwap : ECBaseCommand(
        name = "auth-descriptor-evm-swap",
        help = "This command will swap the main auth descriptor signer of an ft4 provider account with the EVM address based on the " +
                "provided metamask private key and set the auth flags to both T (Transfer) and A (Account). " +
                "It will also link the account to an EOA (external owned account) using the EVM address"
) {

    private val accountId by option("--account-id", help = "Account id of the account to be updated.")
            .convert { it.hexStringToByteArray() }.required().validate { require(it.isNotEmpty()) { "Account id not provided." } }

    private val metamaskPrivateKeyFile by option("--metamask-private-key-file", help = "Metamask private key file.")
            .file(mustExist = true, mustBeReadable = true, canBeDir = false)
            .required()

    private val hashCalculator = GtvMerkleHashCalculator(Secp256K1CryptoSystem())

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {
        val metamaskPrivateKey = try {
            metamaskPrivateKeyFile.readText().trim()
        } catch (e: Exception) {
            throw CliktError("Metamask private key not provided.")
        }

        val accountMainAuthDescriptor = economyChainClient.getAccountMainAuthDescriptor(accountId)
        val evmKeyPair = ECKeyPair.create(BigInteger(metamaskPrivateKey, 16))
        val addressString = Keys.getAddress(evmKeyPair.publicKey)
        val addressByteArray = addressString.hexStringToByteArray()

        val ecRid = client.getEconomyChainRid()!!
        val updateMainAuthDescriptorSignature = getUpdateMainAuthDescriptorSignature(addressByteArray, economyChainClient, accountMainAuthDescriptor, evmKeyPair, ecRid)
        val linkEvmEoaAccountSignature = getLinkEvmEoaAccountSignature(addressByteArray, economyChainClient, evmKeyPair, ecRid)

        economyChainClient.transactionBuilder()
                .evmSignaturesOperation(listOf(addressByteArray), listOf(linkEvmEoaAccountSignature))
                .ftAuthOperation(accountId, accountMainAuthDescriptor.id.data)
                .linkEvmEoaAccountOperation(addressByteArray)
                .evmSignaturesOperation(listOf(addressByteArray), listOf(updateMainAuthDescriptorSignature))
                .ftAuthOperation(accountId, accountMainAuthDescriptor.id.data)
                .updateMainAuthDescriptorOperation(AuthDescriptor(AuthType.S, listOf(gtv(gtv("A"), gtv("T")), gtv(addressByteArray)), GtvNull))
                .postAwaitConfirmation()
                .printResult(
                        "Link EVM account to EOA account and update auth description signer with evm address: $addressString",
                        "Failed to link and update auth descriptor signer to evm address. "
                )
    }

    private fun getLinkEvmEoaAccountSignature(addressByteArray: ByteArray, economyChainClient: PostchainClient, evmKeyPair: ECKeyPair, ecRid: ByteArray): Signature {
        val opName = "eif.hbridge.link_evm_eoa_account"
        val opArgs = gtv(listOf(gtv(addressByteArray)))

        val nonce = gtv(listOf(
                gtv(ecRid),
                gtv(opName),
                opArgs,
                gtv(0),
        )).merkleHash(hashCalculator)

        val template = economyChainClient.getAuthMessageTemplate(opName, opArgs)
        val message = template.replace("{blockchain_rid}", ecRid.toHex().uppercase())
                .replace("{nonce}", nonce.toHex().uppercase())

        val evmSig = Sign.signPrefixedMessage(
                message.toByteArray(StandardCharsets.UTF_8),
                evmKeyPair
        )
        val signature = Signature(
                evmSig.r.wrap(),
                evmSig.s.wrap(),
                BigInteger(evmSig.v).longValueExact()
        )
        return signature
    }

    private fun getUpdateMainAuthDescriptorSignature(addressByteArray: ByteArray, economyChainClient: PostchainClient, accountMainAuthDescriptor: Ft4GetAccountMainAuthDescriptorResult, keyPair: ECKeyPair, ecRid: ByteArray): Signature {
        val updateMainAuthDescriptorOpName = "ft4.update_main_auth_descriptor"
        val authDescriptorArgs = gtv(gtv(gtv("A"), gtv("T")), gtv(addressByteArray))
        val authDescriptor = gtv(
                gtv(AuthType.S.ordinal.toLong()),
                authDescriptorArgs,
                GtvNull)
        val updateMainAuthDescriptorOpArgs = listOf(authDescriptor)
        val authMessageTemplate = economyChainClient.getAuthMessageTemplate(updateMainAuthDescriptorOpName, gtv(updateMainAuthDescriptorOpArgs))

        val nonce = gtv(
                gtv(ecRid),
                gtv(updateMainAuthDescriptorOpName),
                gtv(updateMainAuthDescriptorOpArgs),
                gtv(0),
        ).merkleHash(hashCalculator)
        val accountId = accountMainAuthDescriptor.accountId

        val authMessage = authMessageTemplate
                .replace("{blockchain_rid}", ecRid.toHex())
                .replace("{nonce}", nonce.toHex().uppercase())
                .replace("{account_id}", accountId.toHex().uppercase())

        val signatureData = Sign.signPrefixedMessage(
                authMessage.toByteArray(StandardCharsets.UTF_8),
                keyPair
        )
        val signature = Signature(
                signatureData.r.wrap(),
                signatureData.s.wrap(),
                BigInteger(signatureData.v).longValueExact()
        )
        return signature
    }
}