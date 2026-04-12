package com.lifeos.modules.lifeos_mealtracker.di;

import com.lifeos.modules.lifeos_mealtracker.data.local.AppDatabase;
import com.lifeos.modules.lifeos_mealtracker.data.local.SavedMealDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideSavedMealDaoFactory implements Factory<SavedMealDao> {
  private final Provider<AppDatabase> databaseProvider;

  public AppModule_ProvideSavedMealDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public SavedMealDao get() {
    return provideSavedMealDao(databaseProvider.get());
  }

  public static AppModule_ProvideSavedMealDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new AppModule_ProvideSavedMealDaoFactory(databaseProvider);
  }

  public static SavedMealDao provideSavedMealDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSavedMealDao(database));
  }
}
