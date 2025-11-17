package net.postchain.mc.cli

import net.postchain.client.core.PostchainClient
import net.postchain.client.transaction.TransactionBuilder
import net.postchain.crypto.PubKey
import net.postchain.mc.cli.economy.getEconomyChainClient
import net.postchain.mc.network.Version
import net.postchain.mc.network.requireApiVersion

abstract class ECBaseCommand(
        name: String,
        help: String,
        private val requiresECVersion: Long? = null,
        override val printHelpOnEmptyArgs: Boolean = true
) : DCBaseCommand(name = name, help = help) {

    protected lateinit var economyChainClient: PostchainClient
    protected lateinit var ecVersion: Version

    override fun runDC() {

        economyChainClient = getEconomyChainClient(config)
        ecVersion = Version(economyChainClient)

        requiresECVersion?.let {
            economyChainClient.requireApiVersion(ecVersion.version, requiresECVersion)
        }

        runEC(client, economyChainClient)
    }

    abstract fun runEC(client: PostchainClient, economyChainClient: PostchainClient)

    override fun transactionBuilder(additionalRequiredSignatures: List<PubKey>): TransactionBuilder {
        val initialSigners = economyChainClient.config.signers
        remainingSigners = ((extraSigners ?: fetchRemainingSignersFromDC()) + additionalRequiredSignatures)
                .filterNot { signer -> initialSigners.any { it.pubKey == signer } }
        return economyChainClient.transactionBuilder(initialSigners, remainingSigners)
                .apply {
                    if (timeb != null) {
                        addTimeBound(0, timeb)
                    }
                }
    }
}