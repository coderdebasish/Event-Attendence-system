package com.eventattendance.app.di;

import com.eventattendance.app.data.local.UserPreferences;
import com.eventattendance.app.data.remote.SupabaseClientProvider;
import com.eventattendance.app.data.repository.AuthRepository;
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
    "KotlinInternalInJava",
    "cast"
})
public final class AppModule_ProvideAuthRepositoryFactory implements Factory<AuthRepository> {
  private final Provider<SupabaseClientProvider> clientProvider;

  private final Provider<UserPreferences> preferencesProvider;

  public AppModule_ProvideAuthRepositoryFactory(Provider<SupabaseClientProvider> clientProvider,
      Provider<UserPreferences> preferencesProvider) {
    this.clientProvider = clientProvider;
    this.preferencesProvider = preferencesProvider;
  }

  @Override
  public AuthRepository get() {
    return provideAuthRepository(clientProvider.get(), preferencesProvider.get());
  }

  public static AppModule_ProvideAuthRepositoryFactory create(
      Provider<SupabaseClientProvider> clientProvider,
      Provider<UserPreferences> preferencesProvider) {
    return new AppModule_ProvideAuthRepositoryFactory(clientProvider, preferencesProvider);
  }

  public static AuthRepository provideAuthRepository(SupabaseClientProvider clientProvider,
      UserPreferences preferences) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideAuthRepository(clientProvider, preferences));
  }
}
