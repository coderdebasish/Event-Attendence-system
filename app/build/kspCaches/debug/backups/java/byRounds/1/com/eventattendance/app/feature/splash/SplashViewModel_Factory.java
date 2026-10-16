package com.eventattendance.app.feature.splash;

import com.eventattendance.app.data.local.UserPreferences;
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
public final class SplashViewModel_Factory implements Factory<SplashViewModel> {
  private final Provider<UserPreferences> preferencesProvider;

  public SplashViewModel_Factory(Provider<UserPreferences> preferencesProvider) {
    this.preferencesProvider = preferencesProvider;
  }

  @Override
  public SplashViewModel get() {
    return newInstance(preferencesProvider.get());
  }

  public static SplashViewModel_Factory create(Provider<UserPreferences> preferencesProvider) {
    return new SplashViewModel_Factory(preferencesProvider);
  }

  public static SplashViewModel newInstance(UserPreferences preferences) {
    return new SplashViewModel(preferences);
  }
}
