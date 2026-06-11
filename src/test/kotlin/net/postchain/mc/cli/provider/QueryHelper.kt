package net.postchain.mc.cli.provider

import net.postchain.chain0.model.Provider
import net.postchain.chain0.model.ProviderTier
import net.postchain.common.hexStringToByteArray
import net.postchain.common.types.WrappedByteArray
import net.postchain.common.wrap
import net.postchain.gtv.GtvArray
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.mc.cli.test_helpers.DEFAULT_PROVIDER01_PUBKEY
import net.postchain.mc.cli.test_helpers.DEFAULT_PROVIDER02_PUBKEY
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi

fun buildGetAllProvidersResponse(): GtvArray {
    return gtv(listOf(
            Provider(
                    DEFAULT_PROVIDER01_PUBKEY.hexStringToByteArray().wrap(),
                    "provider01",
                    "http://provider01:7740",
                    true,
                    ProviderTier.NODE_PROVIDER,
                    true
            ),
            Provider(
                    DEFAULT_PROVIDER02_PUBKEY.hexStringToByteArray().wrap(),
                    "provider02",
                    "http://provider02:7740",
                    true,
                    ProviderTier.NODE_PROVIDER,
                    true
            )
    ).map(GtvObjectMapper::toGtvDictionary))
}

const val PROVIDER03_PUBKEY = "021111111111111111111111111111111111111111111111111111111111111111"

fun buildMixedGetAllProvidersResponse(): GtvArray {
    return gtv(listOf(
            Provider(
                    DEFAULT_PROVIDER01_PUBKEY.hexStringToByteArray().wrap(),
                    "provider01",
                    "http://provider01:7740",
                    true,
                    ProviderTier.NODE_PROVIDER,
                    true
            ),
            Provider(
                    DEFAULT_PROVIDER02_PUBKEY.hexStringToByteArray().wrap(),
                    "provider02",
                    "http://provider02:7740",
                    true,
                    ProviderTier.NODE_PROVIDER,
                    false
            ),
            Provider(
                    PROVIDER03_PUBKEY.hexStringToByteArray().wrap(),
                    "provider03",
                    "http://provider03:7740",
                    true,
                    ProviderTier.DAPP_PROVIDER,
                    false
            )
    ).map(GtvObjectMapper::toGtvDictionary))
}

fun ManagedRestTestApi.addDcGetProviderData(): ManagedRestTestApi {
    withDCQuery("get_provider_data", GtvObjectMapper.toGtvDictionary(Provider(
            WrappedByteArray(34),
            "provider-name",
            "https://localhost:7740",
            true,
            ProviderTier.NODE_PROVIDER,
            true
    )))
    return this
}
