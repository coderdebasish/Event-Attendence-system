package com.eventattendance.app.data.repository;

import com.eventattendance.app.data.local.AttendanceDao;
import com.eventattendance.app.data.local.StudentDao;
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
public final class AttendanceRepositoryImpl_Factory implements Factory<AttendanceRepositoryImpl> {
  private final Provider<StudentDao> studentDaoProvider;

  private final Provider<AttendanceDao> attendanceDaoProvider;

  private final Provider<SupabaseAttendanceApi> apiProvider;

  public AttendanceRepositoryImpl_Factory(Provider<StudentDao> studentDaoProvider,
      Provider<AttendanceDao> attendanceDaoProvider, Provider<SupabaseAttendanceApi> apiProvider) {
    this.studentDaoProvider = studentDaoProvider;
    this.attendanceDaoProvider = attendanceDaoProvider;
    this.apiProvider = apiProvider;
  }

  @Override
  public AttendanceRepositoryImpl get() {
    return newInstance(studentDaoProvider.get(), attendanceDaoProvider.get(), apiProvider.get());
  }

  public static AttendanceRepositoryImpl_Factory create(Provider<StudentDao> studentDaoProvider,
      Provider<AttendanceDao> attendanceDaoProvider, Provider<SupabaseAttendanceApi> apiProvider) {
    return new AttendanceRepositoryImpl_Factory(studentDaoProvider, attendanceDaoProvider, apiProvider);
  }

  public static AttendanceRepositoryImpl newInstance(StudentDao studentDao,
      AttendanceDao attendanceDao, SupabaseAttendanceApi api) {
    return new AttendanceRepositoryImpl(studentDao, attendanceDao, api);
  }
}
