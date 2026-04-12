package com.lifeos.modules.lifeos_mealtracker.data.repository;

import com.lifeos.modules.lifeos_mealtracker.data.local.MealEntryDao;
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
public final class MealRepository_Factory implements Factory<MealRepository> {
  private final Provider<MealEntryDao> mealEntryDaoProvider;

  public MealRepository_Factory(Provider<MealEntryDao> mealEntryDaoProvider) {
    this.mealEntryDaoProvider = mealEntryDaoProvider;
  }

  @Override
  public MealRepository get() {
    return newInstance(mealEntryDaoProvider.get());
  }

  public static MealRepository_Factory create(Provider<MealEntryDao> mealEntryDaoProvider) {
    return new MealRepository_Factory(mealEntryDaoProvider);
  }

  public static MealRepository newInstance(MealEntryDao mealEntryDao) {
    return new MealRepository(mealEntryDao);
  }
}
