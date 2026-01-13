package net.postchain.mc.cli.image

import net.postchain.chain0.common.queries.SubnodeImageData
import net.postchain.chain0.model.SubnodeImageType
import net.postchain.common.wrap
import net.postchain.gtv.GtvArray
import net.postchain.gtv.GtvDictionary
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.mapper.GtvObjectMapper

fun buildGetSubnodeImagesResponse(
        url: String = "gitlab.com/....",
): GtvArray {
    return gtv(listOf(SubnodeImageData(
            "image01",
            url,
            "sha256:abcdef123",
            SubnodeImageType.COMMON,
            ByteArray(0).wrap(),
            true,
            "Description of image01",
            "gtx_modules01",
            "sync_ext01",
            baseComputeRequests = 0,
            "native_func01"
    )).map(GtvObjectMapper::toGtvDictionary))
}

fun buildGetSubnodeImageResponse(
        url: String = "gitlab.com/....",
): GtvDictionary {
    return GtvObjectMapper.toGtvDictionary(SubnodeImageData(
            "image01",
            url,
            "sha256:abcdef123",
            SubnodeImageType.COMMON,
            ByteArray(0).wrap(),
            true,
            "Description of image01",
            "gtx_modules01",
            "sync_ext01",
            baseComputeRequests = 0,
            "native_func01"
    ))
}
