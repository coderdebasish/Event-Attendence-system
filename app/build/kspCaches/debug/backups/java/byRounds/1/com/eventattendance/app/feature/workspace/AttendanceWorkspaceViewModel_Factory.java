package com.eventattendance.app.feature.workspace;

import androidx.lifecycle.SavedStateHandle;
import com.eventattendance.app.data.local.UserPreferences;
import com.eventattendance.app.data.remote.SupabaseRealtimeManager;
import com.eventattendance.app.data.repository.AttendanceRepository;
import com.eventattendance.app.data.repository.EventRepository;
import com.eventattendance.app.domain.usecase.SearchStudentsUseCase;
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
public final class AttendanceWorkspaceViewModel_Factory implements Factory<AttendanceWorkspaceViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<AttendanceRepository> attendanceRepositoryProvider;

  private final Provider<EventRepository> eventRepositoryProvider;

  private final Provider<SearchStudentsUseCase> searchUseCaseProvider;

  private final Provider<SupabaseRealtimeManager> realtimeManagerProvider;

  private final Provider<UserPreferences> preferencesProvider;

  public AttendanceWorkspaceViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<AttendanceRepository> attendanceRepositoryProvider,
      Provider<EventRepository> eventRepositoryProvider,
      Provider<SearchStudentsUseCase> searchUseCaseProvider,
      Provider<SupabaseRealtimeManager> realtimeManagerProvider,
      Provider<UserPreferences> preferencesProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.attendanceRepositoryProvider = attendanceRepositoryProvider;
    this.eventRepositoryProvider = eventRepositoryProvider;
    this.searchUseCaseProvider = searchUseCaseProvider;
    this.realtimeManagerProvider = realtimeManagerProvider;
    this.preferencesProvider = preferencesProvider;
  }

  @Override
  public AttendanceWorkspaceViewModel get() {
    return newInstance(savedStateHandleProvider.get(), attendanceRepositoryProvider.get(), eventRepositoryProvider.get(), searchUseCaseProvider.get(), realtimeManagerProvider.get(), preferencesProvider.get());
  }

  public static AttendanceWorkspaceViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<AttendanceRepository> attendanceRepositoryProvider,
      Provider<EventRepository> eventRepositoryProvider,
      Provider<SearchStudentsUseCase> searchUseCaseProvider,
      Provider<SupabaseRealtimeManager> realtimeManagerProvider,
      Provider<UserPreferences> preferencesProvider) {
    return new AttendanceWorkspaceViewModel_Factory(savedStateHandleProvider, attendanceRepositoryProvider, eventRepositoryProvider, searchUseCaseProvider, realtimeManagerProvider, preferencesProvider);
  }

  public static AttendanceWorkspaceViewModel newInstance(SavedStateHandle savedStateHandle,
      AttendanceRepository attendanceRepository, EventRepository eventRepository,
      SearchStudentsUseCase searchUseCase, SupabaseRealtimeManager realtimeManager,
      UserPreferences preferences) {
    return new AttendanceWorkspaceViewModel(savedStateHandle, attendanceRepository, eventRepository, searchUseCase, realtimeManager, preferences);
  }
}
