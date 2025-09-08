package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.boolean
import com.github.ajalt.clikt.parameters.types.long
import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain.proposeStakingRequirementConstantsOperation
import net.postchain.mc.cli.ECBaseCommand
import net.postchain.mc.cli.base.ECONOMY_CHAIN_EC_CONSTANTS_AS_PROPOSALS_VERSION
import net.postchain.mc.cli.base.ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION
import net.postchain.mc.cli.base.ECONOMY_CHAIN_SCHEDULED_PROPOSAL_VERSION
import net.postchain.mc.cli.base.ECONOMY_CHAIN_STAKING_REQUIREMENTS_VERSION
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.scheduleAt
import net.postchain.mc.compatibility.ApiCompatECV28.updateStakingRequirementsEconomyConstantsOperationECV28
import net.postchain.mc.compatibility.ApiCompatECV52.proposeStakingRequirementConstantsOperationECV52
import net.postchain.mc.compatibility.ApiCompatECV57.proposeStakingRequirementConstantsOperation

class CommandUpdateStakingEconomyConstants : ECBaseCommand(
        name = "update-staking-constants",
        help = "Update staking economy chain constants which is required to be meet in order for providers to receive rewards.",
        requiresECVersion = ECONOMY_CHAIN_STAKING_REQUIREMENTS_VERSION
) {

    private val stakingRequirementsEnabled by option("--staking-requirements-enabled", help = "Enable or disable the staking requirement check on reward pay out").boolean()
    private val stakingRequirementsStopPayoutDays by option("--staking-requirements-stop-payout-days", help = "Number of days a provider can fail to meet the staking requirements no longer receiving reward payouts").long()
    private val stakingRequirementsSystemNodeOwnStakeChr by option("--staking-requirements-sn-own", help = "Required provider staking amount in CHR for system node").long()
    private val stakingRequirementsSystemNodeTotalStakeChr by option("--staking-requirements-sn-total", help = "Required total staking amount in CHR for system node").long()
    private val stakingRequirementsDappNodeOwnStakeChr by option("--staking-requirements-dn-own", help = "Required provider staking amount in CHR for dapp node").long()
    private val stakingRequirementsDappNodeTotalStakeChr by option("--staking-requirements-dn-total", help = "Required total staking amount in CHR for dapp node").long()
    private val scheduleAt by scheduleAt()

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        if (
                listOfNotNull(
                        stakingRequirementsEnabled,
                        stakingRequirementsStopPayoutDays,
                        stakingRequirementsSystemNodeOwnStakeChr,
                        stakingRequirementsSystemNodeTotalStakeChr,
                        stakingRequirementsDappNodeOwnStakeChr,
                        stakingRequirementsDappNodeTotalStakeChr,
                ).isEmpty()
        ) {
            throw CliktError("No value provided")
        }

        when {
            ecVersion.version < ECONOMY_CHAIN_EC_CONSTANTS_AS_PROPOSALS_VERSION -> {
                economyChainClient.transactionBuilder()
                        .updateStakingRequirementsEconomyConstantsOperationECV28(
                                stakingRequirementsEnabled,
                                stakingRequirementsStopPayoutDays,
                                stakingRequirementsSystemNodeOwnStakeChr,
                                stakingRequirementsSystemNodeTotalStakeChr,
                                stakingRequirementsDappNodeOwnStakeChr,
                                stakingRequirementsDappNodeTotalStakeChr,
                        )
            }
            ecVersion.version < ECONOMY_CHAIN_SCHEDULED_PROPOSAL_VERSION -> {
                economyChainClient.transactionBuilder()
                        .proposeStakingRequirementConstantsOperationECV52(
                                stakingRequirementsEnabled,
                                stakingRequirementsStopPayoutDays,
                                stakingRequirementsSystemNodeOwnStakeChr,
                                stakingRequirementsSystemNodeTotalStakeChr,
                                stakingRequirementsDappNodeOwnStakeChr,
                                stakingRequirementsDappNodeTotalStakeChr,
                        )
            }
            ecVersion.version < ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION -> {
                economyChainClient.transactionBuilder()
                        .proposeStakingRequirementConstantsOperation(
                                stakingRequirementsEnabled,
                                stakingRequirementsStopPayoutDays,
                                stakingRequirementsSystemNodeOwnStakeChr?.times(UNITS_PER_CHR),
                                stakingRequirementsSystemNodeTotalStakeChr?.times(UNITS_PER_CHR),
                                stakingRequirementsDappNodeOwnStakeChr?.times(UNITS_PER_CHR),
                                stakingRequirementsDappNodeTotalStakeChr?.times(UNITS_PER_CHR),
                                scheduleAt
                        )
            }
            else -> {
                economyChainClient.transactionBuilder()
                        .proposeStakingRequirementConstantsOperation(
                                clientProviderPubkey,
                                stakingRequirementsEnabled,
                                stakingRequirementsStopPayoutDays,
                                stakingRequirementsSystemNodeOwnStakeChr?.times(UNITS_PER_CHR),
                                stakingRequirementsSystemNodeTotalStakeChr?.times(UNITS_PER_CHR),
                                stakingRequirementsDappNodeOwnStakeChr?.times(UNITS_PER_CHR),
                                stakingRequirementsDappNodeTotalStakeChr?.times(UNITS_PER_CHR),
                                scheduleAt
                        )
            }
        }
        .postAwaitConfirmation(txListener())
        .printResult(
                "Proposal for updating staking economy constants is created and awaits approval.",
                "Failed to create staking economy constants update proposal"
        )
    }
}
