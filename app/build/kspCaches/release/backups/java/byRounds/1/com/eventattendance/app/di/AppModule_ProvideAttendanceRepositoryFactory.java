package com.eventattendance.app.di;

import com.eventattendance.app.data.local.AttendanceDao;
import com.eventattendance.app.data.local.StudentDao;
import com.eventattendance.app.data.remote.SupabaseAttendanceApi;
import com.eventattendance.app.data.repository.AttendanceRepository;
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
public final class AppModule_ProvideAttendanceRepositoryFactory implements Factory<AttendanceRepository> {
  private final Provider<StudentDao> studentDaoProvider;

  private final Provider<AttendanceDao> attendanceDaoProvider;

  private final Provider<SupabaseAttendanceApi> apiProvider;

  public AppModule_ProvideAttendanceRepositoryFactory(Provider<StudentDao> studentDaoProvider,
      Provider<AttendanceDao> attendanceDaoProvider, Provider<SupabaseAttendanceApi> apiProvider) {
    this.studentDaoProvider = studentDaoProvider;
    this.attendanceDaoProvider = attendanceDaoProvider;
    this.apiProvider = apiProvider;
  }

  @Override
  public AttendanceRepository get() {
    return provideAttendanceRepository(studentDaoProvider.get(), attendanceDaoProvider.get(), apiProvider.get());
  }

  public static AppModule_ProvideAttendanceRepositoryFactory create(
      Provider<StudentDao> studentDaoProvider, Provider<AttendanceDao> attendanceDaoProvider,
      Provider<SupabaseAttendanceApi> apiProvider) {
    return new AppModule_ProvideAttendanceRepositoryFactory(studentDaoProvider, attendanceDaoProvider, apiProvider);
  }

  public static AttendanceRepository provideAttendanceRepository(StudentDao studentDao,
      AttendanceDao attendanceDao, SupabaseAttendanceApi api) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideAttendanceRepository(studentDao, attendanceDao, api));
  }
}
