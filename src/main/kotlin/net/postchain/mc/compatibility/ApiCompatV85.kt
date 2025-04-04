package net.postchain.mc.compatibility

import net.postchain.chain0.model.SubnodeImageType
import net.postchain.client.transaction.TransactionBuilder
import net.postchain.gtv.GtvFactory.gtv
import javax.annotation.processing.Generated

object ApiCompatV85 {
    private const val PROPOSE_SUBNODE_IMAGE_V85 = "propose_subnode_image"
    private const val PROPOSE_UPDATE_SUBNODE_IMAGE_V85 = "propose_update_subnode_image"
    private const val PROPOSE_SUBNODE_IMAGE_STATE_V85 = "propose_subnode_image_state"
    private const val PROPOSE_ADD_CLUSTER_SUBNODE_IMAGE_V85 = "propose_add_cluster_subnode_image"
    private const val PROPOSE_REMOVE_CLUSTER_SUBNODE_IMAGE_V85 = "propose_remove_cluster_subnode_image"

    /**
     * Operation proposal_subnode_image:propose_subnode_image
     *
     * Propose a new subnode image.
     *
     * Permission: container deployer
     *
     * Rate limit: actions
     * @param providerPubkey pubkey of provider
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "proposal_subnode_image:propose_subnode_image")
    fun TransactionBuilder.proposeSubnodeImageOperationV85(
        providerPubkey: ByteArray,
        name: String,
        url: String,
        digest: String,
        type: SubnodeImageType,
        imageDescription: String,
        gtxModules: String,
        syncExts: String,
        description: String
    ) =
        addOperation(
            PROPOSE_SUBNODE_IMAGE_V85, gtv(providerPubkey),
            gtv(name),
            gtv(url),
            gtv(digest),
            gtv(type.name),
            gtv(imageDescription),
            gtv(gtxModules),
            gtv(syncExts),
            gtv(description)
        )

    /**
     * Operation proposal_subnode_image:propose_update_subnode_image
     *
     * Propose an update to a subnode image.
     *
     * Permission: container deployer
     *
     * Rate limit: actions
     * @param providerPubkey pubkey of provider
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "proposal_subnode_image:propose_update_subnode_image")
    fun TransactionBuilder.proposeUpdateSubnodeImageOperationV85(
        providerPubkey: ByteArray,
        name: String,
        url: String,
        digest: String,
        description: String
    ) =
        addOperation(
            PROPOSE_UPDATE_SUBNODE_IMAGE_V85, gtv(providerPubkey),
            gtv(name),
            gtv(url),
            gtv(digest),
            gtv(description)
        )

    /**
     * Operation proposal_subnode_image:propose_subnode_image_state
     *
     * Propose enabling or disabling a subnode image.
     *
     * Permission: container deployer
     *
     * Rate limit: actions
     * @param providerPubkey pubkey of provider
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "proposal_subnode_image:propose_subnode_image_state")
    fun TransactionBuilder.proposeSubnodeImageStateOperationV85(
        providerPubkey: ByteArray,
        name: String,
        state: Boolean,
        description: String
    ) =
        addOperation(
            PROPOSE_SUBNODE_IMAGE_STATE_V85, gtv(providerPubkey),
            gtv(name),
            gtv(state),
            gtv(description)
        )

    /**
     * Operation proposal_subnode_image:propose_add_cluster_subnode_image
     *
     * Propose adding a subnode image to a cluster.
     *
     * Permission: container deployer
     *
     * Rate limit: actions
     * @param providerPubkey pubkey of provider
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "proposal_subnode_image:propose_add_cluster_subnode_image")
    fun TransactionBuilder.proposeAddClusterSubnodeImageOperationV85(
        providerPubkey: ByteArray,
        clusterName: String,
        subnodeImageName: String,
        description: String
    ) =
        addOperation(
            PROPOSE_ADD_CLUSTER_SUBNODE_IMAGE_V85, gtv(providerPubkey),
            gtv(clusterName),
            gtv(subnodeImageName),
            gtv(description)
        )

    /**
     * Operation proposal_subnode_image:propose_remove_cluster_subnode_image
     *
     * Propose removing a subnode image from a cluster.
     *
     * Permission: container deployer
     *
     * Rate limit: actions
     * @param providerPubkey pubkey of provider
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "proposal_subnode_image:propose_remove_cluster_subnode_image")
    fun TransactionBuilder.proposeRemoveClusterSubnodeImageOperationV85(
        providerPubkey: ByteArray,
        clusterName: String,
        subnodeImageName: String,
        description: String
    ) =
        addOperation(
            PROPOSE_REMOVE_CLUSTER_SUBNODE_IMAGE_V85, gtv(providerPubkey),
            gtv(clusterName),
            gtv(subnodeImageName),
            gtv(description)
        )
}