package net.postchain.mc.cli.base

import com.google.gson.JsonParser
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

const val DIRECTORY_CHAIN_ECONOMY_CHAIN_VERSION = 30L // The version of directory chain introducing economy chain

const val CLUSTER_ANCHORING_CHAIN_RESOURCE_USAGE_VERSION = 3L

const val ECONOMY_CHAIN_MAX_CLUSTER_NODES_VERSION = 63L
const val ECONOMY_CHAIN_DYNAMIC_STAKING_REWARD_SHARE_VERSION = 64L
const val ECONOMY_CHAIN_JAR_EXTENSIONS = 65L
const val ECONOMY_CHAIN_TAG_COMPUTE_REQUEST_PRICE_VERSION = 67L

/**
 * Checks for the latest version of pmc from GitLab container registry
 */
object VersionChecker {
    private const val PMC_PROJECT_ID = "46346037"
    private const val GITLAB_API_URL = "https://gitlab.com/api/v4"
    private const val TIMEOUT_SECONDS = 3L

    fun fetchLatestVersion(): String? {
        return try {
            val client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .build()

            val request = HttpRequest.newBuilder()
                    .uri(URI.create("${GITLAB_API_URL}/projects/${PMC_PROJECT_ID}/repository/tags"))
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .GET()
                    .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() == 200) {
                // Parse JSON and get the first tag name
                val jsonArray = JsonParser.parseString(response.body()).asJsonArray
                if (jsonArray.size() >= 1) {
                    jsonArray[0].asJsonObject.get("name")?.asString
                } else {
                    null
                }
            } else {
                null
            }
        } catch (_: Exception) {
            // Silently fail on any network/parsing errors
            null
        }
    }

    fun compareVersions(current: String, latest: String): Int {
        try {
            val currentVersionParts = current.split(".").map { it.toIntOrNull() ?: 0 }
            val latestVersionParts = latest.split(".").map { it.toIntOrNull() ?: 0 }

            val maxLength = maxOf(currentVersionParts.size, latestVersionParts.size)

            for (i in 0 until maxLength) {
                val currentPart = currentVersionParts.getOrElse(i) { 0 }
                val latestPart = latestVersionParts.getOrElse(i) { 0 }

                if (currentPart != latestPart) {
                    return currentPart - latestPart
                }
            }

            return 0
        } catch (_: Exception) {
            // If parsing fails, assume outdated
            return -1
        }
    }

    fun checkAndWarnIfOutdated(currentVersion: String) {
        try {
            if ((System.getenv("PMC_SKIP_VERSION_CHECK") ?: "false").toBoolean()) {
                return
            }

            // no need to check version if dev or unknow
            if (currentVersion == "dev" || currentVersion == "(unknown)") {
                return
            }

            val latestVersion = fetchLatestVersion()
            if (latestVersion != null && compareVersions(currentVersion, latestVersion) < 0) {
                System.err.println(
                        "[WARN] You are using version $currentVersion, the latest version is $latestVersion"
                )
            }
        } catch (_: Exception) {
            // Silently ignore errors
        }
    }

}
