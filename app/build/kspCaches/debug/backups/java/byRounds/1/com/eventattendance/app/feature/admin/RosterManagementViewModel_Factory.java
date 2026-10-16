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
public final class RosterManagementViewModel_Factory implements Factory<RosterManagementViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<AttendanceRepository> attendanceRepositoryProvider;

  public RosterManagementViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<AttendanceRepository> attendanceRepositoryProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.attendanceRepositoryProvider = attendanceRepositoryProvider;
  }

  @Override
  public RosterManagementViewModel get() {
    return newInstance(savedStateHandleProvider.get(), attendanceRepositoryProvider.get());
  }

  public static RosterManagementViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<AttendanceRepository> attendanceRepositoryProvider) {
    return new RosterManagementViewModel_Factory(savedStateHandleProvider, attendanceRepositoryProvider);
  }

  public static RosterManagementViewModel newInstance(SavedStateHandle savedStateHandle,
      AttendanceRepository attendanceRepository) {
    return new RosterManagementViewModel(savedStateHandle, attendanceRepository);
  }
}
