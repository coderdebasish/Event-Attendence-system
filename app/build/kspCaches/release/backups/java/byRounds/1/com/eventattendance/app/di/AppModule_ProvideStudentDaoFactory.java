package com.eventattendance.app.di;

import com.eventattendance.app.data.local.AppDatabase;
import com.eventattendance.app.data.local.StudentDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideStudentDaoFactory implements Factory<StudentDao> {
  private final Provider<AppDatabase> databaseProvider;

  public AppModule_ProvideStudentDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public StudentDao get() {
    return provideStudentDao(databaseProvider.get());
  }

  public static AppModule_ProvideStudentDaoFactory create(Provider<AppDatabase> databaseProvider) {
    return new AppModule_ProvideStudentDaoFactory(databaseProvider);
  }

  public static StudentDao provideStudentDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideStudentDao(database));
  }
}
