package com.eventattendance.app.data.remote;

import com.eventattendance.app.data.local.UserPreferences;
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
public final class SupabaseClientProvider_Factory implements Factory<SupabaseClientProvider> {
  private final Provider<UserPreferences> preferencesProvider;

  public SupabaseClientProvider_Factory(Provider<UserPreferences> preferencesProvider) {
    this.preferencesProvider = preferencesProvider;
  }

  @Override
  public SupabaseClientProvider get() {
    return newInstance(preferencesProvider.get());
  }

  public static SupabaseClientProvider_Factory create(
      Provider<UserPreferences> preferencesProvider) {
    return new SupabaseClientProvider_Factory(preferencesProvider);
  }

  public static SupabaseClientProvider newInstance(UserPreferences preferences) {
    return new SupabaseClientProvider(preferences);
  }
}
