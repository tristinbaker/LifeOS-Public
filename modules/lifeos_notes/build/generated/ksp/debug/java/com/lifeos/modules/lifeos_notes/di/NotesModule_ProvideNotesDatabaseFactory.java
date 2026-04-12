package com.lifeos.modules.lifeos_notes.di;

import android.content.Context;
import com.lifeos.modules.lifeos_notes.data.local.NotesDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
    "KotlinInternalInJava"
})
public final class NotesModule_ProvideNotesDatabaseFactory implements Factory<NotesDatabase> {
  private final Provider<Context> contextProvider;

  public NotesModule_ProvideNotesDatabaseFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public NotesDatabase get() {
    return provideNotesDatabase(contextProvider.get());
  }

  public static NotesModule_ProvideNotesDatabaseFactory create(Provider<Context> contextProvider) {
    return new NotesModule_ProvideNotesDatabaseFactory(contextProvider);
  }

  public static NotesDatabase provideNotesDatabase(Context context) {
    return Preconditions.checkNotNullFromProvides(NotesModule.INSTANCE.provideNotesDatabase(context));
  }
}
