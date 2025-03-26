package net.postchain.mc.cli.provider

import net.postchain.mc.cli.test_helpers.DEFAULT_PROVIDER01_PUBKEY
import net.postchain.mc.cli.test_helpers.DEFAULT_PROVIDER02_PUBKEY
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.assertLineValue
import net.postchain.mc.cli.test_helpers.buildGetAllProvidersResponse
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
}