package net.postchain.mc.compatibility

import net.postchain.chain0.direct_cluster.CREATE_CLUSTER_FROM_WITH_CLUSTER_DATA
import net.postchain.chain0.direct_cluster.CREATE_CLUSTER_WITH_CLUSTER_DATA
import net.postchain.client.transaction.TransactionBuilder
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.gtv.mapper.Name
import net.postchain.gtv.mapper.Nullable
import javax.annotation.processing.Generated

object ApiCompatV97 {

    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "model:cluster_creation_data")
    data class ClusterCreationDataV97(
            @param:Name("cluster_units") val clusterUnits: Long,
            @param:Name("extra_storage") val extraStorage: Long,
            @param:Name("system_container_units") @param:Nullable val systemContainerUnits: Long?,
            @param:Name("container_unit_cpu") @param:Nullable val containerUnitCpu: Long?,
            @param:Name("container_unit_ram") @param:Nullable val containerUnitRam: Long?,
            @param:Name("container_unit_io_read") @param:Nullable val containerUnitIoRead: Long?,

            @param:Name("container_unit_io_write") @param:Nullable val containerUnitIoWrite: Long?,
            @param:Name("container_unit_storage") @param:Nullable val containerUnitStorage: Long?,
    )

    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "direct_cluster:create_cluster_with_cluster_data")
    fun TransactionBuilder.createClusterWithClusterDataOperationV97(myPubkey: ByteArray,
                                                                    name: String,
                                                                    governorVoterSet: String,
                                                                    providerPubkeys: List<ByteArray>,
                                                                    clusterCreationData: ClusterCreationDataV97) =
            addOperation(CREATE_CLUSTER_WITH_CLUSTER_DATA, gtv(myPubkey),
                    gtv(name),
                    gtv(governorVoterSet),
                    gtv(providerPubkeys.map { gtv(it) }),
                    GtvObjectMapper.toGtvArray(clusterCreationData))

    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "direct_cluster:create_cluster_from_with_cluster_data")
    fun TransactionBuilder.createClusterFromWithClusterDataOperationV97(myPubkey: ByteArray,
                                                                        name: String,
                                                                        governorVoterSet: String,
                                                                        providerVoterSet: String,
                                                                        clusterCreationData: ClusterCreationDataV97) =
            addOperation(CREATE_CLUSTER_FROM_WITH_CLUSTER_DATA, gtv(myPubkey),
                    gtv(name),
                    gtv(governorVoterSet),
                    gtv(providerVoterSet),
                    GtvObjectMapper.toGtvArray(clusterCreationData))
}
