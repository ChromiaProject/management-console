package net.postchain.mc.compatibility

import net.postchain.client.core.PostchainQuery
import net.postchain.client.transaction.TransactionBuilder
import net.postchain.common.BlockchainRid
import net.postchain.common.types.RowId
import net.postchain.common.types.WrappedByteArray
import net.postchain.gtv.GtvFactory
import net.postchain.gtv.GtvNull
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.gtv.mapper.Name
import net.postchain.gtv.mapper.Nullable
import net.postchain.gtv.mapper.toObject
import javax.annotation.processing.Generated

object ApiCompatV33 {

    /**
     * Query proposal_blockchain_import:get_finish_blockchain_import_proposal
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "proposal_blockchain_import:get_finish_blockchain_import_proposal")
    fun PostchainQuery.getFinishBlockchainImportProposalV33(rowid: RowId?) =
            query("get_finish_blockchain_import_proposal", GtvFactory.gtv(mapOf("rowid" to rowid.let { if (it == null) GtvNull else GtvFactory.gtv(it.id) }))).let { v -> if (v is GtvNull) null else v.toObject<GetFinishBlockchainImportProposalResultV33>() }

    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "")
    data class GetFinishBlockchainImportProposalResultV33(
            @Name("blockchain_rid") val blockchainRid: WrappedByteArray,
            @Name("finish_at_height") val finishAtHeight: Long
    )

    /**
     * Query proposal_blockchain_import:get_foreign_blockchain_blocks_import_proposal
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "proposal_blockchain_import:get_foreign_blockchain_blocks_import_proposal")
    fun PostchainQuery.getForeignBlockchainBlocksImportProposalV33(rowid: RowId?) =
            query("get_foreign_blockchain_blocks_import_proposal", GtvFactory.gtv(mapOf("rowid" to rowid.let { if (it == null) GtvNull else GtvFactory.gtv(it.id) }))).let { v -> if (v is GtvNull) null else v.toObject<GetForeignBlockchainBlocksImportProposalResultV33>() }

    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "")
    data class GetForeignBlockchainBlocksImportProposalResultV33(
            @Name("blockchain_rid") val blockchainRid: WrappedByteArray,
            @Name("up_to_height") val upToHeight: Long
    )

    /**
     * Query common.queries:get_importing_foreign_blockchain_info
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "common.queries:get_importing_foreign_blockchain_info")
    fun PostchainQuery.getImportingForeignBlockchainInfoV33(blockchainRid: BlockchainRid) =
            query("get_importing_foreign_blockchain_info", GtvFactory.gtv(mapOf("blockchain_rid" to GtvFactory.gtv(blockchainRid)))).let { v -> if (v is GtvNull) null else v.toObject<ImportingForeignBlockchainV33>() }

    /*
    * Entity model:importing_foreign_blockchain
    *
    * Rell entity is typically encoded as a GtvInteger. If used as struct<model:importing_foreign_blockchain>, then GtvObjectMapper.toGtvArray() is used for encoding.
    */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "model:importing_foreign_blockchain")
    data class ImportingForeignBlockchainV33(
            @Name("blockchain_rid") val blockchainRid: WrappedByteArray,
            @Name("pubkey") val pubkey: WrappedByteArray,
            @Name("host") val host: String,
            @Name("port") val port: Long,
            @Name("api_url") val apiUrl: String,
            @Name("chain0_rid") val chain0Rid: WrappedByteArray,
            @Name("up_to_height") val upToHeight: Long
    )


    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "model:cluster_creation_data")
    data class ClusterCreationDataV33(
            @Name("cluster_units") val clusterUnits: Long,
            @Name("extra_storage") val extraStorage: Long,
            @Name("cluster_class") val clusterClass: String
    )

    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "common.queries:cluster_data")
    data class ClusterDataV33(
            @Name("name") val name: String,
            @Name("governor") val governor: String,
            @Name("is_operational") val isOperational: Boolean,
            @Name("cluster_units") @Nullable val clusterUnits: Long?,
            @Name("extra_storage") @Nullable val extraStorage: Long?,
            @Name("number_of_nodes") @Nullable val numberOfNodes: Long?,
            @Name("container_units_available") @Nullable val containerUnitsAvailable: Long?,
            @Name("extra_storage_available") @Nullable val extraStorageAvailable: Long?,
            @Name("cluster_class") @Nullable val clusterClass: String?
    )

    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "direct_cluster:create_cluster_with_cluster_data")
    fun TransactionBuilder.createClusterWithClusterDataOperationV33(myPubkey: ByteArray,
                                                                    name: String,
                                                                    governorVoterSet: String,
                                                                    providerPubkeys: List<ByteArray>,
                                                                    clusterCreationData: ClusterCreationDataV33) =
            addOperation("create_cluster_with_cluster_data", GtvFactory.gtv(myPubkey),
                    GtvFactory.gtv(name),
                    GtvFactory.gtv(governorVoterSet),
                    GtvFactory.gtv(providerPubkeys.map { GtvFactory.gtv(it) }),
                    GtvObjectMapper.toGtvArray(clusterCreationData))

    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "direct_cluster:create_cluster_from_with_cluster_data")
    fun TransactionBuilder.createClusterFromWithClusterDataOperationV33(myPubkey: ByteArray,
                                                                        name: String,
                                                                        governorVoterSet: String,
                                                                        providerVoterSet: String,
                                                                        clusterCreationData: ClusterCreationDataV33) =
            addOperation("create_cluster_from_with_cluster_data", GtvFactory.gtv(myPubkey),
                    GtvFactory.gtv(name),
                    GtvFactory.gtv(governorVoterSet),
                    GtvFactory.gtv(providerVoterSet),
                    GtvObjectMapper.toGtvArray(clusterCreationData))

    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "common.queries:get_cluster_data")
    fun PostchainQuery.getClusterDataV33(name: String) =
            query("get_cluster_data", GtvFactory.gtv(mapOf("name" to GtvFactory.gtv(name)))).toObject<ClusterDataV33>()
}