package com.eventattendance.app.di;

import com.eventattendance.app.data.local.EventDao;
import com.eventattendance.app.data.remote.SupabaseAttendanceApi;
import com.eventattendance.app.data.repository.EventRepository;
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
public final class AppModule_ProvideEventRepositoryFactory implements Factory<EventRepository> {
  private final Provider<EventDao> eventDaoProvider;

  private final Provider<SupabaseAttendanceApi> apiProvider;

  public AppModule_ProvideEventRepositoryFactory(Provider<EventDao> eventDaoProvider,
      Provider<SupabaseAttendanceApi> apiProvider) {
    this.eventDaoProvider = eventDaoProvider;
    this.apiProvider = apiProvider;
  }

  @Override
  public EventRepository get() {
    return provideEventRepository(eventDaoProvider.get(), apiProvider.get());
  }

  public static AppModule_ProvideEventRepositoryFactory create(Provider<EventDao> eventDaoProvider,
      Provider<SupabaseAttendanceApi> apiProvider) {
    return new AppModule_ProvideEventRepositoryFactory(eventDaoProvider, apiProvider);
  }

  public static EventRepository provideEventRepository(EventDao eventDao,
      SupabaseAttendanceApi api) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideEventRepository(eventDao, api));
  }
}
