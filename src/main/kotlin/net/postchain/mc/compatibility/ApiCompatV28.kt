package net.postchain.mc.compatibility

import net.postchain.client.core.PostchainQuery
import net.postchain.client.transaction.TransactionBuilder
import net.postchain.gtv.GtvFactory
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.gtv.mapper.Name
import net.postchain.gtv.mapper.Nullable
import net.postchain.gtv.mapper.toObject

object ApiCompatV28 {

    // @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "model:cluster_quota_data")
    data class ClusterQuotaDataV28(
            @Name("cluster_units") val clusterUnits: Long,
            @Name("extra_storage") val extraStorage: Long
    )

    // @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "common.queries:cluster_data")
    data class ClusterDataV28(
            @Name("name") val name: String,
            @Name("governor") val governor: String,
            @Name("is_operational") val isOperational: Boolean,
            @Name("cluster_units") @Nullable val clusterUnits: Long?,
            @Name("extra_storage") @Nullable val extraStorage: Long?,
            @Name("number_of_nodes") val numberOfNodes: Long
    )

    // @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "common.queries:get_cluster_data")
    fun PostchainQuery.getClusterDataV28(name: String) =
            query("get_cluster_data", GtvFactory.gtv(mapOf("name" to GtvFactory.gtv(name)))).toObject<ClusterDataV28>()

    // @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "direct_cluster:create_cluster_with_cluster_quota_data")
    fun TransactionBuilder.createClusterWithClusterQuotaDataOperationV28(myPubkey: ByteArray,
                                                                         name: String,
                                                                         governorVoterSet: String,
                                                                         providerPubkeys: List<ByteArray>,
                                                                         clusterQuotaData: ClusterQuotaDataV28) =
            addOperation("create_cluster_with_cluster_quota_data", GtvFactory.gtv(myPubkey),
                    GtvFactory.gtv(name),
                    GtvFactory.gtv(governorVoterSet),
                    GtvFactory.gtv(providerPubkeys.map { GtvFactory.gtv(it) }),
                    GtvObjectMapper.toGtvArray(clusterQuotaData))

    // @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "direct_cluster:create_cluster_from_with_cluster_quota_data_data")
    fun TransactionBuilder.createClusterFromWithClusterQuotaDataDataOperationV28(myPubkey: ByteArray,
                                                                                 name: String,
                                                                                 governorVoterSet: String,
                                                                                 providerVoterSet: String,
                                                                                 clusterQuotaData: ClusterQuotaDataV28) =
            addOperation("create_cluster_from_with_cluster_quota_data_data", GtvFactory.gtv(myPubkey),
                    GtvFactory.gtv(name),
                    GtvFactory.gtv(governorVoterSet),
                    GtvFactory.gtv(providerVoterSet),
                    GtvObjectMapper.toGtvArray(clusterQuotaData))

    // @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "direct_cluster:request_cluster")
    fun TransactionBuilder.requestClusterOperationV28(myPubkey: ByteArray,
                                                      name: String,
                                                      size: Long,
                                                      requireFull: Boolean) =
            addOperation("request_cluster", GtvFactory.gtv(myPubkey),
                    GtvFactory.gtv(name),
                    GtvFactory.gtv(size),
                    GtvFactory.gtv(requireFull))

    // @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "model:node_capability_type")
    enum class NodeCapabilityTypeV28 {
        SYSTEM_MANAGED,
        ETHEREUM_BRIDGE,
        ETHEREUM_TESTNET_BRIDGE,
        BCS_BRIDGE,
        BCS_TESTNET_BRIDGE,
        POLYGON_BRIDGE,
        POLYGON_TESTNET_BRIDGE
    }

    // @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "common.operations:update_node_capability")
    fun TransactionBuilder.updateNodeCapabilityOperationV28(myPubkey: ByteArray,
                                                            nodePubkey: ByteArray,
                                                            type: NodeCapabilityTypeV28,
                                                            add: Boolean) =
            addOperation("update_node_capability", GtvFactory.gtv(myPubkey),
                    GtvFactory.gtv(nodePubkey),
                    GtvFactory.gtv(type.ordinal.toLong()),
                    GtvFactory.gtv(add))
}