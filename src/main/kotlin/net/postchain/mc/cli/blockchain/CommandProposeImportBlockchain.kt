package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.path
import net.postchain.chain0.proposal_blockchain.proposeImportBlockchainOperation
import net.postchain.client.core.TransactionResult
import net.postchain.common.BlockchainRid
import net.postchain.common.tx.TransactionStatus
import net.postchain.crypto.sha256Digest
import net.postchain.gtv.GtvDecoder
import net.postchain.gtv.merkle.GtvMerkleHashCalculator
import net.postchain.gtv.merkleHash
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.nopClientOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import java.io.BufferedInputStream
import java.io.FileInputStream

class CommandProposeImportBlockchain : CliktCommand(
        name = "import",
        help = "Propose importing a blockchain in a specific container. Change will be applied after voting within the deployer voter set of the cluster that the container belongs to."
) {
    private val client by nopClientOption()

    private val configurationsFile by option("--configurations-file", help = "File to import blockchain configurations from")
            .path(mustExist = true, canBeDir = false, canBeFile = true, mustBeReadable = true).required()

    private val container by option("-c", "--container", help = "Name of container to run in").required()

    private val name by nameOption("Name of blockchain").required()

    private val description by proposalDescriptionOption(default = "Propose importing of blockchain")

    override fun run() {
        echo("Blockchain $name will be imported")
        BufferedInputStream(FileInputStream(configurationsFile.toFile())).use {
            val blockchainRid = BlockchainRid(GtvDecoder.decodeGtv(it).asByteArray())
            val configurations = buildMap {
                while (true) {
                    val gtv = GtvDecoder.decodeGtv(it)
                    if (gtv.isNull()) {
                        break
                    }
                    val height = gtv.asArray()[0].asInteger()
                    val data = gtv.asArray()[1].asByteArray()
                    put(height, hash(data))
                }
            }
            val txBuilder = client.transactionBuilder()
            txBuilder.proposeImportBlockchainOperation(
                    client.pubkey, name, blockchainRid, configurations, container, description)
            txBuilder.postAwaitConfirmation().printResultPolitely(configurations.size)
        }
    }

    private fun hash(data: ByteArray) =
            GtvDecoder.decodeGtv(data).merkleHash(GtvMerkleHashCalculator(::sha256Digest))

    private fun TransactionResult.printResultPolitely(numConfigs: Int) {
        val onSuccess = "Blockchain $name with $numConfigs blockchain configuration(s) imported"
        val onFail = "Cannot import blockchain config(s)"
        try {
            printResult(onSuccess, onFail)
        } catch (e: CliktError) {
            when (status) {
                TransactionStatus.CONFIRMED -> echo(onSuccess)
                else -> throw e
            }
        }
    }
}
