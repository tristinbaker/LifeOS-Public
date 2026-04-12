package com.lifeos.modules.lifeos_mealtracker.data.repository;

import com.lifeos.modules.lifeos_mealtracker.data.local.SavedMealDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class SavedMealRepository_Factory implements Factory<SavedMealRepository> {
  private final Provider<SavedMealDao> savedMealDaoProvider;

  public SavedMealRepository_Factory(Provider<SavedMealDao> savedMealDaoProvider) {
    this.savedMealDaoProvider = savedMealDaoProvider;
  }

  @Override
  public SavedMealRepository get() {
    return newInstance(savedMealDaoProvider.get());
  }

  public static SavedMealRepository_Factory create(Provider<SavedMealDao> savedMealDaoProvider) {
    return new SavedMealRepository_Factory(savedMealDaoProvider);
  }

  public static SavedMealRepository newInstance(SavedMealDao savedMealDao) {
    return new SavedMealRepository(savedMealDao);
  }
}
