package com.eventattendance.app.data.remote;

import com.eventattendance.app.data.local.AttendanceDao;
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
public final class SupabaseRealtimeManager_Factory implements Factory<SupabaseRealtimeManager> {
  private final Provider<SupabaseClientProvider> clientProvider;

  private final Provider<AttendanceDao> attendanceDaoProvider;

  public SupabaseRealtimeManager_Factory(Provider<SupabaseClientProvider> clientProvider,
      Provider<AttendanceDao> attendanceDaoProvider) {
    this.clientProvider = clientProvider;
    this.attendanceDaoProvider = attendanceDaoProvider;
  }

  @Override
  public SupabaseRealtimeManager get() {
    return newInstance(clientProvider.get(), attendanceDaoProvider.get());
  }

  public static SupabaseRealtimeManager_Factory create(
      Provider<SupabaseClientProvider> clientProvider,
      Provider<AttendanceDao> attendanceDaoProvider) {
    return new SupabaseRealtimeManager_Factory(clientProvider, attendanceDaoProvider);
  }

  public static SupabaseRealtimeManager newInstance(SupabaseClientProvider clientProvider,
      AttendanceDao attendanceDao) {
    return new SupabaseRealtimeManager(clientProvider, attendanceDao);
  }
}
