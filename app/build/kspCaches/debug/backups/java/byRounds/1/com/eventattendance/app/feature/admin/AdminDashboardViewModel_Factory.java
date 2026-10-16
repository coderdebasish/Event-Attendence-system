package com.eventattendance.app.feature.admin;

import androidx.lifecycle.SavedStateHandle;
import com.eventattendance.app.data.repository.AttendanceRepository;
import com.eventattendance.app.data.repository.EventRepository;
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
public final class AdminDashboardViewModel_Factory implements Factory<AdminDashboardViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<EventRepository> eventRepositoryProvider;

  private final Provider<AttendanceRepository> attendanceRepositoryProvider;

  public AdminDashboardViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<EventRepository> eventRepositoryProvider,
      Provider<AttendanceRepository> attendanceRepositoryProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.eventRepositoryProvider = eventRepositoryProvider;
    this.attendanceRepositoryProvider = attendanceRepositoryProvider;
  }

  @Override
  public AdminDashboardViewModel get() {
    return newInstance(savedStateHandleProvider.get(), eventRepositoryProvider.get(), attendanceRepositoryProvider.get());
  }

  public static AdminDashboardViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<EventRepository> eventRepositoryProvider,
      Provider<AttendanceRepository> attendanceRepositoryProvider) {
    return new AdminDashboardViewModel_Factory(savedStateHandleProvider, eventRepositoryProvider, attendanceRepositoryProvider);
  }

  public static AdminDashboardViewModel newInstance(SavedStateHandle savedStateHandle,
      EventRepository eventRepository, AttendanceRepository attendanceRepository) {
    return new AdminDashboardViewModel(savedStateHandle, eventRepository, attendanceRepository);
  }
}
