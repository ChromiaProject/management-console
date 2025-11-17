package net.postchain.mc.cli.transaction

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import com.chromia.build.tools.multisignature.MultiSignatureTxData
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
import net.postchain.mc.cli.SignatureData
import net.postchain.mc.cli.test_helpers.ADDITIONAL_PRIVKEY_1
import net.postchain.mc.cli.test_helpers.ADDITIONAL_PRIVKEY_2
import net.postchain.mc.cli.test_helpers.ADDITIONAL_PUBKEY_1
import net.postchain.mc.cli.test_helpers.ADDITIONAL_PUBKEY_2
import net.postchain.mc.cli.test_helpers.DEFAULT_PROVIDER01_PRIVKEY
import net.postchain.mc.cli.test_helpers.DEFAULT_PROVIDER01_PUBKEY
import net.postchain.mc.cli.test_helpers.assertCommandFailureContains
import net.postchain.mc.cli.test_helpers.assertCommandSuccessContains
import net.postchain.mc.cli.test_helpers.testPmcCommand
import net.postchain.mc.cli.test_helpers.writeChromiaConfig
import net.postchain.mc.cli.test_helpers.writeToTempDir
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.opentest4j.AssertionFailedError
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText

class CommandSignTransactionIT {

    @Test
    fun `sign succeeds`(@TempDir dir: Path) {
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
        writeChromiaConfig(dir, "http://localhost:0", null, null, ADDITIONAL_PUBKEY_2, ADDITIONAL_PRIVKEY_2, BlockchainRid.ZERO_RID)
        val result = testPmcCommand(dir, CommandSignTransaction(),
                "--target", dir.absolutePathString(),
                savedTxFile.absoluteFile.toString()
        )
        assertCommandSuccessContains(result, "Signature for ${signBuilder.txRid.toHex()} by $ADDITIONAL_PUBKEY_2 is written as hex to file:")
        val signatureFiles = dir.listDirectoryEntries("signature_for_*.sig")
        if (signatureFiles.size != 1) throw AssertionFailedError(result.output)
        val savedSignatureData = signatureFiles.single().readText()
        val savedTransaction = SignatureData.decode(savedSignatureData)
        assertThat(savedTransaction.txRid.toHex()).isEqualTo(signBuilder.txRid.toHex())
        assertThat(savedTransaction.signature.subjectID.toHex()).isEqualTo(ADDITIONAL_PUBKEY_2)
        assertThat(savedTransaction.signature.data).isNotEmpty()
    }

    @Test
    fun `sign fails when needed keys are missing`(@TempDir dir: Path) {
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
        writeChromiaConfig(dir, "http://localhost:0", null, null, DEFAULT_PROVIDER01_PUBKEY, DEFAULT_PROVIDER01_PRIVKEY, BlockchainRid.ZERO_RID)
        val result = testPmcCommand(dir, CommandSignTransaction(),
                "--target", dir.absolutePathString(),
                savedTxFile.absoluteFile.toString()
        )
        assertCommandFailureContains(result, "I don't have any of the keys required to sign this transaction, needs [$ADDITIONAL_PUBKEY_2]")
    }
}
