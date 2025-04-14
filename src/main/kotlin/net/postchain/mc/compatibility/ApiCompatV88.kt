package net.postchain.mc.compatibility

import net.postchain.client.transaction.TransactionBuilder
import net.postchain.common.BlockchainRid
import net.postchain.gtv.GtvFactory.gtv
import javax.annotation.processing.Generated

object ApiCompatV88 {

    const val PROPOSE_REMOVE_FORCED_CONFIGURATION = "propose_remove_forced_configuration"

    /**
     * Operation proposal_blockchain:propose_remove_forced_configuration
     *
     * Propose removal of forced configuration.
     *
     * Permission: container deployer
     *
     * Rate limit: actions
     * @param blockchainRid Blockchain RID to remove the configuration from.
     * @param height The height to remove the configuration from.
     * @param description A description for this proposal to explain why is should be approved.
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "proposal_blockchain:propose_remove_forced_configuration")
    fun TransactionBuilder.proposeRemoveForcedConfigurationOperation(blockchainRid: BlockchainRid,
                                                                     height: Long,
                                                                     description: String) =
            addOperation(PROPOSE_REMOVE_FORCED_CONFIGURATION, gtv(blockchainRid),
                    gtv(height),
                    gtv(description))


}