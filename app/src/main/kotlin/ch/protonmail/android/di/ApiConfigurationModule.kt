package ch.protonmail.android.di

import ch.protonmail.android.api.MailRustApiConfig
import ch.protonmail.android.mailsession.domain.model.RustApiConfig
import ch.protonmail.android.useragent.BuildUserAgent
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.configuration.EnvironmentConfiguration
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiConfigurationModule {

    @Provides
    @Singleton
    fun provideRustApiConfig(buildUserAgent: BuildUserAgent, httpUrl: HttpUrl): RustApiConfig =
        MailRustApiConfig(buildUserAgent, httpUrl)

    @Provides
    @Singleton
    fun provideProtonApiUrl(envConfig: EnvironmentConfiguration): HttpUrl = envConfig.baseUrl.toHttpUrl()
}
