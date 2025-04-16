package net.postchain.mc.compatibility

import net.postchain.chain0.blockchain_auth.ADD_PROVIDER_BLOCKCHAIN_AUTH
import net.postchain.chain0.common.operations.ADD_PROVIDER_KEY
import net.postchain.chain0.common.operations.REVOKE_PROVIDER_KEY
import net.postchain.chain0.common.operations.SET_PROVIDER_KEY_THRESHOLD
import net.postchain.chain0.direct_cluster.CREATE_CLUSTER_FROM_WITH_CLUSTER_DATA
import net.postchain.chain0.direct_cluster.CREATE_CLUSTER_WITH_CLUSTER_DATA
import net.postchain.chain0.proposal_blockchain.PROPOSE_REMOVE_FORCED_CONFIGURATION
import net.postchain.chain0.provider_auth.model.ProviderKeyRole
import net.postchain.client.transaction.TransactionBuilder
import net.postchain.common.BlockchainRid
import net.postchain.crypto.PubKey
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.gtv.mapper.Name
import javax.annotation.processing.Generated

object ApiCompatV87 {

    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "model:cluster_creation_data")
    data class ClusterCreationDataV87(
            @Name("cluster_units") val clusterUnits: Long,
            @Name("extra_storage") val extraStorage: Long
    )

    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "direct_cluster:create_cluster_with_cluster_data")
    fun TransactionBuilder.createClusterWithClusterDataOperationV87(myPubkey: ByteArray,
                                                                    name: String,
                                                                    governorVoterSet: String,
                                                                    providerPubkeys: List<ByteArray>,
                                                                    clusterCreationData: ClusterCreationDataV87) =
            addOperation(CREATE_CLUSTER_WITH_CLUSTER_DATA, gtv(myPubkey),
                    gtv(name),
                    gtv(governorVoterSet),
                    gtv(providerPubkeys.map { gtv(it) }),
                    GtvObjectMapper.toGtvArray(clusterCreationData))

    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "direct_cluster:create_cluster_from_with_cluster_data")
    fun TransactionBuilder.createClusterFromWithClusterDataOperationV87(myPubkey: ByteArray,
                                                                        name: String,
                                                                        governorVoterSet: String,
                                                                        providerVoterSet: String,
                                                                        clusterCreationData: ClusterCreationDataV87) =
            addOperation(CREATE_CLUSTER_FROM_WITH_CLUSTER_DATA, gtv(myPubkey),
                    gtv(name),
                    gtv(governorVoterSet),
                    gtv(providerVoterSet),
                    GtvObjectMapper.toGtvArray(clusterCreationData))

    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "proposal_blockchain:propose_remove_forced_configuration")
    fun TransactionBuilder.proposeRemoveForcedConfigurationOperationV87(blockchainRid: BlockchainRid,
                                                                        height: Long,
                                                                        description: String) =
            addOperation(PROPOSE_REMOVE_FORCED_CONFIGURATION, gtv(blockchainRid),
                    gtv(height),
                    gtv(description))

    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "blockchain_auth:add_provider_blockchain_auth")
    fun TransactionBuilder.addProviderBlockchainAuthOperationV87(blockchainRid: BlockchainRid) =
       addOperation(ADD_PROVIDER_BLOCKCHAIN_AUTH,
    	gtv(blockchainRid))

    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "common.operations:add_provider_key")
    fun TransactionBuilder.addProviderKeyOperationV87(role: ProviderKeyRole,
    	pubkey: PubKey) =
       addOperation(ADD_PROVIDER_KEY,
    	gtv(role.ordinal.toLong()),
    	gtv(pubkey.data))

    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "common.operations:revoke_provider_key")
    fun TransactionBuilder.revokeProviderKeyOperationV87(role: ProviderKeyRole,
    	pubkey: PubKey) =
       addOperation(REVOKE_PROVIDER_KEY,
    	gtv(role.ordinal.toLong()),
    	gtv(pubkey.data))

    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "common.operations:set_provider_key_threshold")
    fun TransactionBuilder.setProviderKeyThresholdOperationV87(role: ProviderKeyRole,
    	threshold: Long) =
       addOperation(SET_PROVIDER_KEY_THRESHOLD,
    	gtv(role.ordinal.toLong()),
    	gtv(threshold))
}
