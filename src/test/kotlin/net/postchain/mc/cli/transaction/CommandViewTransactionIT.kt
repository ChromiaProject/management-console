package net.postchain.mc.cli.transaction

import com.chromia.build.tools.multisignature.MultiSignatureTxData
import com.github.ajalt.clikt.testing.test
import net.postchain.common.BlockchainRid
import net.postchain.common.hexStringToByteArray
import net.postchain.common.toHex
import net.postchain.crypto.KeyPair
import net.postchain.crypto.PrivKey
import net.postchain.crypto.PubKey
import net.postchain.crypto.Secp256K1CryptoSystem
import net.postchain.crypto.sha256Digest
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.merkle.GtvMerkleHashCalculatorV2
import net.postchain.gtx.GtxBody
import net.postchain.gtx.GtxOp
import net.postchain.gtx.GtxSignatureBuilder
import net.postchain.mc.cli.test_helpers.ADDITIONAL_PRIVKEY_1
import net.postchain.mc.cli.test_helpers.ADDITIONAL_PUBKEY_1
import net.postchain.mc.cli.test_helpers.ADDITIONAL_PUBKEY_2
import net.postchain.mc.cli.test_helpers.assertCommandSuccessContains
import net.postchain.mc.cli.test_helpers.writeToTempDir
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandViewTransactionIT {

    @Test
    fun `view saved transaction`(@TempDir dir: Path) {
        val blockchainRid = BlockchainRid.buildRepeat(1)
        val gtxBody = GtxBody(blockchainRid, listOf(GtxOp("my_op", gtv(17))), listOf(
                ADDITIONAL_PUBKEY_1.hexStringToByteArray(),
                ADDITIONAL_PUBKEY_2.hexStringToByteArray(),
        ))
        val cryptoSystem = Secp256K1CryptoSystem()
        val signBuilder = GtxSignatureBuilder(gtxBody, gtxBody.calculateTxRid(GtvMerkleHashCalculatorV2(::sha256Digest)),
                cryptoSystem, check = false)
        signBuilder.sign(cryptoSystem.buildSigMaker(KeyPair(PubKey(ADDITIONAL_PUBKEY_1), PrivKey(ADDITIONAL_PRIVKEY_1))))
        val savedTx = MultiSignatureTxData(signBuilder.buildGtx().encode(), signBuilder.txRid)
        val savedTxFile = writeToTempDir(dir, "saved.tx", savedTx.encode())
        val result = CommandViewTransaction().test(argv = listOf(savedTxFile.absoluteFile.toString()))
        assertCommandSuccessContains(result, """
            {
            "transactionRID": "${signBuilder.txRid.toHex()}",
            "blockchainRID": "0101010101010101010101010101010101010101010101010101010101010101",
            "operations": [
            {
            "operation": "my_op",
            "arguments": [
            "integer: 17"
            ]
            }
            ],
            "signers": [
            "$ADDITIONAL_PUBKEY_1",
            "$ADDITIONAL_PUBKEY_2"
            ],
            "signatures": [
            "3E3D94E20396923478D502B1DCD2659F0021C785E98617475EFEF4CFC8042F693D95D5FA4E45A4BA23F60297E924E130125BEA9FE284272F0BFBFAFA2B42A574",
            ""
            ]
            }            
        """.trimIndent())
    }
}
