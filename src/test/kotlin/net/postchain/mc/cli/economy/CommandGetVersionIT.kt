package net.postchain.mc.cli.economy

import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.assertCommandOutput
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandGetVersionIT {
    @Test
    fun `get version`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = 100)
                .testCommand(CommandVersion()) { result, _ ->
                    assertCommandOutput(result.stdout, "Economy chain api version: 100")
                }
    }
}