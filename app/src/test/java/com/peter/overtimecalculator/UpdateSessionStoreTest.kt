package com.peter.overtimecalculator

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import androidx.test.core.app.ApplicationProvider
import com.peter.overtimecalculator.data.update.SharedPreferencesUpdateSessionStore
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class UpdateSessionStoreTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun pendingDownload_roundTripsThroughSharedPreferences() {
        val store = SharedPreferencesUpdateSessionStore.create(isolatedContext("pending-download"))

        store.savePendingDownload(downloadId = 42L, versionName = "1.2.1")

        val pending = store.getPendingDownload()
        requireNotNull(pending)
        assertEquals(42L, pending.downloadId)
        assertEquals("1.2.1", pending.versionName)
        assertFalse(store.isAwaitingInstallPermission())
    }

    @Test
    fun awaitingInstallPermission_roundTripsIndependently() {
        val store = SharedPreferencesUpdateSessionStore.create(isolatedContext("awaiting"))

        store.setAwaitingInstallPermission(true)

        assertTrue(store.isAwaitingInstallPermission())
    }

    @Test
    fun clearPendingDownload_removesStoredState() {
        val store = SharedPreferencesUpdateSessionStore.create(isolatedContext("clear"))
        store.savePendingDownload(downloadId = 99L, versionName = "1.2.1")
        store.setAwaitingInstallPermission(true)

        store.clearPendingDownload()

        assertNull(store.getPendingDownload())
        assertFalse(store.isAwaitingInstallPermission())
    }

    private fun isolatedContext(name: String): Context {
        val base = ApplicationProvider.getApplicationContext<Context>()
        val root = temporaryFolder.newFolder(name)
        return object : ContextWrapper(base) {
            override fun getApplicationContext(): Context = this

            override fun getDataDir(): File = root

            override fun getFilesDir(): File = File(root, "files").apply(File::mkdirs)

            override fun getCacheDir(): File = File(root, "cache").apply(File::mkdirs)

            override fun getNoBackupFilesDir(): File = File(root, "no-backup").apply(File::mkdirs)
        }
    }
}
