package com.eventattendance.app.data.repository;

import com.eventattendance.app.data.local.EventDao;
import com.eventattendance.app.data.remote.SupabaseAttendanceApi;
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
public final class EventRepositoryImpl_Factory implements Factory<EventRepositoryImpl> {
  private final Provider<EventDao> eventDaoProvider;

  private final Provider<SupabaseAttendanceApi> apiProvider;

  public EventRepositoryImpl_Factory(Provider<EventDao> eventDaoProvider,
      Provider<SupabaseAttendanceApi> apiProvider) {
    this.eventDaoProvider = eventDaoProvider;
    this.apiProvider = apiProvider;
  }

  @Override
  public EventRepositoryImpl get() {
    return newInstance(eventDaoProvider.get(), apiProvider.get());
  }

  public static EventRepositoryImpl_Factory create(Provider<EventDao> eventDaoProvider,
      Provider<SupabaseAttendanceApi> apiProvider) {
    return new EventRepositoryImpl_Factory(eventDaoProvider, apiProvider);
  }

  public static EventRepositoryImpl newInstance(EventDao eventDao, SupabaseAttendanceApi api) {
    return new EventRepositoryImpl(eventDao, api);
  }
}
