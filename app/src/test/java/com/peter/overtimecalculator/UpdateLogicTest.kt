package com.peter.overtimecalculator

import com.peter.overtimecalculator.domain.AppUpdateAssetSelector
import com.peter.overtimecalculator.domain.AppUpdateVersioning
import com.peter.overtimecalculator.domain.ReleaseAsset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateLogicTest {
    @Test
    fun semanticVersionComparison_supportsPatchUpdates() {
        assertTrue(AppUpdateVersioning.isRemoteNewer("1.2.0", "1.2.1"))
    }

    @Test
    fun semanticVersionComparison_supportsTwoDigitMinorVersions() {
        assertTrue(AppUpdateVersioning.isRemoteNewer("1.2.0", "1.10.0"))
    }

    @Test
    fun semanticVersionComparison_treatsTagPrefixAsSameVersion() {
        assertFalse(AppUpdateVersioning.isRemoteNewer("1.2.0", "v1.2.0"))
        assertEquals("1.2.0", AppUpdateVersioning.normalize("v1.2.0"))
    }

    @Test
    fun apkSelector_prefersVersionMatchedUniversalApk() {
        val selected = AppUpdateAssetSelector.selectApkAsset(
            versionName = "1.2.0",
            assets = listOf(
                ReleaseAsset("app-debug.apk", "https://example.com/debug.apk"),
                ReleaseAsset("OvertimeCalculator-1.2.0-universal.apk", "https://example.com/release.apk"),
            ),
        )

        assertEquals("https://example.com/release.apk", selected?.downloadUrl)
    }

    @Test
    fun apkSelector_returnsNullWhenNoApkAssetExists() {
        val selected = AppUpdateAssetSelector.selectApkAsset(
            versionName = "1.2.0",
            assets = listOf(
                ReleaseAsset("notes.txt", "https://example.com/notes.txt"),
                ReleaseAsset("source.zip", "https://example.com/source.zip"),
            ),
        )

        assertNull(selected)
    }
}
