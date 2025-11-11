package net.postchain.mc.cli.provider.keys

import com.chromia.cli.tools.util.thresholdOption
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import net.postchain.chain0.common.operations.setProviderKeyThresholdOperation
import net.postchain.chain0.provider_auth.model.ProviderKeyRole
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.DIRECTORY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER
import net.postchain.mc.compatibility.ApiCompatV87.setProviderKeyThresholdOperationV87

class CommandSetProviderKeyThreshold : DCBaseCommand(
        name = "threshold",
        help = """
            Set key threshold which controls the number of keys necessary to sign transactions for a provider.
        """.trimIndent(),
        requiresVersion = 65,
) {
    private val threshold by thresholdOption()
            .required()
            .validate { require(it >= -1L) { "Threshold must be -1, 0 or a positive integer" } }

    override fun runDC() {
        when {
            dcVersion < DIRECTORY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER -> {
                transactionBuilder()
                        .setProviderKeyThresholdOperationV87(ProviderKeyRole.main, threshold)
                        .postOrSave()
                        .printResult(
                                "Threshold set",
                                "Failed to set provider key threshold"
                        )
            }

            else -> {
                transactionBuilder()
                        .setProviderKeyThresholdOperation(clientProviderPubkey, ProviderKeyRole.main, threshold)
                        .postOrSave()
                        .printResult(
                                "Threshold set",
                                "Failed to set provider key threshold"
                        )
            }
        }
    }
}
