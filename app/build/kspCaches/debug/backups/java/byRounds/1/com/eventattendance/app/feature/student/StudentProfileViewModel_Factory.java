package com.eventattendance.app.feature.student;

import androidx.lifecycle.SavedStateHandle;
import com.eventattendance.app.core.audio.SoundFeedbackManager;
import com.eventattendance.app.data.local.UserPreferences;
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
public final class StudentProfileViewModel_Factory implements Factory<StudentProfileViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<AttendanceRepository> attendanceRepositoryProvider;

  private final Provider<EventRepository> eventRepositoryProvider;

  private final Provider<SoundFeedbackManager> soundManagerProvider;

  private final Provider<UserPreferences> preferencesProvider;

  public StudentProfileViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<AttendanceRepository> attendanceRepositoryProvider,
      Provider<EventRepository> eventRepositoryProvider,
      Provider<SoundFeedbackManager> soundManagerProvider,
      Provider<UserPreferences> preferencesProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.attendanceRepositoryProvider = attendanceRepositoryProvider;
    this.eventRepositoryProvider = eventRepositoryProvider;
    this.soundManagerProvider = soundManagerProvider;
    this.preferencesProvider = preferencesProvider;
  }

  @Override
  public StudentProfileViewModel get() {
    return newInstance(savedStateHandleProvider.get(), attendanceRepositoryProvider.get(), eventRepositoryProvider.get(), soundManagerProvider.get(), preferencesProvider.get());
  }

  public static StudentProfileViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<AttendanceRepository> attendanceRepositoryProvider,
      Provider<EventRepository> eventRepositoryProvider,
      Provider<SoundFeedbackManager> soundManagerProvider,
      Provider<UserPreferences> preferencesProvider) {
    return new StudentProfileViewModel_Factory(savedStateHandleProvider, attendanceRepositoryProvider, eventRepositoryProvider, soundManagerProvider, preferencesProvider);
  }

  public static StudentProfileViewModel newInstance(SavedStateHandle savedStateHandle,
      AttendanceRepository attendanceRepository, EventRepository eventRepository,
      SoundFeedbackManager soundManager, UserPreferences preferences) {
    return new StudentProfileViewModel(savedStateHandle, attendanceRepository, eventRepository, soundManager, preferences);
  }
}
