package net.postchain.mc.compatibility

import net.postchain.client.core.PostchainQuery
import net.postchain.common.BlockchainRid
import net.postchain.common.types.RowId
import net.postchain.common.types.WrappedByteArray
import net.postchain.gtv.GtvFactory
import net.postchain.gtv.GtvNull
import net.postchain.gtv.mapper.Name
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
}