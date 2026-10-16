package com.eventattendance.app.feature.importexport;

import androidx.lifecycle.SavedStateHandle;
import com.eventattendance.app.data.remote.SupabaseAttendanceApi;
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
public final class SpreadsheetImportViewModel_Factory implements Factory<SpreadsheetImportViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<AttendanceRepository> attendanceRepositoryProvider;

  private final Provider<SupabaseAttendanceApi> apiProvider;

  public SpreadsheetImportViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<AttendanceRepository> attendanceRepositoryProvider,
      Provider<SupabaseAttendanceApi> apiProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.attendanceRepositoryProvider = attendanceRepositoryProvider;
    this.apiProvider = apiProvider;
  }

  @Override
  public SpreadsheetImportViewModel get() {
    return newInstance(savedStateHandleProvider.get(), attendanceRepositoryProvider.get(), apiProvider.get());
  }

  public static SpreadsheetImportViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<AttendanceRepository> attendanceRepositoryProvider,
      Provider<SupabaseAttendanceApi> apiProvider) {
    return new SpreadsheetImportViewModel_Factory(savedStateHandleProvider, attendanceRepositoryProvider, apiProvider);
  }

  public static SpreadsheetImportViewModel newInstance(SavedStateHandle savedStateHandle,
      AttendanceRepository attendanceRepository, SupabaseAttendanceApi api) {
    return new SpreadsheetImportViewModel(savedStateHandle, attendanceRepository, api);
  }
}
