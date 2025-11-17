package net.postchain.mc.cli

import com.chromia.cli.tools.util.signersOption
import com.chromia.cli.tools.util.timebOptions
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.input.interactiveMultiSelectList
import net.postchain.chain0.cm_api.cmGetSystemAnchoringChain
import net.postchain.chain0.common.queries.getProviderByKey
import net.postchain.chain0.common.queries.getProviderKeysAndThreshold
import net.postchain.chain0.economy_chain_in_directory_chain.getEconomyChainRid
import net.postchain.chain0.token_chain_in_directory_chain.getTokenChainRid
import net.postchain.client.transaction.TransactionBuilder
import net.postchain.common.BlockchainRid
import net.postchain.common.toHex
import net.postchain.crypto.PubKey
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.pmcKeyConfigOption
import net.postchain.mc.network.Version
import net.postchain.mc.network.requireApiVersion
import java.time.Clock

/**
 * Use this for commands which makes transactions, or otherwise need access to keys.
 * Commands which only makes queries should inherit from `PmcCommand` instead.
 */
abstract class DCBaseCommand(
        name: String,
        help: String,
        private val requiresVersion: Long? = null,
        override val printHelpOnEmptyArgs: Boolean = true
) : PmcCommand(name = name, help = help) {

    val config by pmcKeyConfigOption()
    val client get() = config.txClient
    val dcVersion get() = Version(client).version

    protected val timeb by timebOptions(Clock.systemUTC())

    protected val extraSigners by signersOption()

    protected val alwaysSaveTx by option("--save-tx", help = "Always save transaction to file, even if it is fully signed").flag()

    protected val outputFolder by outputFolderOption()

    // Get provider pubkey from (1) config, (2) by looking up based on signer keys or (3) use default/first signer key
    val clientProviderPubkey by lazy {
        config.providerPubkey?.data
                ?: getProviderByClientKeys()
                ?: client.pubkey
    }

    override fun run() {
        requiresVersion?.apply {
            client.requireApiVersion(dcVersion, requiresVersion)
        }

        runDC()
    }

    lateinit var remainingSigners: List<PubKey>

    abstract fun runDC()

    open fun transactionBuilder(additionalRequiredSignatures: List<PubKey> = listOf()): TransactionBuilder {
        val initialSigners = client.config.signers
        remainingSigners = ((extraSigners ?: fetchRemainingSignersFromDC()) + additionalRequiredSignatures)
                .filterNot { signer -> initialSigners.any { it.pubKey == signer } }
        return client.transactionBuilder(initialSigners, remainingSigners)
                .apply {
                    if (timeb != null) {
                        addTimeBound(0, timeb)
                    }
                }
    }

    fun fetchRemainingSignersFromDC(): List<PubKey> = if (dcVersion >= DIRECTORY_CHAIN_PROVIDER_MULTI_KEY_VERSION) {
        val (keys, threshold) = client.getProviderKeysAndThreshold(PubKey(clientProviderPubkey))
        val eligibleKeys = keys.map { PubKey(it) }
        val possessedKeys = client.config.signers.map { it.pubKey }.toSet()
        val validKeys = eligibleKeys.intersect(possessedKeys)
        val missingKeys = eligibleKeys.subtract(possessedKeys)
        if (validKeys.size < threshold) {
            if (threshold < eligibleKeys.size) {
                if (terminal.terminalInfo.interactive) {
                    val neededAdditionalKeys = (threshold - possessedKeys.size).toInt()
                    val selectedKeys = terminal.interactiveMultiSelectList {
                        title("Select $neededAdditionalKeys additional keys to sign transaction with")
                        entries(missingKeys.map { it.data.toHex() })
                        limit(neededAdditionalKeys)
                    }?.map { PubKey(it) } ?: listOf()
                    if (selectedKeys.size < neededAdditionalKeys) {
                        throw CliktError("You need to select $neededAdditionalKeys keys, only ${selectedKeys.size} was selected")
                    } else {
                        selectedKeys
                    }
                } else {
                    throw CliktError("Transaction needs to be signed by $threshold keys of $eligibleKeys, please specify which keys to use with --signers option")
                }
            } else {
                missingKeys.toList()
            }
        } else {
            listOf()
        }
    } else {
        listOf()
    }

    fun getProviderByClientKeys(): ByteArray? {
        if (dcVersion >= DIRECTORY_CHAIN_PROVIDER_MULTI_KEY_VERSION) {
            config.config.signers.forEach {
                client.getProviderByKey(it.pubKey)?.let { provider ->
                    return provider
                }
            }
        }
        return null
    }

    fun resolveBlockchainRid(systemBlockchain: SystemBlockchain): BlockchainRid = when (systemBlockchain) {
        SystemBlockchain.chain0 -> client.config.blockchainRid
        SystemBlockchain.economy_chain -> BlockchainRid(client.getEconomyChainRid()
                ?: throw CliktError("Economy chain is not installed"))

        SystemBlockchain.token_chain -> {
            val brid = client.getTokenChainRid()
            if (brid.isEmpty()) throw CliktError("Token chain is not installed")
            BlockchainRid(brid)
        }

        SystemBlockchain.system_anchoring_chain -> BlockchainRid(client.cmGetSystemAnchoringChain()
                ?: throw CliktError("System anchoring chain is not installed"))
    }

    // Helper to either post and await confirmation or save to file when extra signers are used
    fun TransactionBuilder.postOrSave() =
            if (alwaysSaveTx || remainingSigners.isNotEmpty()) {
                val gtx = finish().apply {
                    client.config.signers.forEach { sign(it.sigMaker(client.config.cryptoSystem)) }
                }.buildGtx()
                val txRid = gtx.calculateTxRid(client.merkleHashCalculator)
                saveTransaction(outputFolder, txRid, gtx)
                throw ProgramResult(0)
            } else {
                this.postAwaitConfirmation(txListener())
            }
}
