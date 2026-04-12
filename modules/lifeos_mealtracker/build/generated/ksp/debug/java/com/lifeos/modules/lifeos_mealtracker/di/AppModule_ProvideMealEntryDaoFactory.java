package com.lifeos.modules.lifeos_mealtracker.di;

import com.lifeos.modules.lifeos_mealtracker.data.local.AppDatabase;
import com.lifeos.modules.lifeos_mealtracker.data.local.MealEntryDao;
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
public final class AppModule_ProvideMealEntryDaoFactory implements Factory<MealEntryDao> {
  private final Provider<AppDatabase> databaseProvider;

  public AppModule_ProvideMealEntryDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public MealEntryDao get() {
    return provideMealEntryDao(databaseProvider.get());
  }

  public static AppModule_ProvideMealEntryDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new AppModule_ProvideMealEntryDaoFactory(databaseProvider);
  }

  public static MealEntryDao provideMealEntryDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideMealEntryDao(database));
  }
}
