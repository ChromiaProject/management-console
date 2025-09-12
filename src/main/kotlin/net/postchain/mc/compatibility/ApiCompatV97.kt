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
            @Name("cluster_units") val clusterUnits: Long,
            @Name("extra_storage") val extraStorage: Long,
            @Name("system_container_units") @Nullable val systemContainerUnits: Long?,
            @Name("container_unit_cpu") @Nullable val containerUnitCpu: Long?,
            @Name("container_unit_ram") @Nullable val containerUnitRam: Long?,
            @Name("container_unit_io_read") @Nullable val containerUnitIoRead: Long?,
            @Name("container_unit_io_write") @Nullable val containerUnitIoWrite: Long?,
            @Name("container_unit_storage") @Nullable val containerUnitStorage: Long?,
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
