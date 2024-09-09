package net.postchain.mc.cli.base

const val NAME_LENGTH = 10
const val NAME_LENGTH_MAX = 64
const val RID_LENGTH = 64
const val PUBKEY_LENGTH = 66
const val METADATA_LENGTH_MAX = 1_000
const val URL_LENGTH_MAX = 1_000
const val HOST_NAME_LENGTH_MAX = 255
const val DIGEST_LENGTH_MAX = 100

object CommandBase {

    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    private val entityNameRegex = "^[a-zA-Z0-9_]*$".toRegex()
    private val digestRegex = "^[a-zA-Z0-9:]*$".toRegex()

    fun autoGenerateName(): String = (1..NAME_LENGTH)
            .map { charPool.random() }
            .joinToString("")

    fun isEntityNameValid(string: String): Boolean = string.matches(entityNameRegex)

    fun isDigestValid(string: String): Boolean = string.matches(digestRegex)
}
