package net.postchain.mc.cli.provider.keys

import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import net.postchain.chain0.common.setProviderKeyThresholdOperation
import net.postchain.chain0.provider_auth.model.ProviderKeyRole
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import com.chromia.cli.tools.util.thresholdOption

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

        client.transactionBuilder()
                .setProviderKeyThresholdOperation(ProviderKeyRole.main, threshold)
                .postAwaitConfirmation()
                .printResult(
                        "Threshold set",
                        "Failed to set provider key threshold"
                )
    }
}
