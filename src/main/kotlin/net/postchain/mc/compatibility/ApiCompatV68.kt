package net.postchain.mc.compatibility

import net.postchain.client.core.PostchainQuery
import net.postchain.common.BlockchainRid
import net.postchain.common.types.RowId
import net.postchain.common.types.WrappedByteArray
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.mapper.Name
import net.postchain.gtv.mapper.toObject
import javax.annotation.processing.Generated

object ApiCompatV68 {

    const val GET_FORCED_CONFIGURATION_V68 = "get_forced_configuration"

    /**
     * Struct proposal_blockchain:forced_configuration_data
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "proposal_blockchain:forced_configuration_data")
    data class ForcedConfigurationDataV68(
            @Name("blockchain") val blockchain: RowId,
            @Name("height") val height: Long,
            @Name("config_hash") val configHash: WrappedByteArray
    )

    /**
     * Query proposal_blockchain:get_forced_configuration
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "proposal_blockchain:get_forced_configuration")
    fun PostchainQuery.getForcedConfigurationV68(blockchainRid: BlockchainRid) =
            query(GET_FORCED_CONFIGURATION_V68, gtv(mapOf("blockchain_rid" to gtv(blockchainRid)))).asArray().map { v1 -> v1.toObject<ForcedConfigurationDataV68>() }

}
