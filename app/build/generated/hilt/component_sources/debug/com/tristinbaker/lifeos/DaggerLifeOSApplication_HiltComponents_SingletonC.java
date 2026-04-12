package com.tristinbaker.lifeos;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.lifeos.modules.lifeos_mealtracker.data.local.AppDatabase;
import com.lifeos.modules.lifeos_mealtracker.data.local.MealEntryDao;
import com.lifeos.modules.lifeos_mealtracker.data.local.SavedMealDao;
import com.lifeos.modules.lifeos_mealtracker.data.local.WeightEntryDao;
import com.lifeos.modules.lifeos_mealtracker.data.repository.MealRepository;
import com.lifeos.modules.lifeos_mealtracker.data.repository.SavedMealRepository;
import com.lifeos.modules.lifeos_mealtracker.data.repository.SettingsRepository;
import com.lifeos.modules.lifeos_mealtracker.data.repository.WeightRepository;
import com.lifeos.modules.lifeos_mealtracker.di.AppModule_ProvideAppDatabaseFactory;
import com.lifeos.modules.lifeos_mealtracker.di.AppModule_ProvideMealEntryDaoFactory;
import com.lifeos.modules.lifeos_mealtracker.di.AppModule_ProvideSavedMealDaoFactory;
import com.lifeos.modules.lifeos_mealtracker.di.AppModule_ProvideWeightEntryDaoFactory;
import com.lifeos.modules.lifeos_mealtracker.domain.usecase.EstimateGoalDateUseCase;
import com.lifeos.modules.lifeos_mealtracker.service.ShameReminderService;
import com.lifeos.modules.lifeos_mealtracker.service.ShameReminderService_MembersInjector;
import com.lifeos.modules.lifeos_mealtracker.ui.addmeal.AddMealViewModel;
import com.lifeos.modules.lifeos_mealtracker.ui.addmeal.AddMealViewModel_HiltModules_KeyModule_ProvideFactory;
import com.lifeos.modules.lifeos_mealtracker.ui.analytics.AnalyticsViewModel;
import com.lifeos.modules.lifeos_mealtracker.ui.analytics.AnalyticsViewModel_HiltModules_KeyModule_ProvideFactory;
import com.lifeos.modules.lifeos_mealtracker.ui.dashboard.DashboardViewModel;
import com.lifeos.modules.lifeos_mealtracker.ui.dashboard.DashboardViewModel_HiltModules_KeyModule_ProvideFactory;
import com.lifeos.modules.lifeos_mealtracker.ui.savedmeals.SavedMealsViewModel;
import com.lifeos.modules.lifeos_mealtracker.ui.savedmeals.SavedMealsViewModel_HiltModules_KeyModule_ProvideFactory;
import com.lifeos.modules.lifeos_mealtracker.ui.settings.SettingsViewModel;
import com.lifeos.modules.lifeos_mealtracker.ui.settings.SettingsViewModel_HiltModules_KeyModule_ProvideFactory;
import com.lifeos.modules.lifeos_mealtracker.ui.weight.WeightViewModel;
import com.lifeos.modules.lifeos_mealtracker.ui.weight.WeightViewModel_HiltModules_KeyModule_ProvideFactory;
import com.lifeos.modules.lifeos_notes.data.local.NoteDao;
import com.lifeos.modules.lifeos_notes.data.local.NotesDatabase;
import com.lifeos.modules.lifeos_notes.data.repository.NotesRepository;
import com.lifeos.modules.lifeos_notes.di.NotesModule_ProvideNoteDaoFactory;
import com.lifeos.modules.lifeos_notes.di.NotesModule_ProvideNotesDatabaseFactory;
import com.lifeos.modules.lifeos_notes.ui.NotesViewModel;
import com.lifeos.modules.lifeos_notes.ui.NotesViewModel_HiltModules_KeyModule_ProvideFactory;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.SetBuilder;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

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
public final class DaggerLifeOSApplication_HiltComponents_SingletonC {
  private DaggerLifeOSApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public LifeOSApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements LifeOSApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public LifeOSApplication_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements LifeOSApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public LifeOSApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements LifeOSApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public LifeOSApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements LifeOSApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public LifeOSApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements LifeOSApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public LifeOSApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements LifeOSApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public LifeOSApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements LifeOSApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public LifeOSApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends LifeOSApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends LifeOSApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends LifeOSApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends LifeOSApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Set<String> getViewModelKeys() {
      return SetBuilder.<String>newSetBuilder(7).add(AddMealViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(AnalyticsViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(DashboardViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(NotesViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(SavedMealsViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(SettingsViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(WeightViewModel_HiltModules_KeyModule_ProvideFactory.provide()).build();
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }
  }

  private static final class ViewModelCImpl extends LifeOSApplication_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AddMealViewModel> addMealViewModelProvider;

    private Provider<AnalyticsViewModel> analyticsViewModelProvider;

    private Provider<DashboardViewModel> dashboardViewModelProvider;

    private Provider<NotesViewModel> notesViewModelProvider;

    private Provider<SavedMealsViewModel> savedMealsViewModelProvider;

    private Provider<SettingsViewModel> settingsViewModelProvider;

    private Provider<WeightViewModel> weightViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    private EstimateGoalDateUseCase estimateGoalDateUseCase() {
      return new EstimateGoalDateUseCase(singletonCImpl.weightRepositoryProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.addMealViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.analyticsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.dashboardViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.notesViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.savedMealsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.weightViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
    }

    @Override
    public Map<String, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(7).put("com.lifeos.modules.lifeos_mealtracker.ui.addmeal.AddMealViewModel", ((Provider) addMealViewModelProvider)).put("com.lifeos.modules.lifeos_mealtracker.ui.analytics.AnalyticsViewModel", ((Provider) analyticsViewModelProvider)).put("com.lifeos.modules.lifeos_mealtracker.ui.dashboard.DashboardViewModel", ((Provider) dashboardViewModelProvider)).put("com.lifeos.modules.lifeos_notes.ui.NotesViewModel", ((Provider) notesViewModelProvider)).put("com.lifeos.modules.lifeos_mealtracker.ui.savedmeals.SavedMealsViewModel", ((Provider) savedMealsViewModelProvider)).put("com.lifeos.modules.lifeos_mealtracker.ui.settings.SettingsViewModel", ((Provider) settingsViewModelProvider)).put("com.lifeos.modules.lifeos_mealtracker.ui.weight.WeightViewModel", ((Provider) weightViewModelProvider)).build();
    }

    @Override
    public Map<String, Object> getHiltViewModelAssistedMap() {
      return Collections.<String, Object>emptyMap();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.lifeos.modules.lifeos_mealtracker.ui.addmeal.AddMealViewModel 
          return (T) new AddMealViewModel(singletonCImpl.mealRepositoryProvider.get(), singletonCImpl.savedMealRepositoryProvider.get(), singletonCImpl.settingsRepositoryProvider.get());

          case 1: // com.lifeos.modules.lifeos_mealtracker.ui.analytics.AnalyticsViewModel 
          return (T) new AnalyticsViewModel(singletonCImpl.mealRepositoryProvider.get(), singletonCImpl.settingsRepositoryProvider.get());

          case 2: // com.lifeos.modules.lifeos_mealtracker.ui.dashboard.DashboardViewModel 
          return (T) new DashboardViewModel(singletonCImpl.mealRepositoryProvider.get(), singletonCImpl.settingsRepositoryProvider.get());

          case 3: // com.lifeos.modules.lifeos_notes.ui.NotesViewModel 
          return (T) new NotesViewModel(singletonCImpl.notesRepositoryProvider.get());

          case 4: // com.lifeos.modules.lifeos_mealtracker.ui.savedmeals.SavedMealsViewModel 
          return (T) new SavedMealsViewModel(singletonCImpl.savedMealRepositoryProvider.get());

          case 5: // com.lifeos.modules.lifeos_mealtracker.ui.settings.SettingsViewModel 
          return (T) new SettingsViewModel(singletonCImpl.settingsRepositoryProvider.get());

          case 6: // com.lifeos.modules.lifeos_mealtracker.ui.weight.WeightViewModel 
          return (T) new WeightViewModel(singletonCImpl.weightRepositoryProvider.get(), singletonCImpl.settingsRepositoryProvider.get(), viewModelCImpl.estimateGoalDateUseCase());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends LifeOSApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends LifeOSApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }

    @Override
    public void injectShameReminderService(ShameReminderService shameReminderService) {
      injectShameReminderService2(shameReminderService);
    }

    private ShameReminderService injectShameReminderService2(ShameReminderService instance) {
      ShameReminderService_MembersInjector.injectDatabase(instance, singletonCImpl.provideAppDatabaseProvider.get());
      ShameReminderService_MembersInjector.injectSettingsRepository(instance, singletonCImpl.settingsRepositoryProvider.get());
      return instance;
    }
  }

  private static final class SingletonCImpl extends LifeOSApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<AppDatabase> provideAppDatabaseProvider;

    private Provider<MealEntryDao> provideMealEntryDaoProvider;

    private Provider<MealRepository> mealRepositoryProvider;

    private Provider<SavedMealDao> provideSavedMealDaoProvider;

    private Provider<SavedMealRepository> savedMealRepositoryProvider;

    private Provider<SettingsRepository> settingsRepositoryProvider;

    private Provider<NotesDatabase> provideNotesDatabaseProvider;

    private Provider<NoteDao> provideNoteDaoProvider;

    private Provider<NotesRepository> notesRepositoryProvider;

    private Provider<WeightEntryDao> provideWeightEntryDaoProvider;

    private Provider<WeightRepository> weightRepositoryProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideAppDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<AppDatabase>(singletonCImpl, 2));
      this.provideMealEntryDaoProvider = DoubleCheck.provider(new SwitchingProvider<MealEntryDao>(singletonCImpl, 1));
      this.mealRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<MealRepository>(singletonCImpl, 0));
      this.provideSavedMealDaoProvider = DoubleCheck.provider(new SwitchingProvider<SavedMealDao>(singletonCImpl, 4));
      this.savedMealRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<SavedMealRepository>(singletonCImpl, 3));
      this.settingsRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<SettingsRepository>(singletonCImpl, 5));
      this.provideNotesDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<NotesDatabase>(singletonCImpl, 8));
      this.provideNoteDaoProvider = DoubleCheck.provider(new SwitchingProvider<NoteDao>(singletonCImpl, 7));
      this.notesRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<NotesRepository>(singletonCImpl, 6));
      this.provideWeightEntryDaoProvider = DoubleCheck.provider(new SwitchingProvider<WeightEntryDao>(singletonCImpl, 10));
      this.weightRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<WeightRepository>(singletonCImpl, 9));
    }

    @Override
    public void injectLifeOSApplication(LifeOSApplication lifeOSApplication) {
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.lifeos.modules.lifeos_mealtracker.data.repository.MealRepository 
          return (T) new MealRepository(singletonCImpl.provideMealEntryDaoProvider.get());

          case 1: // com.lifeos.modules.lifeos_mealtracker.data.local.MealEntryDao 
          return (T) AppModule_ProvideMealEntryDaoFactory.provideMealEntryDao(singletonCImpl.provideAppDatabaseProvider.get());

          case 2: // com.lifeos.modules.lifeos_mealtracker.data.local.AppDatabase 
          return (T) AppModule_ProvideAppDatabaseFactory.provideAppDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 3: // com.lifeos.modules.lifeos_mealtracker.data.repository.SavedMealRepository 
          return (T) new SavedMealRepository(singletonCImpl.provideSavedMealDaoProvider.get());

          case 4: // com.lifeos.modules.lifeos_mealtracker.data.local.SavedMealDao 
          return (T) AppModule_ProvideSavedMealDaoFactory.provideSavedMealDao(singletonCImpl.provideAppDatabaseProvider.get());

          case 5: // com.lifeos.modules.lifeos_mealtracker.data.repository.SettingsRepository 
          return (T) new SettingsRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 6: // com.lifeos.modules.lifeos_notes.data.repository.NotesRepository 
          return (T) new NotesRepository(singletonCImpl.provideNoteDaoProvider.get());

          case 7: // com.lifeos.modules.lifeos_notes.data.local.NoteDao 
          return (T) NotesModule_ProvideNoteDaoFactory.provideNoteDao(singletonCImpl.provideNotesDatabaseProvider.get());

          case 8: // com.lifeos.modules.lifeos_notes.data.local.NotesDatabase 
          return (T) NotesModule_ProvideNotesDatabaseFactory.provideNotesDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 9: // com.lifeos.modules.lifeos_mealtracker.data.repository.WeightRepository 
          return (T) new WeightRepository(singletonCImpl.provideWeightEntryDaoProvider.get());

          case 10: // com.lifeos.modules.lifeos_mealtracker.data.local.WeightEntryDao 
          return (T) AppModule_ProvideWeightEntryDaoFactory.provideWeightEntryDao(singletonCImpl.provideAppDatabaseProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
