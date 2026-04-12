package com.lifeos.modules.lifeos_mealtracker.domain.usecase;

import com.lifeos.modules.lifeos_mealtracker.data.repository.WeightRepository;
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
public final class EstimateGoalDateUseCase_Factory implements Factory<EstimateGoalDateUseCase> {
  private final Provider<WeightRepository> weightRepositoryProvider;

  public EstimateGoalDateUseCase_Factory(Provider<WeightRepository> weightRepositoryProvider) {
    this.weightRepositoryProvider = weightRepositoryProvider;
  }

  @Override
  public EstimateGoalDateUseCase get() {
    return newInstance(weightRepositoryProvider.get());
  }

  public static EstimateGoalDateUseCase_Factory create(
      Provider<WeightRepository> weightRepositoryProvider) {
    return new EstimateGoalDateUseCase_Factory(weightRepositoryProvider);
  }

  public static EstimateGoalDateUseCase newInstance(WeightRepository weightRepository) {
    return new EstimateGoalDateUseCase(weightRepository);
  }
}
