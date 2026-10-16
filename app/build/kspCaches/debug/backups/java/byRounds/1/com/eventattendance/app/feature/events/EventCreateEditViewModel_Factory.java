package com.eventattendance.app.feature.events;

import androidx.lifecycle.SavedStateHandle;
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
public final class EventCreateEditViewModel_Factory implements Factory<EventCreateEditViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<EventRepository> eventRepositoryProvider;

  public EventCreateEditViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<EventRepository> eventRepositoryProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.eventRepositoryProvider = eventRepositoryProvider;
  }

  @Override
  public EventCreateEditViewModel get() {
    return newInstance(savedStateHandleProvider.get(), eventRepositoryProvider.get());
  }

  public static EventCreateEditViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<EventRepository> eventRepositoryProvider) {
    return new EventCreateEditViewModel_Factory(savedStateHandleProvider, eventRepositoryProvider);
  }

  public static EventCreateEditViewModel newInstance(SavedStateHandle savedStateHandle,
      EventRepository eventRepository) {
    return new EventCreateEditViewModel(savedStateHandle, eventRepository);
  }
}
