package net.postchain.mc.cli.base

import com.github.ajalt.clikt.core.CliktError
import net.postchain.client.config.PostchainClientConfig
import net.postchain.client.core.PostchainClient


val PostchainClient.pubkey: ByteArray
    get() {
        if (!hasSigners()) throw CliktError("No keypair specified in configuration")
        return config.pubkey().data
    }

fun PostchainClient.hasSigners() = config.signers.isNotEmpty()

fun PostchainClientConfig.pubkey() = signers.first().pubKey