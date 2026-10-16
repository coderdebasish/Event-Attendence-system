package com.eventattendance.app.data.remote;

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
public final class SupabaseAttendanceApi_Factory implements Factory<SupabaseAttendanceApi> {
  private final Provider<SupabaseClientProvider> clientProvider;

  public SupabaseAttendanceApi_Factory(Provider<SupabaseClientProvider> clientProvider) {
    this.clientProvider = clientProvider;
  }

  @Override
  public SupabaseAttendanceApi get() {
    return newInstance(clientProvider.get());
  }

  public static SupabaseAttendanceApi_Factory create(
      Provider<SupabaseClientProvider> clientProvider) {
    return new SupabaseAttendanceApi_Factory(clientProvider);
  }

  public static SupabaseAttendanceApi newInstance(SupabaseClientProvider clientProvider) {
    return new SupabaseAttendanceApi(clientProvider);
  }
}
