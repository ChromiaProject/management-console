package net.postchain.mc.compatibility

import net.postchain.client.transaction.TransactionBuilder
import net.postchain.gtv.GtvFactory.gtv
import javax.annotation.processing.Generated

object ApiCompatECV56 {

    const val CREATE_CLUSTER = "create_cluster"
    /**
     * Operation economy_chain:create_cluster
     *
     *
     * @param extraStorage MiB
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain:create_cluster")
    fun TransactionBuilder.createClusterOperationV56(name: String,
                                                  governorVoterSetName: String,
                                                  voterSetName: String,
                                                  clusterUnits: Long,
                                                  extraStorage: Long,
                                                  tagName: String) =
            addOperation(CREATE_CLUSTER, gtv(name),
                    gtv(governorVoterSetName),
                    gtv(voterSetName),
                    gtv(clusterUnits),
                    gtv(extraStorage),
                    gtv(tagName))
}