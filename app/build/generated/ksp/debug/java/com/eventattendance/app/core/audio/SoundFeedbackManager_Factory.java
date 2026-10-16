package com.eventattendance.app.core.audio;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class SoundFeedbackManager_Factory implements Factory<SoundFeedbackManager> {
  private final Provider<Context> contextProvider;

  public SoundFeedbackManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SoundFeedbackManager get() {
    return newInstance(contextProvider.get());
  }

  public static SoundFeedbackManager_Factory create(Provider<Context> contextProvider) {
    return new SoundFeedbackManager_Factory(contextProvider);
  }

  public static SoundFeedbackManager newInstance(Context context) {
    return new SoundFeedbackManager(context);
  }
}
