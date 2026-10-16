package com.eventattendance.app.feature.admin;

import androidx.lifecycle.SavedStateHandle;
import com.eventattendance.app.data.remote.SupabaseAttendanceApi;
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
public final class AuditLogViewModel_Factory implements Factory<AuditLogViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<SupabaseAttendanceApi> apiProvider;

  public AuditLogViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<SupabaseAttendanceApi> apiProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.apiProvider = apiProvider;
  }

  @Override
  public AuditLogViewModel get() {
    return newInstance(savedStateHandleProvider.get(), apiProvider.get());
  }

  public static AuditLogViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<SupabaseAttendanceApi> apiProvider) {
    return new AuditLogViewModel_Factory(savedStateHandleProvider, apiProvider);
  }

  public static AuditLogViewModel newInstance(SavedStateHandle savedStateHandle,
      SupabaseAttendanceApi api) {
    return new AuditLogViewModel(savedStateHandle, api);
  }
}
