package com.eventattendance.app.data.repository;

import com.eventattendance.app.data.local.UserPreferences;
import com.eventattendance.app.data.remote.SupabaseClientProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
    "KotlinInternalInJava",
    "cast"
})
public final class AuthRepositoryImpl_Factory implements Factory<AuthRepositoryImpl> {
  private final Provider<SupabaseClientProvider> clientProvider;

  private final Provider<UserPreferences> preferencesProvider;

  public AuthRepositoryImpl_Factory(Provider<SupabaseClientProvider> clientProvider,
      Provider<UserPreferences> preferencesProvider) {
    this.clientProvider = clientProvider;
    this.preferencesProvider = preferencesProvider;
  }

  @Override
  public AuthRepositoryImpl get() {
    return newInstance(clientProvider.get(), preferencesProvider.get());
  }

  public static AuthRepositoryImpl_Factory create(Provider<SupabaseClientProvider> clientProvider,
      Provider<UserPreferences> preferencesProvider) {
    return new AuthRepositoryImpl_Factory(clientProvider, preferencesProvider);
  }

  public static AuthRepositoryImpl newInstance(SupabaseClientProvider clientProvider,
      UserPreferences preferences) {
    return new AuthRepositoryImpl(clientProvider, preferences);
  }
}
