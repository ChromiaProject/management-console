package net.postchain.mc.compatibility

import net.postchain.client.core.PostchainQuery
import net.postchain.client.transaction.TransactionBuilder
import net.postchain.common.types.RowId
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.GtvNull
import net.postchain.gtv.mapper.Name
import net.postchain.gtv.mapper.toObject

object ApiCompatV22 {

    // @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "model:container_resource_limit_type")
    enum class ContainerResourceLimitType {
        container_units,
        max_blockchains
    }

    // @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "proposal_container.proposal_container_limits:propose_container_limits")
    fun TransactionBuilder.proposeContainerLimitsOperationV22(myPubkey: ByteArray,
                                                              containerName: String,
                                                              limits: Map<ContainerResourceLimitType, Long>,
                                                              description: String) =
            addOperation("propose_container_limits", gtv(myPubkey),
                    gtv(containerName),
                    gtv(limits.map { (k, v) -> gtv(gtv(k.ordinal.toLong()), gtv(v)) }),
                    gtv(description))

    // @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "proposal_cluster:propose_cluster_limits")
    fun TransactionBuilder.proposeClusterLimitsOperationV22(myPubkey: ByteArray,
                                                            clusterName: String,
                                                            clusterUnits: Long?,
                                                            description: String) =
            addOperation("propose_cluster_limits", gtv(myPubkey),
                    gtv(clusterName),
                    clusterUnits.let { if (it == null) GtvNull else gtv(it) },
                    gtv(description))

    // @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "proposal_container.proposal_container_limits:get_container_limits_proposal")
    fun PostchainQuery.getContainerLimitsProposalV22(rowid: RowId) =
            query("get_container_limits_proposal", gtv(mapOf("rowid" to gtv(rowid.id)))).let { v -> if (v is GtvNull) null else v.toObject<GetContainerLimitsProposalResult>() }

    // @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "")
    data class GetContainerLimitsProposalResult(
            @Name("container") val container: String,
            @Name("container_units") val containerUnits: Long,
            @Name("max_blockchains") val maxBlockchains: Long
    )

    // @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "proposal_cluster:get_cluster_limits_proposal")
    fun PostchainQuery.getClusterLimitsProposalV22(rowid: RowId) =
            query("get_cluster_limits_proposal", gtv(mapOf("rowid" to gtv(rowid.id)))).let { v -> if (v is GtvNull) null else v.toObject<GetClusterLimitsProposalResult>() }

    // @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "")
    data class GetClusterLimitsProposalResult(
            @Name("cluster") val cluster: String,
            @Name("cluster_units") val clusterUnits: Long
    )

    // @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "proposal_container:get_container_proposal")
    fun PostchainQuery.getContainerProposalV22(rowid: RowId) =
            query("get_container_proposal", gtv(mapOf("rowid" to gtv(rowid.id)))).let { v -> if (v is GtvNull) null else v.toObject<GetContainerProposalResult>() }

    // @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "")
    data class GetContainerProposalResult(
            @Name("container") val container: String,
            @Name("container_units") val containerUnits: Long,
            @Name("max_blockchains") val maxBlockchains: Long
    )

}