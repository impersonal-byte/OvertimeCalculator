package com.peter.overtimecalculator.data.update

import android.content.Context
import android.content.SharedPreferences
import com.peter.overtimecalculator.domain.PendingUpdateDownload

interface UpdateSessionStore {
    fun savePendingDownload(downloadId: Long, versionName: String)

    fun getPendingDownload(): PendingUpdateDownload?

    fun clearPendingDownload()

    fun setAwaitingInstallPermission(awaiting: Boolean)

    fun isAwaitingInstallPermission(): Boolean
}

class SharedPreferencesUpdateSessionStore(
    private val preferences: SharedPreferences,
) : UpdateSessionStore {
    override fun savePendingDownload(downloadId: Long, versionName: String) {
        preferences.edit()
            .putLong(KEY_DOWNLOAD_ID, downloadId)
            .putString(KEY_REMOTE_VERSION, versionName)
            .putBoolean(KEY_AWAITING_PERMISSION, false)
            .apply()
    }

    override fun getPendingDownload(): PendingUpdateDownload? {
        val downloadId = preferences.getLong(KEY_DOWNLOAD_ID, INVALID_DOWNLOAD_ID)
        val versionName = preferences.getString(KEY_REMOTE_VERSION, null)
        if (downloadId == INVALID_DOWNLOAD_ID || versionName.isNullOrBlank()) {
            return null
        }
        return PendingUpdateDownload(downloadId, versionName)
    }

    override fun clearPendingDownload() {
        preferences.edit()
            .remove(KEY_DOWNLOAD_ID)
            .remove(KEY_REMOTE_VERSION)
            .remove(KEY_AWAITING_PERMISSION)
            .apply()
    }

    override fun setAwaitingInstallPermission(awaiting: Boolean) {
        preferences.edit().putBoolean(KEY_AWAITING_PERMISSION, awaiting).apply()
    }

    override fun isAwaitingInstallPermission(): Boolean {
        return preferences.getBoolean(KEY_AWAITING_PERMISSION, false)
    }

    companion object {
        private const val PREFS_NAME = "app-update-prefs"
        private const val KEY_DOWNLOAD_ID = "download_id"
        private const val KEY_REMOTE_VERSION = "remote_version"
        private const val KEY_AWAITING_PERMISSION = "awaiting_permission"
        private const val INVALID_DOWNLOAD_ID = -1L

        fun create(context: Context): SharedPreferencesUpdateSessionStore {
            return SharedPreferencesUpdateSessionStore(
                preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
            )
        }
    }
}
