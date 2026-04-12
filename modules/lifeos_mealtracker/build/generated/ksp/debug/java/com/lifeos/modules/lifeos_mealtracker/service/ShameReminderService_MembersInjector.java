package com.lifeos.modules.lifeos_mealtracker.service;

import com.lifeos.modules.lifeos_mealtracker.data.local.AppDatabase;
import com.lifeos.modules.lifeos_mealtracker.data.repository.SettingsRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class ShameReminderService_MembersInjector implements MembersInjector<ShameReminderService> {
  private final Provider<AppDatabase> databaseProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public ShameReminderService_MembersInjector(Provider<AppDatabase> databaseProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.databaseProvider = databaseProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  public static MembersInjector<ShameReminderService> create(Provider<AppDatabase> databaseProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new ShameReminderService_MembersInjector(databaseProvider, settingsRepositoryProvider);
  }

  @Override
  public void injectMembers(ShameReminderService instance) {
    injectDatabase(instance, databaseProvider.get());
    injectSettingsRepository(instance, settingsRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.lifeos.modules.lifeos_mealtracker.service.ShameReminderService.database")
  public static void injectDatabase(ShameReminderService instance, AppDatabase database) {
    instance.database = database;
  }

  @InjectedFieldSignature("com.lifeos.modules.lifeos_mealtracker.service.ShameReminderService.settingsRepository")
  public static void injectSettingsRepository(ShameReminderService instance,
      SettingsRepository settingsRepository) {
    instance.settingsRepository = settingsRepository;
  }
}
