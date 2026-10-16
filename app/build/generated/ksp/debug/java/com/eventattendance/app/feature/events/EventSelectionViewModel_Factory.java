package com.eventattendance.app.feature.events;

import com.eventattendance.app.data.local.UserPreferences;
import com.eventattendance.app.data.repository.AttendanceRepository;
import com.eventattendance.app.data.repository.AuthRepository;
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
public final class EventSelectionViewModel_Factory implements Factory<EventSelectionViewModel> {
  private final Provider<EventRepository> eventRepositoryProvider;

  private final Provider<AttendanceRepository> attendanceRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<UserPreferences> preferencesProvider;

  public EventSelectionViewModel_Factory(Provider<EventRepository> eventRepositoryProvider,
      Provider<AttendanceRepository> attendanceRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<UserPreferences> preferencesProvider) {
    this.eventRepositoryProvider = eventRepositoryProvider;
    this.attendanceRepositoryProvider = attendanceRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
    this.preferencesProvider = preferencesProvider;
  }

  @Override
  public EventSelectionViewModel get() {
    return newInstance(eventRepositoryProvider.get(), attendanceRepositoryProvider.get(), authRepositoryProvider.get(), preferencesProvider.get());
  }

  public static EventSelectionViewModel_Factory create(
      Provider<EventRepository> eventRepositoryProvider,
      Provider<AttendanceRepository> attendanceRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<UserPreferences> preferencesProvider) {
    return new EventSelectionViewModel_Factory(eventRepositoryProvider, attendanceRepositoryProvider, authRepositoryProvider, preferencesProvider);
  }

  public static EventSelectionViewModel newInstance(EventRepository eventRepository,
      AttendanceRepository attendanceRepository, AuthRepository authRepository,
      UserPreferences preferences) {
    return new EventSelectionViewModel(eventRepository, attendanceRepository, authRepository, preferences);
  }
}
