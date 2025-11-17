package net.postchain.mc.cli.transaction

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isTrue
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
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.assertCommandFailureContains
import net.postchain.mc.cli.test_helpers.assertCommandSuccessContains
import net.postchain.mc.cli.test_helpers.writeToTempDir
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandSendTransactionIT {

    @Test
    fun `send fully signed transaction succeeds`(@TempDir dir: Path) {
        val blockchainRid = BlockchainRid.buildRepeat(1)
        val gtxBody = GtxBody(blockchainRid, listOf(GtxOp("my_op", gtv(17))), listOf(
                ADDITIONAL_PUBKEY_1.hexStringToByteArray(),
                ADDITIONAL_PUBKEY_2.hexStringToByteArray(),
        ))
        val cryptoSystem = Secp256K1CryptoSystem()
        val signBuilder = GtxSignatureBuilder(gtxBody, gtxBody.calculateTxRid(GtvMerkleHashCalculatorV2(::sha256Digest)),
                cryptoSystem, check = false)
        signBuilder.sign(cryptoSystem.buildSigMaker(KeyPair(PubKey(ADDITIONAL_PUBKEY_1), PrivKey(ADDITIONAL_PRIVKEY_1))))
        signBuilder.sign(cryptoSystem.buildSigMaker(KeyPair(PubKey(ADDITIONAL_PUBKEY_2), PrivKey(ADDITIONAL_PRIVKEY_2))))
        val savedTx = MultiSignatureTxData(signBuilder.buildGtx().encode(), signBuilder.txRid)
        val savedTxFile = writeToTempDir(dir, "saved.tx", savedTx.encode())
        ManagedRestTestApi(dir)
                .testCommand(
                        CommandSendTransaction(),
                        savedTxFile.absolutePath.toString(),
                ) { result, api ->
                    assertCommandSuccessContains(result, "Transaction sent successfully")
                    assertThat(api.getDcModel().opWasCalled("my_op") {
                        it[0].asInteger() == 17L
                    }).isTrue()
                }
    }

    @Test
    fun `send partially signed transaction succeeds if needed signatures are provided`(@TempDir dir: Path) {
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

        val signatureFile = writeToTempDir(dir, "signature.sig", SignatureData(signBuilder.txRid,
                cryptoSystem.buildSigMaker(KeyPair(PubKey(ADDITIONAL_PUBKEY_2), PrivKey(ADDITIONAL_PRIVKEY_2))).signDigest(signBuilder.txRid)
        ).encode())

        ManagedRestTestApi(dir)
                .testCommand(
                        CommandSendTransaction(),
                        "--signature", signatureFile.absolutePath.toString(),
                        savedTxFile.absolutePath.toString(),
                ) { result, api ->
                    assertCommandSuccessContains(result, "Transaction sent successfully")
                    assertThat(api.getDcModel().opWasCalled("my_op") {
                        it[0].asInteger() == 17L
                    }).isTrue()
                }
    }

    @Test
    fun `signature for wrong transaction fails`(@TempDir dir: Path) {
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

        val wrongTxRid = sha256Digest(ByteArray(1))
        val signatureFile = writeToTempDir(dir, "signature.sig", SignatureData(wrongTxRid,
                cryptoSystem.buildSigMaker(KeyPair(PubKey(ADDITIONAL_PUBKEY_2), PrivKey(ADDITIONAL_PRIVKEY_2))).signDigest(wrongTxRid)
        ).encode())

        ManagedRestTestApi(dir)
                .testCommand(
                        CommandSendTransaction(),
                        "--signature", signatureFile.absolutePath.toString(),
                        savedTxFile.absolutePath.toString(),
                ) { result, api ->
                    assertCommandFailureContains(result, "Got signature for unexpected transaction ${wrongTxRid.toHex()}, expected ${signBuilder.txRid.toHex()}")
                    assertThat(api.getDcModel().capturedOps).isEmpty()
                }
    }

    @Test
    fun `send not fully signed transaction fails`(@TempDir dir: Path) {
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
        ManagedRestTestApi(dir)
                .testCommand(
                        CommandSendTransaction(),
                        savedTxFile.absolutePath.toString(),
                ) { result, api ->
                    assertCommandFailureContains(result, "Transaction needs to be signed by [$ADDITIONAL_PUBKEY_2] before it can be sent")
                    assertThat(api.getDcModel().capturedOps).isEmpty()
                }
    }
}
