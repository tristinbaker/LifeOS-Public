package com.lifeos.modules.lifeos_mealtracker.di;

import com.lifeos.modules.lifeos_mealtracker.data.local.AppDatabase;
import com.lifeos.modules.lifeos_mealtracker.data.local.WeightEntryDao;
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
public final class AppModule_ProvideWeightEntryDaoFactory implements Factory<WeightEntryDao> {
  private final Provider<AppDatabase> databaseProvider;

  public AppModule_ProvideWeightEntryDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public WeightEntryDao get() {
    return provideWeightEntryDao(databaseProvider.get());
  }

  public static AppModule_ProvideWeightEntryDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new AppModule_ProvideWeightEntryDaoFactory(databaseProvider);
  }

  public static WeightEntryDao provideWeightEntryDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideWeightEntryDao(database));
  }
}
