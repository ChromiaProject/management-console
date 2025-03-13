package net.postchain.mc.cli.base

import com.github.ajalt.clikt.core.CliktError
import net.postchain.client.core.PostchainClient

val PostchainClient.pubkey: ByteArray
    get() = config.signers.firstOrNull()?.pubKey?.data
            ?: throw CliktError("No keypair specified in configuration")
