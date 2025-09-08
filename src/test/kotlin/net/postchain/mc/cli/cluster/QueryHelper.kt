package net.postchain.mc.cli.cluster

import net.postchain.chain0.common.queries.ClusterData
import net.postchain.chain0.common.queries.GetClusterNodesResult
import net.postchain.chain0.common.queries.GetClusterProvidersResult
import net.postchain.common.types.WrappedByteArray
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi

fun ManagedRestTestApi.addGetClusterData(): ManagedRestTestApi {
    withDCQuery("get_cluster_data", GtvObjectMapper.toGtvDictionary(ClusterData(
            "cluster1",
            "SYSTEM_P",
            true,
            true,
            30,
            null,
            null,
            15,
            null,
            listOf()
    )))
    return this
}

fun ManagedRestTestApi.addGetClusterProviders(): ManagedRestTestApi {
    withDCQuery("get_cluster_providers", gtv(GtvObjectMapper.toGtvDictionary(
            GetClusterProvidersResult("node1", WrappedByteArray(33)))))
    return this
}


fun ManagedRestTestApi.addGetClusterNodes(): ManagedRestTestApi {
    withDCQuery("get_cluster_nodes", gtv(GtvObjectMapper.toGtvDictionary(GetClusterNodesResult(
            WrappedByteArray(33), "host", 7740, "https://host:7740", true))))
    return this
}
