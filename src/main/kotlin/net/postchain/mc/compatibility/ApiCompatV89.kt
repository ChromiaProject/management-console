package net.postchain.mc.compatibility

import net.postchain.client.transaction.TransactionBuilder
import net.postchain.gtv.GtvFactory.gtv
import javax.annotation.processing.Generated

object ApiCompatV89 {
	const val CREATE_CONTAINER_WITH_UNITS = "create_container_with_units"
    /**
     * Operation direct_container:create_container_with_units
     *
     *
     * @param consensusThreshold majority (-1), super majority (0) or custom (1, ...)
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "direct_container:create_container_with_units")
    fun TransactionBuilder.createContainerWithUnitsOperationV89(myPubkey: ByteArray,
    	name: String,
    	clusterName: String,
    	consensusThreshold: Long,
    	deployers: List<ByteArray>,
    	containerUnits: Long) =
       addOperation(CREATE_CONTAINER_WITH_UNITS, gtv(myPubkey),
    	gtv(name),
    	gtv(clusterName),
    	gtv(consensusThreshold),
    	gtv(deployers.map { gtv(it) }),
    	gtv(containerUnits))

	const val CREATE_CONTAINER_FROM_WITH_UNITS = "create_container_from_with_units"
    /**
     * Operation direct_container:create_container_from_with_units
     *
     *
     * @param consensusThreshold majority (-1), super majority (0) or custom (1, ...)
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "direct_container:create_container_from_with_units")
    fun TransactionBuilder.createContainerFromWithUnitsOperationV89(myPubkey: ByteArray,
    	name: String,
    	clusterName: String,
    	consensusThreshold: Long,
    	voterSetName: String,
    	containerUnits: Long) =
       addOperation(CREATE_CONTAINER_FROM_WITH_UNITS, gtv(myPubkey),
    	gtv(name),
    	gtv(clusterName),
    	gtv(consensusThreshold),
    	gtv(voterSetName),
    	gtv(containerUnits))
}
