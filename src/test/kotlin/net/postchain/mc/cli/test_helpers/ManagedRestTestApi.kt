package net.postchain.mc.cli.test_helpers

import com.chromia.build.tools.restapi.TestModel
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.testing.CliktCommandTestResult
import net.postchain.api.rest.controller.Model
import net.postchain.api.rest.controller.RestApi
import net.postchain.common.BlockchainRid
import net.postchain.common.exception.ProgrammerMistake
import net.postchain.gtv.Gtv
import net.postchain.gtx.GtxQuery
import java.io.File
import java.nio.file.Path

/**
 * Mocks a Postchain Rest server with any set of models attached. DC/EC is added by default with
 * basic queries.
 */
open class ManagedRestTestApi(
        val dir: Path,
        dcVersion: Long = Long.MAX_VALUE,
        ecVersion: Long = Long.MAX_VALUE,
        val models: MutableMap<BlockchainRid, RestTestModel> = mutableMapOf(),
        val pubKey: String = DEFAULT_PROVIDER_PUBKEY,
        val privKey: String = DEFAULT_PROVIDER_PRIVKEY,
        val providerPubKey: String? = null,
        val keyId: String? = null,
) {

    companion object {
        val DEFAULT_PROVIDER_PUBKEY = DEFAULT_PROVIDER01_PUBKEY
        val DEFAULT_PROVIDER_PRIVKEY = DEFAULT_PROVIDER01_PRIVKEY
    }

    var dcBcRid = DEFAULT_BRID_DIRECTORY_CHAIN
    var sacBcRid = DEFAULT_BRID_SYSTEM_ANCHORING_CHAIN
    var cacBcRid = DEFAULT_BRID_CLUSTER_ANCHORING_CHAIN
    var ecBcRid = DEFAULT_BRID_ECONOMY_CHAIN

    lateinit var apiUrl: String
    private lateinit var afterServerBeforeTestStep: (ManagedRestTestApi) -> Unit

    private val extraModels = mutableMapOf<Pair<BlockchainRid, String>, Model>()

    init {
        models[dcBcRid] = D1TestModel(
                TestModel(dcBcRid), ecBcRid.data, dcVersion = dcVersion, sacBcRid = sacBcRid,
                providerByKey = pubKey
        )
        models[sacBcRid] = RestTestModel(1, TestModel(sacBcRid))
        models[cacBcRid] = RestTestModel(2, TestModel(cacBcRid))
        models[ecBcRid] = ECTestModel(ecBcRid, ecVersion)
    }

    /**
     * Starts server, attach models, creates client config and call runTests. Shutdown everything after runTests.
     */
    open fun test(runTests: (ManagedRestTestApi) -> Unit) {
        RestApi(0, "", gracefulShutdown = false).use {
            apiUrl = "http://localhost:${it.server.port()}"
            models.forEach { (rid, model) -> it.attachModel(rid, model) }
            extraModels.forEach { (ridContainer, model) ->
                it.attachModel(ridContainer.first, model, container = ridContainer.second)
            }
            val dcModel = models[dcBcRid]
            if (dcModel is D1TestModel) {
                dcModel.setBlockchainApiUrlsByQuery(apiUrl)
            }
            with(File(dir.toFile(), ".chromia/config")) {
                parentFile.mkdirs()
                writeText("""
                    api.url = $apiUrl
                    ${if (providerPubKey != null) "provider.pubkey=$providerPubKey" else ""}
                    ${if (keyId != null) "key.id=$keyId" else ""}
                    pubkey=$pubKey
                    privkey=$privKey
                    brid=$dcBcRid
                    """.trimIndent())
            }

            if (::afterServerBeforeTestStep.isInitialized) {
                afterServerBeforeTestStep(this)
            }

            runTests(this)
        }
    }

    /**
     * Run block after the Rest API server is started but before the test block is executed. This is useful
     * in case you need to add queries that returns the node api url which is available in this context.
     */
    open fun afterServerBeforeTest(function: (ManagedRestTestApi) -> Unit): ManagedRestTestApi {
        afterServerBeforeTestStep = function
        return this
    }

    /**
     * Once the test REST Api is started, run the command with arguments. This command is not run in a separate process.
     */
    open fun testCommand(command: CliktCommand, vararg args: String, function: (CliktCommandTestResult, ManagedRestTestApi) -> Unit) {
        test {
            val result = testPmcCommand(dir, command, *args)
            function(result, this)
        }
    }

    fun getModel(blockchainRid: BlockchainRid): RestTestModel {
        return models[blockchainRid] ?: throw ProgrammerMistake("Blockahin $blockchainRid not found")
    }

    fun getDcModel() = getModel(dcBcRid)
    fun getEcModel() = getModel(ecBcRid)
    fun getSacModel() = getModel(sacBcRid)
    fun getCacModel() = getModel(cacBcRid)

    fun withExtraModel(blockchainRid: BlockchainRid, container: String, model: Model): ManagedRestTestApi {
        extraModels[blockchainRid to container] = model
        return this
    }

    // Manipulate a test model
    fun withModel(blockchainRid: BlockchainRid, function: (RestTestModel) -> Unit): ManagedRestTestApi {
        function(models[blockchainRid]!!)
        return this
    }

    // Manipulate the directory chain model
    fun withDCModel(function: (RestTestModel) -> Unit): ManagedRestTestApi {
        withModel(dcBcRid, function)
        return this
    }

    // Manipulate the economy chain model
    fun withECModel(function: (RestTestModel) -> Unit): ManagedRestTestApi {
        withModel(ecBcRid, function)
        return this
    }

    // Add a query response to any test chain
    fun withQuery(blockchainRid: BlockchainRid, query: String, vararg value: Gtv): ManagedRestTestApi {
        models[blockchainRid]!!.withQuery(query, *value)
        return this
    }

    // Add a query response to directory chain
    fun withDCQuery(query: String, function: (query: GtxQuery) -> Gtv): ManagedRestTestApi {
        models[dcBcRid]!!.withQuery(query, function)
        return this
    }

    // Add a query response to economy chain
    fun withECQuery(query: String, function: (query: GtxQuery) -> Gtv): ManagedRestTestApi {
        models[ecBcRid]!!.withQuery(query, function)
        return this
    }

    // Add a query response to system anchoring chain
    fun withSACQuery(query: String, vararg value: Gtv): ManagedRestTestApi {
        withQuery(sacBcRid, query, *value)
        return this
    }

    // Add a query response to cluster anchoring chain
    fun withCACQuery(query: String, vararg value: Gtv): ManagedRestTestApi {
        withQuery(cacBcRid, query, *value)
        return this
    }

    // Add a query response to directory chain
    fun withDCQuery(query: String, vararg value: Gtv): ManagedRestTestApi {
        withQuery(dcBcRid, query, *value)
        return this
    }

    // Add a query response to economy chain
    fun withECQuery(query: String, vararg value: Gtv): ManagedRestTestApi {
        withQuery(ecBcRid, query, *value)
        return this
    }
}
