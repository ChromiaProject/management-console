package net.postchain.mc.cli.util

import net.postchain.base.configuration.BlockchainFeatures
import net.postchain.base.configuration.KEY_FEATURES
import net.postchain.common.types.WrappedByteArray
import net.postchain.common.wrap
import net.postchain.gtv.Gtv
import net.postchain.gtv.GtvEncoder
import net.postchain.gtv.GtvFactory
import net.postchain.gtv.gtvml.GtvMLParser
import net.postchain.gtv.merkle.GtvMerkleHashCalculatorBase
import net.postchain.gtv.merkle.makeMerkleHashCalculator
import net.postchain.gtv.merkleHash
import java.io.File

class BlockchainConfig(
        val hash: WrappedByteArray,
        val data: ByteArray,
        val gtv: Gtv
) {

    companion object {
        fun readFromFile(blockchainConfigFile: File): BlockchainConfig {
            val (gtv, data) = if (blockchainConfigFile.extension == "gtv") {
                val data = blockchainConfigFile.readBytes()
                val gtv = GtvFactory.decodeGtv(data)
                gtv to data
            } else {
                val gtv = GtvMLParser.parseGtvML(blockchainConfigFile.readText())
                val data = GtvEncoder.encodeGtv(gtv)
                gtv to data
            }

            return create(gtv, data)
        }

        fun readFromByteArray(data: ByteArray): BlockchainConfig = create(GtvFactory.decodeGtv(data), data)

        private fun create(gtv: Gtv, data: ByteArray): BlockchainConfig {
            val hash = gtv.merkleHash(getMerkelHashCalculator(gtv)).wrap()
            return BlockchainConfig(hash, data, gtv)
        }

        private fun getMerkelHashCalculator(gtv: Gtv): GtvMerkleHashCalculatorBase {
            val features = gtv[KEY_FEATURES]?.asDict()
            val merkleHashVersion: Long = features?.get(BlockchainFeatures.merkle_hash_version.name)?.asInteger() ?: 1L
            return makeMerkleHashCalculator(merkleHashVersion)
        }
    }
}
