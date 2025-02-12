package net.postchain.mc.cli.economy

import com.chromia.build.tools.restapi.TestModel
import net.postchain.api.rest.controller.Model
import net.postchain.api.rest.model.ApiStatus
import net.postchain.api.rest.model.TxRid
import net.postchain.common.BlockchainRid
import net.postchain.common.tx.TransactionStatus
import net.postchain.economy.economy_chain.EconomyConstantsData
import net.postchain.economy.economy_chain.TagData
import net.postchain.gtv.GtvArray
import net.postchain.gtv.GtvFactory
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.gtx.GtxQuery
import net.postchain.mc.cli.BaseTransactionHandler
import net.postchain.mc.compatibility.ApiCompatECV45
import java.math.BigDecimal

class ECTestModel(val model: Model, val ecVersion: Long) : Model by model, BaseTransactionHandler() {

    constructor(blockchainRid: BlockchainRid, ecVersion: Long) : this(TestModel(blockchainRid), ecVersion)

    override fun query(query: GtxQuery) = when (query.name) {
        "api_version" -> GtvFactory.gtv(ecVersion)
        "get_tags" -> {
            GtvArray(arrayOf(GtvObjectMapper.toGtvDictionary(TagData("t1", 1, 2))))
        }
        "get_economy_constants" -> {
            if (ecVersion < ECONOMY_CHAIN_STAKING_REQ_NODE_BASED_VERSION) {
                GtvObjectMapper.toGtvDictionary(ApiCompatECV45.EconomyConstantsDataV45(
                        minLeaseTimeWeeks = 1,
                        maxLeaseTimeWeeks = 10,
                        chrPerUsd = "5.1".toBigDecimal(),
                        totalCostSystemProviders = 800123456,
                        systemProviderFeeShare = "0.1".toBigDecimal(),
                        stakingRewardRate = "0.2".toBigDecimal(),
                        stakingRewardFeeShare = "0.3".toBigDecimal(),
                        chromiaFoundationFeeShare = "0.4".toBigDecimal(),
                        resourcePoolMarginFeeShare = "0.5".toBigDecimal(),
                        systemProviderRiskShare = "0.6".toBigDecimal(),
                        dappProviderRiskShare = "0.7".toBigDecimal(),
                        stakingRequirementsEnabled = true,
                        stakingRequirementStopPayoutDays = 14,
                        stakingRequirementSystemProviderOwnStakeChr = 1123456,
                        stakingRequirementSystemProviderTotalStakeChr = 2123456,
                        stakingRequirementDappProviderOwnStakeChr = 3123456,
                        stakingRequirementDappProviderTotalStakeChr = 4123456,
                        bridgeLeaseAddMinBalanceUsd = BigDecimal("100"),
                ))
            } else {
                GtvObjectMapper.toGtvDictionary(EconomyConstantsData(
                        minLeaseTimeWeeks = 1,
                        maxLeaseTimeWeeks = 10,
                        chrPerUsd = "5.1".toBigDecimal(),
                        totalCostSystemProviders = 800123456,
                        systemProviderFeeShare = "0.1".toBigDecimal(),
                        stakingRewardRate = "0.2".toBigDecimal(),
                        stakingRewardFeeShare = "0.3".toBigDecimal(),
                        chromiaFoundationFeeShare = "0.4".toBigDecimal(),
                        resourcePoolMarginFeeShare = "0.5".toBigDecimal(),
                        systemProviderRiskShare = "0.6".toBigDecimal(),
                        dappProviderRiskShare = "0.7".toBigDecimal(),
                        stakingRequirementsEnabled = true,
                        stakingRequirementStopPayoutDays = 14,
                        stakingRequirementSystemNodeOwnStakeChr = 1123456,
                        stakingRequirementSystemNodeTotalStakeChr = 2123456,
                        stakingRequirementDappNodeOwnStakeChr = 3123456,
                        stakingRequirementDappNodeTotalStakeChr = 4123456,
                        bridgeLeaseAddMinBalanceUsd = BigDecimal("100"),
                ))
            }
        }
        else -> throw IllegalArgumentException("Query not found: ${query.name}")
    }

    override fun postTransaction(tx: ByteArray) {
        postTransactionImpl(tx)
    }

    override fun getStatus(txRID: TxRid): ApiStatus {
        return ApiStatus(TransactionStatus.CONFIRMED)
    }

}