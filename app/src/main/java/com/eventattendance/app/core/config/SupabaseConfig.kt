package com.eventattendance.app.core.config

import com.eventattendance.app.BuildConfig

object SupabaseConfig {

    const val DEFAULT_PLACEHOLDER_URL = "https://your-project-id.supabase.co"
    const val DEFAULT_PLACEHOLDER_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.your-anon-key"

    fun getBaseUrl(overrideUrl: String?): String {
        return when {
            !overrideUrl.isNullOrBlank() -> overrideUrl.trim()
            BuildConfig.SUPABASE_URL.isNotBlank() && !BuildConfig.SUPABASE_URL.contains("your-project-id") -> BuildConfig.SUPABASE_URL.trim()
            else -> DEFAULT_PLACEHOLDER_URL
        }
    }

    fun getAnonKey(overrideKey: String?): String {
        return when {
            !overrideKey.isNullOrBlank() -> overrideKey.trim()
            BuildConfig.SUPABASE_ANON_KEY.isNotBlank() && !BuildConfig.SUPABASE_ANON_KEY.contains("your-anon-key") -> BuildConfig.SUPABASE_ANON_KEY.trim()
            else -> DEFAULT_PLACEHOLDER_KEY
        }
    }

    fun isConfigured(overrideUrl: String?, overrideKey: String?): Boolean {
        val url = getBaseUrl(overrideUrl)
        val key = getAnonKey(overrideKey)
        return url.startsWith("https://") && !url.contains("your-project-id") && key.length > 20 && !key.contains("your-anon-key")
    }
}
