package com.eventattendance.app.di;

import com.eventattendance.app.data.local.AppDatabase;
import com.eventattendance.app.data.local.AttendanceDao;
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
public final class AppModule_ProvideAttendanceDaoFactory implements Factory<AttendanceDao> {
  private final Provider<AppDatabase> databaseProvider;

  public AppModule_ProvideAttendanceDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public AttendanceDao get() {
    return provideAttendanceDao(databaseProvider.get());
  }

  public static AppModule_ProvideAttendanceDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new AppModule_ProvideAttendanceDaoFactory(databaseProvider);
  }

  public static AttendanceDao provideAttendanceDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideAttendanceDao(database));
  }
}
