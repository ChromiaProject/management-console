package net.postchain.mc.cli.base

const val NAME_LENGTH = 10
const val NAME_LENGTH_MAX = 64
const val RID_LENGTH = 64
const val PUBKEY_LENGTH = 66
const val METADATA_LENGTH_MAX = 1_000
const val URL_LENGTH_MAX = 1_000
const val HOST_NAME_LENGTH_MAX = 255

object CommandBase {

    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    fun autoGenerateName(): String = (1..NAME_LENGTH)
            .map { charPool.random() }
            .joinToString("")

    fun isEntityNameValid(string: String): Boolean {
        val regex = "^[a-zA-Z0-9_]*$"
        return string.matches(regex.toRegex())
    }
}