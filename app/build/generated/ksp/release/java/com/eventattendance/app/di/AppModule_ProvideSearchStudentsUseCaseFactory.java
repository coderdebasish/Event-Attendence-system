package com.eventattendance.app.di;

import com.eventattendance.app.domain.usecase.SearchStudentsUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AppModule_ProvideSearchStudentsUseCaseFactory implements Factory<SearchStudentsUseCase> {
  @Override
  public SearchStudentsUseCase get() {
    return provideSearchStudentsUseCase();
  }

  public static AppModule_ProvideSearchStudentsUseCaseFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SearchStudentsUseCase provideSearchStudentsUseCase() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSearchStudentsUseCase());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideSearchStudentsUseCaseFactory INSTANCE = new AppModule_ProvideSearchStudentsUseCaseFactory();
  }
}
