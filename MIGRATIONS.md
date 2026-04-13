# Room Database Migration Workflow

## Setup (already done)

Each module's `@Database` class has `exportSchema = true` and each `build.gradle.kts` has:

```kotlin
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
```

On every build, Room writes a `schemas/<version>.json` file inside the module directory. **Commit these files** — they are the source of truth for what each version's schema looked like.

## When you change a table

1. **Edit the entity class** (add/remove/rename a column, change a type, etc.)

2. **Bump the version** in `@Database`:
   ```kotlin
   @Database(entities = [...], version = 4, exportSchema = true)
   ```

3. **Write the migration** in the database file (e.g. `NotesDatabase.kt`):
   ```kotlin
   val MIGRATION_3_4 = object : Migration(3, 4) {
       override fun migrate(database: SupportSQLiteDatabase) {
           // Adding a column (safe — existing rows get null/default):
           database.execSQL("ALTER TABLE lifeos_notes ADD COLUMN tags TEXT")

           // Changing a column type or removing a column — must recreate the table:
           database.execSQL("CREATE TABLE lifeos_notes_new (...all columns...)")
           database.execSQL("INSERT INTO lifeos_notes_new SELECT col1, col2, ... FROM lifeos_notes")
           database.execSQL("DROP TABLE lifeos_notes")
           database.execSQL("ALTER TABLE lifeos_notes_new RENAME TO lifeos_notes")
       }
   }
   ```
   **Always list columns explicitly in INSERT SELECT** — never use `SELECT *`. If you forget a column, its data is silently dropped.

4. **Register the migration** in the DI module:
   ```kotlin
   Room.databaseBuilder(context, NotesDatabase::class.java, "lifeos_notes.db")
       .addMigrations(MIGRATION_3_4)
       .build()
   ```

5. **Build the project** — Room generates a new `schemas/4.json`. Commit it alongside your entity and migration changes.

## Quick reference: ALTER TABLE vs recreate

| Change | How |
|--------|-----|
| Add nullable column | `ALTER TABLE t ADD COLUMN col TYPE` |
| Add column with default | `ALTER TABLE t ADD COLUMN col TYPE DEFAULT value` |
| Remove a column | Recreate table |
| Rename a column | Recreate table |
| Change column type | Recreate table |
| Add/remove index | `CREATE INDEX ...` / `DROP INDEX ...` |

## Module database names

| Module | Database file |
|--------|--------------|
| lifeos_notes | `lifeos_notes.db` |
| lifeos_mealtracker | `lifeos_mealtracker.db` |
| lifeos_habittracker | `lifeos_habittracker.db` |
| lifeos_medialogger | `medialogger.db` |
