package com.eventattendance.app.feature.settings;

import com.eventattendance.app.data.local.UserPreferences;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<UserPreferences> preferencesProvider;

  public SettingsViewModel_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<UserPreferences> preferencesProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.preferencesProvider = preferencesProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(authRepositoryProvider.get(), preferencesProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<AuthRepository> authRepositoryProvider,
      Provider<UserPreferences> preferencesProvider) {
    return new SettingsViewModel_Factory(authRepositoryProvider, preferencesProvider);
  }

  public static SettingsViewModel newInstance(AuthRepository authRepository,
      UserPreferences preferences) {
    return new SettingsViewModel(authRepository, preferences);
  }
}
