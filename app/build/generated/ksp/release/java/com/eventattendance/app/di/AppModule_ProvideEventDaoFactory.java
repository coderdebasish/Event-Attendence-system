package com.eventattendance.app.di;

import com.eventattendance.app.data.local.AppDatabase;
import com.eventattendance.app.data.local.EventDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideEventDaoFactory implements Factory<EventDao> {
  private final Provider<AppDatabase> databaseProvider;

  public AppModule_ProvideEventDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public EventDao get() {
    return provideEventDao(databaseProvider.get());
  }

  public static AppModule_ProvideEventDaoFactory create(Provider<AppDatabase> databaseProvider) {
    return new AppModule_ProvideEventDaoFactory(databaseProvider);
  }

  public static EventDao provideEventDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideEventDao(database));
  }
}
