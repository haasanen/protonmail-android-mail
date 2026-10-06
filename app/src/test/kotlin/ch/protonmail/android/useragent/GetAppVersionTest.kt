/*
 * Copyright (c) 2026 haasanen
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package ch.protonmail.android.useragent

import ch.protonmail.android.BuildConfig
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Guards the server-facing version contract: Proton's API validates the
 * app-version header and rejects our fork build suffix (-hN) with error
 * 2064, which kills the mail sync event loop. The version that leaves the
 * device must always be upstream's own version string.
 */
class GetAppVersionTest {

    private val forkSuffix = Regex("-h[0-9]+$")

    @Test
    fun `server-facing version never carries the fork build suffix`() {
        assertFalse(
            forkSuffix.containsMatchIn(GetAppVersion()()),
            "GetAppVersion() must not return a version with the fork -hN suffix " +
                "(Proton API error 2064 breaks mail sync): got '${GetAppVersion()()}'"
        )
    }

    @Test
    fun `server-facing version is the upstream version, not the display version`() {
        assertEquals(BuildConfig.UPSTREAM_VERSION_NAME, GetAppVersion()())
    }

    @Test
    fun `upstream version is a plain semver the API can parse`() {
        assertTrue(
            Regex("^[0-9]+\\.[0-9]+\\.[0-9]+(-[A-Za-z0-9.]+)?$").matches(BuildConfig.UPSTREAM_VERSION_NAME),
            "UPSTREAM_VERSION_NAME must be plain upstream semver: got '${BuildConfig.UPSTREAM_VERSION_NAME}'"
        )
    }
}
