package net.postchain.mc.cli.base

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isLessThan
import assertk.assertions.isGreaterThan
import org.junit.jupiter.api.Test

class VersionCheckerTest {

    @Test
    fun `compareVersions should return 0 for equal versions`() {
        assertThat(VersionChecker.compareVersions("3.28.1", "3.28.1")).isEqualTo(0)
        assertThat(VersionChecker.compareVersions("1.0.0", "1.0.0")).isEqualTo(0)
    }

    @Test
    fun `compareVersions should return negative when current is less than latest`() {
        assertThat(VersionChecker.compareVersions("3.28.0", "3.28.1")).isLessThan(0)
        assertThat(VersionChecker.compareVersions("3.27.1", "3.28.1")).isLessThan(0)
        assertThat(VersionChecker.compareVersions("2.28.1", "3.28.1")).isLessThan(0)
        assertThat(VersionChecker.compareVersions("3.28", "3.28.1")).isLessThan(0)
    }

    @Test
    fun `compareVersions should return positive when current is greater than latest`() {
        assertThat(VersionChecker.compareVersions("3.28.2", "3.28.1")).isGreaterThan(0)
        assertThat(VersionChecker.compareVersions("3.29.0", "3.28.1")).isGreaterThan(0)
        assertThat(VersionChecker.compareVersions("4.0.0", "3.28.1")).isGreaterThan(0)
        assertThat(VersionChecker.compareVersions("3.28.1", "3.28")).isGreaterThan(0)
    }

    @Test
    fun `compareVersions should handle dev version`() {
        assertThat(VersionChecker.compareVersions("dev", "3.28.1")).isLessThan(0)
        assertThat(VersionChecker.compareVersions("(unknown)", "3.28.1")).isLessThan(0)
    }

    @Test
    fun `compareVersions should handle versions with different number of parts`() {
        assertThat(VersionChecker.compareVersions("3.28", "3.28.0")).isEqualTo(0)
        assertThat(VersionChecker.compareVersions("3.28.0", "3.28")).isEqualTo(0)
        assertThat(VersionChecker.compareVersions("3.28", "3.28.1")).isLessThan(0)
    }
}
