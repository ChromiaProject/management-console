package net.postchain.mc.compatibility

import net.postchain.client.core.PostchainQuery
import net.postchain.gtv.GtvFactory
import net.postchain.gtv.mapper.Name
import net.postchain.gtv.mapper.Nullable
import net.postchain.gtv.mapper.toObject
import javax.annotation.processing.Generated

object ApiCompatV56 {

    /*
    * Struct common.queries:cluster_data
    */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "common.queries:cluster_data")
    data class ClusterDataV56(
    	@Name("name") val name: String,
    	@Name("governor") val governor: String,
    	@Name("is_operational") val isOperational: Boolean,
    	@Name("cluster_units") @Nullable val clusterUnits: Long?,
    	@Name("extra_storage") @Nullable val extraStorage: Long?,
    	@Name("number_of_nodes") @Nullable val numberOfNodes: Long?,
    	@Name("container_units_available") @Nullable val containerUnitsAvailable: Long?,
    	@Name("extra_storage_available") @Nullable val extraStorageAvailable: Long?,
    )

    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "common.queries:get_cluster_data")
    fun PostchainQuery.getClusterDataV56(name: String) =
            query("get_cluster_data", GtvFactory.gtv(mapOf("name" to GtvFactory.gtv(name)))).toObject<ClusterDataV56>()
}
