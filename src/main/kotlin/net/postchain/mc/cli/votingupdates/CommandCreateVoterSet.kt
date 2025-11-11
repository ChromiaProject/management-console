package net.postchain.mc.cli.votingupdates

import com.chromia.cli.tools.util.thresholdOption
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import net.postchain.chain0.proposal.voting.createVoterSetOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.entityNameValidator
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.pubkeysOption


class CommandCreateVoterSet : DCBaseCommand(
        name = "create",
        help = "Create a new voter set with a list of providers"
) {
    private val name by nameOption("Name of new voter set").required().validate(entityNameValidator())

    private val pubkeys by pubkeysOption("Comma separated list of provider pubkeys for this voter set")

    private val threshold by thresholdOption().default(0L)
            .validate { require(it >= -1L) { "Threshold must be -1, 0 or a positive integer" } }

    private val governorName by option(
            "-g", "--governor",
            help = "Name of another voter set which can update this voter set. Default: voter set is its own governor."
    )

    override fun runDC() {
        transactionBuilder()
                .createVoterSetOperation(
                        clientProviderPubkey,
                        name,
                        threshold,
                        pubkeys.map { it.data },
                        governorName
                )
                .postOrSave()
                .printResult(
                        "Voter set created",
                        "Cannot create voter set"
                )
    }
}