package net.postchain.mc.compatibility

import net.postchain.client.transaction.TransactionBuilder
import net.postchain.gtv.GtvFactory.gtv
import javax.annotation.processing.Generated

object ApiCompatECV62 {

    const val CREATE_CLUSTER = "create_cluster"

    /**
     * Operation economy_chain:create_cluster
     *
     *
     * @param extraStorage MiB
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain:create_cluster")
    fun TransactionBuilder.createClusterOperationV62(myPubkey: ByteArray,
                                                     name: String,
                                                     governorVoterSetName: String,
                                                     voterSetName: String,
                                                     clusterUnits: Long,
                                                     extraStorage: Long,
                                                     tagName: String,
                                                     containerUnitCpu: Long,
                                                     containerUnitRam: Long,
                                                     containerUnitIoRead: Long,
                                                     containerUnitIoWrite: Long,
                                                     containerUnitStorage: Long,
                                                     systemContainerUnits: Long) =
            addOperation(CREATE_CLUSTER, gtv(myPubkey),
                    gtv(name),
                    gtv(governorVoterSetName),
                    gtv(voterSetName),
                    gtv(clusterUnits),
                    gtv(extraStorage),
                    gtv(tagName),
                    gtv(containerUnitCpu),
                    gtv(containerUnitRam),
                    gtv(containerUnitIoRead),
                    gtv(containerUnitIoWrite),
                    gtv(containerUnitStorage),
                    gtv(systemContainerUnits))
}
