/*
 * Copyright (c) 2026 Proton Technologies AG
 * This file is part of Proton Technologies AG and Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.protonmail.android.useragent

import ch.protonmail.android.BuildConfig
import ch.protonmail.android.di.ApplicationModule
import ch.protonmail.android.useragent.model.AppBuildInfo
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GetAppVersionTest {

    private val getInstallerPackageName = mockk<GetInstallerPackageName>()

    @Test
    fun `should return plain version when prod build is installed from the play store`() {
        // Given
        every { getInstallerPackageName() } returns "com.android.vending"

        // When
        val actual = buildGetAppVersion(flavor = "prod")()

        // Then
        assertEquals("7.12.0", actual)
    }

    @Test
    fun `should append apk suffix when prod build is installed from another store`() {
        // Given
        every { getInstallerPackageName() } returns "org.fdroid.fdroid"

        // When
        val actual = buildGetAppVersion(flavor = "prod")()

        // Then
        assertEquals("7.12.0+apk", actual)
    }

    @Test
    fun `should append apk suffix when prod build has no installer`() {
        // Given
        every { getInstallerPackageName() } returns null

        // When
        val actual = buildGetAppVersion(flavor = "prod")()

        // Then
        assertEquals("7.12.0+apk", actual)
    }

    @Test
    fun `should return plain version without reading the installer when build is not prod`() {
        // Given
        val versionName = "7.12.0-alpha+fc6081a"

        // When
        val actual = buildGetAppVersion(versionName = versionName, flavor = "alpha")()

        // Then
        assertEquals(versionName, actual)
        verify(exactly = 0) { getInstallerPackageName() }
    }

    @Test
    fun `should expose the suffix alone when prod build is sideloaded`() {
        // Given
        every { getInstallerPackageName() } returns null

        // When
        val actual = buildGetAppVersion(flavor = "prod").installSourceSuffix

        // Then
        assertEquals("+apk", actual)
    }

    @Test
    fun `should read the installer only once when invoked multiple times`() {
        // Given
        every { getInstallerPackageName() } returns null
        val getAppVersion = buildGetAppVersion(flavor = "prod")

        // When
        getAppVersion()
        getAppVersion()
        getAppVersion.installSourceSuffix

        // Then
        verify(exactly = 1) { getInstallerPackageName() }
    }

    private fun buildGetAppVersion(versionName: String = "7.12.0", flavor: String) =
        GetAppVersion(AppBuildInfo(versionName = versionName, flavor = flavor), getInstallerPackageName)

    // --- Fork guards (haasanen): the version that leaves the device must be
    // upstream's own; Proton's API rejects our -hN display suffix with error
    // 2064, which kills the mail-sync event loop.
    private val forkSuffix = Regex("-h[0-9]+$")

    @Test
    fun `fork version provider feeds upstream version, never the display version`() {
        val info = ApplicationModule.provideAppBuildInfo()
        assertEquals(BuildConfig.UPSTREAM_VERSION_NAME, info.versionName)
    }

    @Test
    fun `server-facing version never carries the fork build suffix`() {
        val version = ApplicationModule.provideAppBuildInfo().versionName
        assertFalse(
            forkSuffix.containsMatchIn(version),
            "AppBuildInfo.versionName must not carry the fork -hN suffix " +
                "(Proton API error 2064 breaks mail sync): got '$version'"
        )
    }

    @Test
    fun `upstream version is a plain semver the API can parse`() {
        assertTrue(
            Regex("^[0-9]+\\.[0-9]+\\.[0-9]+(-[A-Za-z0-9.]+)?$").matches(BuildConfig.UPSTREAM_VERSION_NAME),
            "UPSTREAM_VERSION_NAME must be plain upstream semver: got '${BuildConfig.UPSTREAM_VERSION_NAME}'"
        )
    }
}
