package com.eventattendance.app.feature.onboarding;

import com.eventattendance.app.data.repository.AuthRepository;
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
    "KotlinInternalInJava",
    "cast"
})
public final class OnboardingViewModel_Factory implements Factory<OnboardingViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  public OnboardingViewModel_Factory(Provider<AuthRepository> authRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public OnboardingViewModel get() {
    return newInstance(authRepositoryProvider.get());
  }

  public static OnboardingViewModel_Factory create(
      Provider<AuthRepository> authRepositoryProvider) {
    return new OnboardingViewModel_Factory(authRepositoryProvider);
  }

  public static OnboardingViewModel newInstance(AuthRepository authRepository) {
    return new OnboardingViewModel(authRepository);
  }
}
