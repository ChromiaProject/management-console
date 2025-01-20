package net.postchain.mc.compatibility

import net.postchain.client.transaction.TransactionBuilder
import net.postchain.common.BlockchainRid
import net.postchain.gtv.GtvFactory.gtv
import javax.annotation.processing.Generated

object ApiCompatV76 {
    const val PROPOSE_CONFIGURATION_V76 = "propose_configuration"
    /**
     * Operation proposal_blockchain:propose_configuration
     *
     * Propose new blockchain configuration.
     *
     * Permission: container deployer
     *
     * Rate limit: actions
     * @param myPubkey pubkey of provider
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "proposal_blockchain:propose_configuration")
    fun TransactionBuilder.proposeConfigurationOperationV76(myPubkey: ByteArray,
                                                            blockchainRid: BlockchainRid,
                                                            configData: ByteArray,
                                                            description: String) =
            addOperation(PROPOSE_CONFIGURATION_V76, gtv(myPubkey),
                    gtv(blockchainRid),
                    gtv(configData),
                    gtv(description))
}
