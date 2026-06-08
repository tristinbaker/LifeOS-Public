package com.lifeos.modules.lifeos_aiinsights.di

import android.content.Context
import androidx.room.Room
import com.lifeos.core.InsightProvider
import com.lifeos.core.ReportDataProvider
import com.lifeos.core.UserPrefs
import com.lifeos.modules.lifeos_aiinsights.BuildConfig
import com.lifeos.modules.lifeos_aiinsights.data.local.AiInsightsDatabase
import com.lifeos.modules.lifeos_aiinsights.data.local.CachedReportDao
import com.lifeos.modules.lifeos_aiinsights.data.remote.GeminiClient
import com.lifeos.modules.lifeos_aiinsights.report.MonthlyOverviewInsightProvider
import com.lifeos.modules.lifeos_aiinsights.report.WeeklyOverviewInsightProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import dagger.multibindings.IntoSet
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiInsightsDiModule {

    @Provides
    @Singleton
    fun provideUserPrefs(@ApplicationContext context: Context): UserPrefs = UserPrefs(context)

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideGeminiClient(okHttpClient: OkHttpClient): GeminiClient =
        GeminiClient(apiKey = BuildConfig.GROQ_API_KEY, okHttpClient = okHttpClient)

    @Provides
    @Singleton
    fun provideAiInsightsDatabase(@ApplicationContext context: Context): AiInsightsDatabase =
        Room.databaseBuilder(context, AiInsightsDatabase::class.java, "lifeos_aiinsights.db").build()

    @Provides
    @Singleton
    fun provideCachedReportDao(db: AiInsightsDatabase): CachedReportDao = db.cachedReportDao()

    @Provides
    @ElementsIntoSet
    fun provideDefaultReportProviders(): Set<ReportDataProvider> = emptySet()

    @Provides
    @IntoSet
    @Singleton
    fun provideWeeklyOverviewInsightProvider(
        providers: Set<@JvmSuppressWildcards ReportDataProvider>
    ): InsightProvider = WeeklyOverviewInsightProvider(providers)

    @Provides
    @IntoSet
    @Singleton
    fun provideMonthlyOverviewInsightProvider(
        providers: Set<@JvmSuppressWildcards ReportDataProvider>
    ): InsightProvider = MonthlyOverviewInsightProvider(providers)

    @Provides
    @ElementsIntoSet
    fun provideDefaultInsightProviders(): Set<InsightProvider> = emptySet()
}
