package net.postchain.mc.cli.economy

import assertk.assertThat
import assertk.assertions.isTrue
import net.postchain.common.hexStringToByteArray
import net.postchain.common.toHex
import net.postchain.economy.economy_chain.SET_PROVIDER_STAKING_ACCOUNT
import net.postchain.economy.lib.ft4.external.accounts.GET_ACCOUNTS_BY_SIGNER
import net.postchain.economy.lib.ft4.utils.PagedResult
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.gtv.merkle.GtvMerkleHashCalculatorV2
import net.postchain.gtv.merkleHash
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.assertCommandFailureContains
import net.postchain.mc.cli.test_helpers.assertCommandSuccessContains
import org.apache.commons.codec.digest.DigestUtils.sha256
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandSetProviderStakingAccountIT {

    val hashCalculator = GtvMerkleHashCalculatorV2(::sha256)

    val providerPubkey = "03445544C073F7B99670EAF3ABBC0972D07AB34A300BCB10A109482A2D99EF264C"
    val newAccountPubkey = "028A28D5A253DC6A253FC9384A5404DDCB0276A0CC0CD9C531DD4A44B1005A130F"
    val evmAddress = "0x1234567890123456789012345678901234567890"

    @Test
    fun `set provider staking account - old EC version success`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = 54)
                .testCommand(CommandSetProviderStakingAccount(),
                        "--pubkey", providerPubkey,
                        "--account-pk", newAccountPubkey) { result, api ->
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
        ManagedRestTestApi(dir, ecVersion = 55)
                .testCommand(CommandSetProviderStakingAccount(),
                        "--pubkey", providerPubkey,
                        "--account-pk", newAccountPubkey) { result, api ->
                    assertCommandSuccessContains(result,
                            "Staking account set to ${gtv(newAccountPubkey.hexStringToByteArray()).merkleHash(hashCalculator).toHex()}")
                    assertThat(api.getEcModel().opWasCalled(SET_PROVIDER_STAKING_ACCOUNT) {
                        it[0].asByteArray().contentEquals(providerPubkey.hexStringToByteArray()) &&
                                it[1].asByteArray().contentEquals(newAccountPubkey.hexStringToByteArray())
                    }).isTrue()
                }
    }

    @Test
    fun `set provider staking account - old EC version doesn't support EVM`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = 54)
                .testCommand(CommandSetProviderStakingAccount(),
                        "--pubkey", providerPubkey,
                        "--evm-address", evmAddress) { result, _ ->
                    assertCommandFailureContains(result, "--account-pk required for EC version < 55 (is 54)")
                }
    }

    @Test
    fun `set provider staking account - new EC version supports EVM`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = 55)
                .withECQuery(GET_ACCOUNTS_BY_SIGNER) { query ->
                    GtvObjectMapper.toGtvDictionary(PagedResult(null, listOf()))
                }
                .testCommand(CommandSetProviderStakingAccount(),
                        "--pubkey", providerPubkey,
                        "--evm-address", evmAddress) { result, _ ->
                    assertCommandFailureContains(result, "No FT4 Account found for signer: ${evmAddress.drop(2)}")
                }
    }

}
