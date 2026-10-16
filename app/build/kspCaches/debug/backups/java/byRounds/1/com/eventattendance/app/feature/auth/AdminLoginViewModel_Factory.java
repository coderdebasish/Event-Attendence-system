package com.eventattendance.app.feature.auth;

import com.eventattendance.app.data.repository.AuthRepository;
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
public final class AdminLoginViewModel_Factory implements Factory<AdminLoginViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  public AdminLoginViewModel_Factory(Provider<AuthRepository> authRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public AdminLoginViewModel get() {
    return newInstance(authRepositoryProvider.get());
  }

  public static AdminLoginViewModel_Factory create(
      Provider<AuthRepository> authRepositoryProvider) {
    return new AdminLoginViewModel_Factory(authRepositoryProvider);
  }

  public static AdminLoginViewModel newInstance(AuthRepository authRepository) {
    return new AdminLoginViewModel(authRepository);
  }
}
