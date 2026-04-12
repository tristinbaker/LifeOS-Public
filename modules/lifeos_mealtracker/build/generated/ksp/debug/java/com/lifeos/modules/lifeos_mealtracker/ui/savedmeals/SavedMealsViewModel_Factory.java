package com.lifeos.modules.lifeos_mealtracker.ui.savedmeals;

import com.lifeos.modules.lifeos_mealtracker.data.repository.SavedMealRepository;
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
public final class SavedMealsViewModel_Factory implements Factory<SavedMealsViewModel> {
  private final Provider<SavedMealRepository> savedMealRepositoryProvider;

  public SavedMealsViewModel_Factory(Provider<SavedMealRepository> savedMealRepositoryProvider) {
    this.savedMealRepositoryProvider = savedMealRepositoryProvider;
  }

  @Override
  public SavedMealsViewModel get() {
    return newInstance(savedMealRepositoryProvider.get());
  }

  public static SavedMealsViewModel_Factory create(
      Provider<SavedMealRepository> savedMealRepositoryProvider) {
    return new SavedMealsViewModel_Factory(savedMealRepositoryProvider);
  }

  public static SavedMealsViewModel newInstance(SavedMealRepository savedMealRepository) {
    return new SavedMealsViewModel(savedMealRepository);
  }
}
