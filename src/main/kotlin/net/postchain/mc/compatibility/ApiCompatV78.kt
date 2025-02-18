package net.postchain.mc.compatibility

import net.postchain.client.transaction.TransactionBuilder
import net.postchain.common.BlockchainRid
import net.postchain.gtv.GtvFactory.gtv
import javax.annotation.processing.Generated

object ApiCompatV78 {
    const val PROPOSE_FORCED_CONFIGURATION_V78 = "propose_forced_configuration"
    /**
     * Operation proposal_blockchain:propose_forced_configuration
     *
     * Propose forcing a new configuration for a PAUSED blockchain at height.
     *
     * Permission: container deployer
     *
     * Rate limit: actions
     * @param myPubkey pubkey of provider
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "proposal_blockchain:propose_forced_configuration")
    fun TransactionBuilder.proposeForcedConfigurationOperationV78(myPubkey: ByteArray,
    	blockchainRid: BlockchainRid,
    	configData: ByteArray,
    	height: Long,
    	description: String) =
       addOperation(PROPOSE_FORCED_CONFIGURATION_V78, gtv(myPubkey),
    	gtv(blockchainRid),
    	gtv(configData),
    	gtv(height),
    	gtv(description))
}
