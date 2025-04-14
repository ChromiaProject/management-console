package net.postchain.mc.compatibility

import net.postchain.chain0.direct_cluster.CREATE_CLUSTER_FROM_WITH_CLUSTER_DATA
import net.postchain.chain0.direct_cluster.CREATE_CLUSTER_WITH_CLUSTER_DATA
import net.postchain.client.transaction.TransactionBuilder
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

}