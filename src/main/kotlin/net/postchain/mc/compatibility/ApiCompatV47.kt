package net.postchain.mc.compatibility

import net.postchain.client.core.PostchainQuery
import net.postchain.common.types.WrappedByteArray
import net.postchain.crypto.PubKey
import net.postchain.gtv.Gtv
import net.postchain.gtv.GtvFactory
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.mapper.Name
import net.postchain.gtv.mapper.ToGtv
import net.postchain.gtv.mapper.toObject
import javax.annotation.processing.Generated

object ApiCompatV47 {

    /**
     * Query common.queries:get_all_providers
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "common.queries:get_all_providers")
    fun PostchainQuery.getAllProviders47() =
            query("get_all_providers", gtv(mapOf())).asArray().map { v1 -> v1.toObject<Provider47>() }


    /**
     * Query common.queries:get_provider_data
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "common.queries:get_provider_data")
    fun PostchainQuery.getProviderData47(pubkey: PubKey) =
            query("get_provider_data", gtv(mapOf("pubkey" to gtv(pubkey.data)))).toObject<Provider47>()

    /**
     * Entity model:provider
     *
     * Rell entity is typically encoded as a GtvInteger. If used as struct<model:provider>, then GtvObjectMapper.toGtvArray() is used for encoding.
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "model:provider")
    data class Provider47(
            @Name("pubkey") val pubkey: WrappedByteArray,
            @Name("name") val name: String,
            @Name("url") val url: String,
            @Name("active") val active: Boolean,
            @Name("tier") val tier: ProviderTier47,
            @Name("system") val system: Boolean
    )

    /**
     * Enum model:provider_tier
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "model:provider_tier")
    enum class ProviderTier47 : ToGtv {
        COMMUNITY_NODE_PROVIDER,
        NODE_PROVIDER;

        override fun toGtv(): Gtv = GtvFactory.gtv(ordinal.toLong())
    }

}