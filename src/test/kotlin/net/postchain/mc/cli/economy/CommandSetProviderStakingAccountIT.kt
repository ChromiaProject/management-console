package net.postchain.mc.cli.economy

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isTrue
import net.postchain.common.hexStringToByteArray
import net.postchain.common.toHex
import net.postchain.common.wrap
import net.postchain.crypto.sha256Digest
import net.postchain.economy.economy_chain.GET_PROVIDER_STAKING_ACCOUNT
import net.postchain.economy.economy_chain.SET_PROVIDER_STAKING_ACCOUNT
import net.postchain.economy.lib.ft4.core.accounts.AuthType
import net.postchain.economy.lib.ft4.external.accounts.Ft4GetAccountMainAuthDescriptorResult
import net.postchain.economy.lib.ft4.external.accounts.GET_ACCOUNTS_BY_SIGNER
import net.postchain.economy.lib.ft4.external.accounts.GET_ACCOUNT_MAIN_AUTH_DESCRIPTOR
import net.postchain.economy.lib.ft4.utils.PagedResult
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.GtvNull
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.gtv.merkle.GtvMerkleHashCalculatorV2
import net.postchain.gtv.merkleHash
import net.postchain.mc.cli.test_helpers.ADDITIONAL_PRIVKEY_2
import net.postchain.mc.cli.test_helpers.ADDITIONAL_PUBKEY_1
import net.postchain.mc.cli.test_helpers.ADDITIONAL_PUBKEY_2
import net.postchain.mc.cli.test_helpers.DEFAULT_BRID_ECONOMY_CHAIN
import net.postchain.mc.cli.test_helpers.DEFAULT_PROVIDER01_PUBKEY
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.assertCommandFailureContains
import net.postchain.mc.cli.test_helpers.assertCommandSuccessContains
import net.postchain.mc.cli.test_helpers.assertSavedTransaction
import org.apache.commons.codec.digest.DigestUtils.sha256
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class CommandSetProviderStakingAccountIT {

    val hashCalculator = GtvMerkleHashCalculatorV2(::sha256)

    val providerPubkey = "03445544C073F7B99670EAF3ABBC0972D07AB34A300BCB10A109482A2D99EF264C"
    val oldAccountPubkey = ADDITIONAL_PUBKEY_1
    val oldAccountId = sha256Digest(oldAccountPubkey.hexStringToByteArray())
    val newAccountPubkey = ADDITIONAL_PUBKEY_2
    val newAccountPrivkey = ADDITIONAL_PRIVKEY_2
    val evmAddress = "0x1234567890123456789012345678901234567890"

    @Test
    fun `set provider staking account - old EC version success`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = 54, extraPubKey = newAccountPubkey, extraPrivKey = newAccountPrivkey)
                .testCommand(
                        CommandSetProviderStakingAccount(),
                        "--pubkey", providerPubkey,
                        "--account-pk", newAccountPubkey,
                        "--target", dir.absolutePathString(),
                ) { result, api ->
                    assertCommandSuccessContains(result,
                            "Staking account set to ${gtv(newAccountPubkey.hexStringToByteArray()).merkleHash(hashCalculator).toHex()}")
                    assertThat(api.getEcModel().opWasCalled(SET_PROVIDER_STAKING_ACCOUNT) {
                        it[0].asByteArray().contentEquals(providerPubkey.hexStringToByteArray()) &&
                                it[1].asByteArray().contentEquals(newAccountPubkey.hexStringToByteArray())
                    }).isTrue()
                }
    }

    @Test
    fun `set provider staking account - new EC version uses old operation with --account-pk option`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = 55, extraPubKey = newAccountPubkey, extraPrivKey = newAccountPrivkey)
                .testCommand(
                        CommandSetProviderStakingAccount(),
                        "--pubkey", providerPubkey,
                        "--account-pk", newAccountPubkey,
                        "--target", dir.absolutePathString(),
                ) { result, api ->
                    assertCommandSuccessContains(result,
                            "Staking account set to ${gtv(newAccountPubkey.hexStringToByteArray()).merkleHash(hashCalculator).toHex()}")
                    assertThat(api.getEcModel().opWasCalled(SET_PROVIDER_STAKING_ACCOUNT) {
                        it[0].asByteArray().contentEquals(providerPubkey.hexStringToByteArray()) &&
                                it[1].asByteArray().contentEquals(newAccountPubkey.hexStringToByteArray())
                    }).isTrue()
                }
    }

    @Test
    fun `set provider staking account - save to file`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = 55)
                .testCommand(
                        CommandSetProviderStakingAccount(),
                        "--pubkey", providerPubkey,
                        "--account-pk", newAccountPubkey,
                        "--target", dir.absolutePathString(),
                ) { result, api ->
                    assertSavedTransaction(result, dir, DEFAULT_BRID_ECONOMY_CHAIN, listOf(DEFAULT_PROVIDER01_PUBKEY), listOf(newAccountPubkey))
                    assertThat(api.getEcModel().capturedOps).isEmpty()
                }
    }

    @Test
    fun `set provider staking account - old EC version doesn't support EVM`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = 54)
                .testCommand(
                        CommandSetProviderStakingAccount(),
                        "--pubkey", providerPubkey,
                        "--evm-address", evmAddress,
                        "--target", dir.absolutePathString(),
                ) { result, _ ->
                    assertCommandFailureContains(result, "--account-pk required for EC version < 55 (is 54)")
                }
    }

    @Test
    fun `set provider staking account - new EC version supports EVM`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = 55)
                .withECQuery(GET_ACCOUNTS_BY_SIGNER) { _ ->
                    GtvObjectMapper.toGtvDictionary(PagedResult(null, listOf()))
                }
                .testCommand(
                        CommandSetProviderStakingAccount(),
                        "--pubkey", providerPubkey,
                        "--evm-address", evmAddress,
                        "--target", dir.absolutePathString(),
                ) { result, _ ->
                    assertCommandFailureContains(result, "No FT4 Account found for signer: ${evmAddress.drop(2)}")
                }
    }

    @Test
    fun `set provider staking account - fetch old account`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = 66)
                .withECQuery(GET_PROVIDER_STAKING_ACCOUNT, gtv(oldAccountId))
                .withECQuery(GET_ACCOUNT_MAIN_AUTH_DESCRIPTOR, GtvObjectMapper.toGtvDictionary(Ft4GetAccountMainAuthDescriptorResult(
                        id = sha256Digest(oldAccountId).wrap(),
                        accountId = oldAccountId.wrap(),
                        authType = AuthType.S,
                        args = gtv(listOf(gtv(0L), gtv(oldAccountPubkey.hexStringToByteArray()))),
                        rules = GtvNull,
                        created = 0,
                )))
                .testCommand(
                        CommandSetProviderStakingAccount(),
                        "--pubkey", providerPubkey,
                        "--account-pk", newAccountPubkey,
                        "--target", dir.absolutePathString(),
                ) { result, api ->
                    assertSavedTransaction(result, dir, DEFAULT_BRID_ECONOMY_CHAIN, listOf(DEFAULT_PROVIDER01_PUBKEY),
                            listOf(newAccountPubkey, oldAccountPubkey))
                    assertThat(api.getEcModel().capturedOps).isEmpty()
                }
    }
}
