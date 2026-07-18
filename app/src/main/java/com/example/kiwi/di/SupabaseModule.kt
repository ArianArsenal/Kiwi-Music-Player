package com.example.kiwi.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = "https://vhmtvynkgnfckeapmhxe.supabase.co",
            supabaseKey = "sb_publishable_ux001UdTm2TL7jRokYyjOw_o77vw7M-"
        ) {
            install(Postgrest)
        }
    }
}