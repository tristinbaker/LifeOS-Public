package com.lifeos.modules.lifeos_mealtracker.ui.analytics;

import com.lifeos.modules.lifeos_mealtracker.data.repository.MealRepository;
import com.lifeos.modules.lifeos_mealtracker.data.repository.SettingsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class AnalyticsViewModel_Factory implements Factory<AnalyticsViewModel> {
  private final Provider<MealRepository> mealRepositoryProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public AnalyticsViewModel_Factory(Provider<MealRepository> mealRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.mealRepositoryProvider = mealRepositoryProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public AnalyticsViewModel get() {
    return newInstance(mealRepositoryProvider.get(), settingsRepositoryProvider.get());
  }

  public static AnalyticsViewModel_Factory create(Provider<MealRepository> mealRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new AnalyticsViewModel_Factory(mealRepositoryProvider, settingsRepositoryProvider);
  }

  public static AnalyticsViewModel newInstance(MealRepository mealRepository,
      SettingsRepository settingsRepository) {
    return new AnalyticsViewModel(mealRepository, settingsRepository);
  }
}
