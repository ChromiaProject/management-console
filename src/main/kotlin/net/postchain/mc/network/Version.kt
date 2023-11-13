package net.postchain.mc.network

import com.github.ajalt.clikt.core.PrintMessage
import net.postchain.chain0.version.apiVersion
import net.postchain.client.core.PostchainQuery
import net.postchain.client.exception.ClientError

class Version(private val client: PostchainQuery) {

    val version by lazy {
        try {
            client.apiVersion()
        } catch (e: ClientError) {
            println("Unable to fetch API version, assuming version 1: ${e.message}")
            1
        }
    }
}

/**
 * @return Actual version if it is valid
 */
fun PostchainQuery.requireApiVersion(version: Long): Long {
    val actualVersion = Version(this)
    if (actualVersion.version < version) throw PrintMessage("Command not supported by network") else return actualVersion.version
}
