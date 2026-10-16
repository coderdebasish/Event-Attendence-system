package com.eventattendance.app.data.remote

import com.eventattendance.app.core.config.SupabaseConfig
import com.eventattendance.app.data.local.UserPreferences
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseClientProvider @Inject constructor(
    private val preferences: UserPreferences
) {
    private var cachedClient: SupabaseClient? = null
    private var cachedUrl: String? = null
    private var cachedKey: String? = null

    suspend fun getClient(): SupabaseClient {
        val overrideUrl = preferences.customSupabaseUrlFlow.first()
        val overrideKey = preferences.customSupabaseAnonKeyFlow.first()

        val url = SupabaseConfig.getBaseUrl(overrideUrl)
        val key = SupabaseConfig.getAnonKey(overrideKey)

        if (cachedClient != null && cachedUrl == url && cachedKey == key) {
            return cachedClient!!
        }

        val client = createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = key
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
        }

        cachedClient = client
        cachedUrl = url
        cachedKey = key
        return client
    }
}
