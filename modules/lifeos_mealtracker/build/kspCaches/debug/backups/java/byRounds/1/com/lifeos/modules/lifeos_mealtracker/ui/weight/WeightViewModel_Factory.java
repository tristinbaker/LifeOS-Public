package com.lifeos.modules.lifeos_mealtracker.ui.weight;

import com.lifeos.modules.lifeos_mealtracker.data.repository.SettingsRepository;
import com.lifeos.modules.lifeos_mealtracker.data.repository.WeightRepository;
import com.lifeos.modules.lifeos_mealtracker.domain.usecase.EstimateGoalDateUseCase;
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
public final class WeightViewModel_Factory implements Factory<WeightViewModel> {
  private final Provider<WeightRepository> weightRepositoryProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<EstimateGoalDateUseCase> estimateGoalDateUseCaseProvider;

  public WeightViewModel_Factory(Provider<WeightRepository> weightRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<EstimateGoalDateUseCase> estimateGoalDateUseCaseProvider) {
    this.weightRepositoryProvider = weightRepositoryProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.estimateGoalDateUseCaseProvider = estimateGoalDateUseCaseProvider;
  }

  @Override
  public WeightViewModel get() {
    return newInstance(weightRepositoryProvider.get(), settingsRepositoryProvider.get(), estimateGoalDateUseCaseProvider.get());
  }

  public static WeightViewModel_Factory create(Provider<WeightRepository> weightRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<EstimateGoalDateUseCase> estimateGoalDateUseCaseProvider) {
    return new WeightViewModel_Factory(weightRepositoryProvider, settingsRepositoryProvider, estimateGoalDateUseCaseProvider);
  }

  public static WeightViewModel newInstance(WeightRepository weightRepository,
      SettingsRepository settingsRepository, EstimateGoalDateUseCase estimateGoalDateUseCase) {
    return new WeightViewModel(weightRepository, settingsRepository, estimateGoalDateUseCase);
  }
}
