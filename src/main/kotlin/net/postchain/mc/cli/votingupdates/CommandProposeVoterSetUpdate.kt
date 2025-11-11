package net.postchain.mc.cli.votingupdates

import com.chromia.cli.tools.util.thresholdOption
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.split
import net.postchain.chain0.proposal_voter_set.proposeUpdateVoterSetOperation
import net.postchain.common.hexStringToByteArray
import net.postchain.common.toHex
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.proposalDescriptionOption


class CommandProposeVoterSetUpdate : DCBaseCommand(
        name = "update",
        help = """
            Propose an update of a voter set's governor
            
            New governor must be an existing voter set.
        """.trimIndent()
) {
    private val voterSet by option(
            "-vs", "--voter-set",
            help = "Name of existing voter set to update"
    ).required()

    private val threshold by thresholdOption()
    private val governor by option("--governor", help = "Name of new governor")
    private val newMember by option("--add-member", help = "Provider pubkey(s) to add to voter set")
            .convert { it.hexStringToByteArray() }
            .split(",")
            .default(listOf())
    private val removeMember by option("--remove-member", help = "Provider pubkey(s) to remove from voter set")
            .convert { it.hexStringToByteArray() }
            .split(",")
            .default(listOf())

    private val description by proposalDescriptionOption {
        "Update voter set $voterSet - threshold: $threshold, governor: $governor, " +
                "add members: ${newMember.map { it.toHex() }.toTypedArray().contentToString()}, " +
                "remove members: ${removeMember.map { it.toHex() }.toTypedArray().contentToString()}"
    }

    override fun runDC() {
        transactionBuilder()
                .proposeUpdateVoterSetOperation(
                        clientProviderPubkey,
                        voterSet, threshold, governor, newMember, removeMember, description
                )
                .postOrSave()
                .printResult(
                        "Proposal for voter set $voterSet has been added",
                        "Failed to add proposal"
                )
    }
}
