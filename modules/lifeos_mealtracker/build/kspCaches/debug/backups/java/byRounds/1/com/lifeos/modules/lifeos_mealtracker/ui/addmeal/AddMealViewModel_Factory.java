package com.lifeos.modules.lifeos_mealtracker.ui.addmeal;

import com.lifeos.modules.lifeos_mealtracker.data.repository.MealRepository;
import com.lifeos.modules.lifeos_mealtracker.data.repository.SavedMealRepository;
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
public final class AddMealViewModel_Factory implements Factory<AddMealViewModel> {
  private final Provider<MealRepository> mealRepositoryProvider;

  private final Provider<SavedMealRepository> savedMealRepositoryProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public AddMealViewModel_Factory(Provider<MealRepository> mealRepositoryProvider,
      Provider<SavedMealRepository> savedMealRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.mealRepositoryProvider = mealRepositoryProvider;
    this.savedMealRepositoryProvider = savedMealRepositoryProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public AddMealViewModel get() {
    return newInstance(mealRepositoryProvider.get(), savedMealRepositoryProvider.get(), settingsRepositoryProvider.get());
  }

  public static AddMealViewModel_Factory create(Provider<MealRepository> mealRepositoryProvider,
      Provider<SavedMealRepository> savedMealRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new AddMealViewModel_Factory(mealRepositoryProvider, savedMealRepositoryProvider, settingsRepositoryProvider);
  }

  public static AddMealViewModel newInstance(MealRepository mealRepository,
      SavedMealRepository savedMealRepository, SettingsRepository settingsRepository) {
    return new AddMealViewModel(mealRepository, savedMealRepository, settingsRepository);
  }
}
