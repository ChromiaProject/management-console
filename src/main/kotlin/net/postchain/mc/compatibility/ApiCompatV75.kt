package net.postchain.mc.compatibility

import net.postchain.client.core.PostchainQuery
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.mapper.Name
import net.postchain.gtv.mapper.toObject
import javax.annotation.processing.Generated

object ApiCompatV75 {
    const val GET_CLUSTERS = "get_clusters"

    /**
     * Query common.queries:get_clusters
     *
     * Returns all clusters.
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "common.queries:get_clusters")
    fun PostchainQuery.getClustersV75() =
            query(GET_CLUSTERS, gtv(mapOf())).asArray().map { v1 -> v1.toObject<GetClustersResult>() }

    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "")
    data class GetClustersResult(
            @Name("name") val name: String,
            @Name("governor") val governor: String,
            @Name("operational") val operational: Boolean
    )
}
