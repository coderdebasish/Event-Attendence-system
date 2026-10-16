package com.eventattendance.app.feature.admin;

import androidx.lifecycle.SavedStateHandle;
import com.eventattendance.app.data.repository.AttendanceRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class AttendanceMonitoringViewModel_Factory implements Factory<AttendanceMonitoringViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<AttendanceRepository> attendanceRepositoryProvider;

  public AttendanceMonitoringViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<AttendanceRepository> attendanceRepositoryProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.attendanceRepositoryProvider = attendanceRepositoryProvider;
  }

  @Override
  public AttendanceMonitoringViewModel get() {
    return newInstance(savedStateHandleProvider.get(), attendanceRepositoryProvider.get());
  }

  public static AttendanceMonitoringViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<AttendanceRepository> attendanceRepositoryProvider) {
    return new AttendanceMonitoringViewModel_Factory(savedStateHandleProvider, attendanceRepositoryProvider);
  }

  public static AttendanceMonitoringViewModel newInstance(SavedStateHandle savedStateHandle,
      AttendanceRepository attendanceRepository) {
    return new AttendanceMonitoringViewModel(savedStateHandle, attendanceRepository);
  }
}
