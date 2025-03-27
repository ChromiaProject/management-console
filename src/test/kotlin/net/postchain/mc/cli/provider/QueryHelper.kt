package net.postchain.mc.cli.provider

import net.postchain.chain0.model.Provider
import net.postchain.chain0.model.ProviderTier
import net.postchain.common.hexStringToByteArray
import net.postchain.common.wrap
import net.postchain.gtv.GtvArray
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.mc.cli.test_helpers.DEFAULT_PROVIDER01_PUBKEY
import net.postchain.mc.cli.test_helpers.DEFAULT_PROVIDER02_PUBKEY

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
