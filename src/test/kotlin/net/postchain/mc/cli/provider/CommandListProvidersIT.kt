package net.postchain.mc.cli.provider

import assertk.assertThat
import assertk.assertions.doesNotContain
import net.postchain.mc.cli.test_helpers.DEFAULT_PROVIDER01_PUBKEY
import net.postchain.mc.cli.test_helpers.DEFAULT_PROVIDER02_PUBKEY
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.assertLineValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandListProvidersIT {

    @Test
    fun `list providers - api v47+`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 47)
                .withDCQuery("get_all_providers", buildGetAllProvidersResponse())
                .testCommand(CommandListProviders()) { result, _ ->
                    assertLineValue(result.stdout, "Name", "provider01")
                    assertLineValue(result.stdout, "Url", "http://provider01:7740")
                    assertLineValue(result.stdout, "Pubkey", DEFAULT_PROVIDER01_PUBKEY)
                    assertLineValue(result.stdout, "Is_System", "true")
                    assertLineValue(result.stdout, "Tier", "NODE_PROVIDER")
                    assertLineValue(result.stdout, "Active", "true")

                    assertLineValue(result.stdout, "Name", "provider02")
                    assertLineValue(result.stdout, "Url", "http://provider02:7740")
                    assertLineValue(result.stdout, "Pubkey", DEFAULT_PROVIDER02_PUBKEY)
                    assertLineValue(result.stdout, "Is_System", "true")
                    assertLineValue(result.stdout, "Tier", "NODE_PROVIDER")
                    assertLineValue(result.stdout, "Active", "true")
                }
    }

    @Test
    fun `list providers - no filter lists all providers`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 47)
                .withDCQuery("get_all_providers", buildMixedGetAllProvidersResponse())
                .testCommand(CommandListProviders()) { result, _ ->
                    assertLineValue(result.stdout, "Name", "provider01")
                    assertLineValue(result.stdout, "Name", "provider02")
                    assertLineValue(result.stdout, "Name", "provider03")
                }
    }

    @Test
    fun `list providers - filter by system`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 47)
                .withDCQuery("get_all_providers", buildMixedGetAllProvidersResponse())
                .testCommand(CommandListProviders(), "--system") { result, _ ->
                    assertLineValue(result.stdout, "Name", "provider01")
                    assertLineValue(result.stdout, "Is_System", "true")
                    assertThat(result.stdout).doesNotContain("provider02")
                    assertThat(result.stdout).doesNotContain("provider03")
                }
    }

    @Test
    fun `list providers - filter by non-system`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 47)
                .withDCQuery("get_all_providers", buildMixedGetAllProvidersResponse())
                .testCommand(CommandListProviders(), "--non-system") { result, _ ->
                    assertLineValue(result.stdout, "Name", "provider02")
                    assertLineValue(result.stdout, "Name", "provider03")
                    assertThat(result.stdout).doesNotContain("provider01")
                }
    }

    @Test
    fun `list providers - filter by tier`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 47)
                .withDCQuery("get_all_providers", buildMixedGetAllProvidersResponse())
                .testCommand(CommandListProviders(), "--tier", "DAPP_PROVIDER") { result, _ ->
                    assertLineValue(result.stdout, "Name", "provider03")
                    assertLineValue(result.stdout, "Tier", "DAPP_PROVIDER")
                    assertThat(result.stdout).doesNotContain("provider01")
                    assertThat(result.stdout).doesNotContain("provider02")
                }
    }

    @Test
    fun `list providers - filter by non-system and tier`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 47)
                .withDCQuery("get_all_providers", buildMixedGetAllProvidersResponse())
                .testCommand(CommandListProviders(), "--non-system", "--tier", "NODE_PROVIDER") { result, _ ->
                    assertLineValue(result.stdout, "Name", "provider02")
                    assertThat(result.stdout).doesNotContain("provider01")
                    assertThat(result.stdout).doesNotContain("provider03")
                }
    }
}