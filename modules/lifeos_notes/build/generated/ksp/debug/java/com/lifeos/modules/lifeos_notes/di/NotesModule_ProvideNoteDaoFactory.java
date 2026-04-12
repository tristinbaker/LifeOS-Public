package com.lifeos.modules.lifeos_notes.di;

import com.lifeos.modules.lifeos_notes.data.local.NoteDao;
import com.lifeos.modules.lifeos_notes.data.local.NotesDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
    "KotlinInternalInJava"
})
public final class NotesModule_ProvideNoteDaoFactory implements Factory<NoteDao> {
  private final Provider<NotesDatabase> databaseProvider;

  public NotesModule_ProvideNoteDaoFactory(Provider<NotesDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public NoteDao get() {
    return provideNoteDao(databaseProvider.get());
  }

  public static NotesModule_ProvideNoteDaoFactory create(Provider<NotesDatabase> databaseProvider) {
    return new NotesModule_ProvideNoteDaoFactory(databaseProvider);
  }

  public static NoteDao provideNoteDao(NotesDatabase database) {
    return Preconditions.checkNotNullFromProvides(NotesModule.INSTANCE.provideNoteDao(database));
  }
}
