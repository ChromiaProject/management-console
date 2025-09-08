package net.postchain.mc.compatibility

import net.postchain.client.transaction.TransactionBuilder
import net.postchain.economy.economy_chain.CREATE_CONTAINER_WITH_SUBNODE_IMAGE
import net.postchain.economy.economy_chain.UPGRADE_CONTAINER
import net.postchain.gtv.GtvFactory.gtv
import javax.annotation.processing.Generated

object ApiCompatECV59 {

    /**
     * Operation economy_chain:create_container_with_subnode_image
     *
     * Request creation of a container with custom subnode image.
     *
     * If enough tokens are available on the users account an ICMF message will be sent to DC for creation of the container.
     * A lease is setup for the request amount of weeks and the cost is deducted from the users account.
     * @param providerPubkey pubkey of provider
     * @param containerUnits number of container units
     * @param durationWeeks lease duration in weeks
     * @param extraStorageGib extra storage in GiB
     * @param clusterName cluster name
     * @param subnodeImageName subnode image to use, or "" to use default subnode image
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain:create_container_with_subnode_image")
    fun TransactionBuilder.createContainerWithSubnodeImageOperationV59(providerPubkey: ByteArray,
    	containerUnits: Long,
    	durationWeeks: Long,
    	extraStorageGib: Long,
    	clusterName: String,
    	autoRenew: Boolean,
    	subnodeImageName: String) =
       addOperation(CREATE_CONTAINER_WITH_SUBNODE_IMAGE, gtv(providerPubkey),
    	gtv(containerUnits),
    	gtv(durationWeeks),
    	gtv(extraStorageGib),
    	gtv(clusterName),
    	gtv(autoRenew),
    	gtv(subnodeImageName))

    /**
     * Operation economy_chain:upgrade_container
     *
     * If possible upgrade (or downgrade) the container to requested specifications.
     *
     * `warning:` Please note that reducing the storage only works as long as the container still has at least
     * 20% of free space (or what is configured in `container_reduce_space_margin_requirement`).
     * @param upgradedContainerUnits The new number of container units for this container. Can both be increased and reduced.
     * @param upgradedExtraStorageGib The new size of extra storage.
     * @param upgradedClusterName Move this container to a new cluster.
     * @param upgradedDurationWeeks Duration for the new upgraded lease
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain:upgrade_container")
    fun TransactionBuilder.upgradeContainerOperationV59(containerName: String,
    	upgradedContainerUnits: Long,
    	upgradedExtraStorageGib: Long,
    	upgradedClusterName: String,
    	upgradedDurationWeeks: Long) =
       addOperation(UPGRADE_CONTAINER, gtv(containerName),
    	gtv(upgradedContainerUnits),
    	gtv(upgradedExtraStorageGib),
    	gtv(upgradedClusterName),
    	gtv(upgradedDurationWeeks))

}
