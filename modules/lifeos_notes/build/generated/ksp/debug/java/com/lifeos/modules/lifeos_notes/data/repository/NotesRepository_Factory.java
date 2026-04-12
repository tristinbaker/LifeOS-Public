package com.lifeos.modules.lifeos_notes.data.repository;

import com.lifeos.modules.lifeos_notes.data.local.NoteDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class NotesRepository_Factory implements Factory<NotesRepository> {
  private final Provider<NoteDao> noteDaoProvider;

  public NotesRepository_Factory(Provider<NoteDao> noteDaoProvider) {
    this.noteDaoProvider = noteDaoProvider;
  }

  @Override
  public NotesRepository get() {
    return newInstance(noteDaoProvider.get());
  }

  public static NotesRepository_Factory create(Provider<NoteDao> noteDaoProvider) {
    return new NotesRepository_Factory(noteDaoProvider);
  }

  public static NotesRepository newInstance(NoteDao noteDao) {
    return new NotesRepository(noteDao);
  }
}
