package net.postchain.mc.compatibility

import net.postchain.client.core.PostchainQuery
import net.postchain.client.transaction.TransactionBuilder
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.GtvNull
import net.postchain.gtv.mapper.Name
import net.postchain.gtv.mapper.toObject
import javax.annotation.processing.Generated

object ApiCompatECV66 {
    const val CREATE_TAG = "create_tag"
    /**
     * Operation economy_chain:create_tag
     *
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain:create_tag")
    fun TransactionBuilder.createTagOperationECV66(myPubkey: ByteArray,
                                                   name: String,
                                                   scuPrice: Long,
                                                   extraStoragePrice: Long) =
            addOperation(CREATE_TAG, gtv(myPubkey),
                    gtv(name),
                    gtv(scuPrice),
                    gtv(extraStoragePrice))


    const val UPDATE_TAG = "update_tag"
    /**
     * Operation economy_chain:update_tag
     *
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain:update_tag")
    fun TransactionBuilder.updateTagOperationECV66(myPubkey: ByteArray,
                                                   name: String,
                                                   scuPrice: Long?,
                                                   extraStoragePrice: Long?) =
            addOperation(UPDATE_TAG, gtv(myPubkey),
                    gtv(name),
                    scuPrice.let { if (it == null) GtvNull else gtv(it) },
                    extraStoragePrice.let { if (it == null) GtvNull else gtv(it) })


    /**
     * Struct economy_chain:tag_data
     *
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain:tag_data")
    data class TagDataECV66(
            @Name("name") val name: String,
            @Name("scu_price") val scuPrice: Long,
            @Name("extra_storage_price") val extraStoragePrice: Long
    )

    const val GET_TAGS = "get_tags"
    /**
     * Query economy_chain:get_tags
     *
     * Get a list of all tags.
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain:get_tags")
    fun PostchainQuery.getTagsECV66() =
            query(GET_TAGS, gtv(mapOf())).asArray().map { v1 -> v1.toObject<TagDataECV66>() }
}
