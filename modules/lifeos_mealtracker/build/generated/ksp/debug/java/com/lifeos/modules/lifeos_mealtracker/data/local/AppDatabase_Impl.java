package com.lifeos.modules.lifeos_mealtracker.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile MealEntryDao _mealEntryDao;

  private volatile SavedMealDao _savedMealDao;

  private volatile WeightEntryDao _weightEntryDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `lifeos_mealtracker_meal_entries` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT NOT NULL, `mealType` TEXT NOT NULL, `name` TEXT, `calories` INTEGER NOT NULL, `protein` INTEGER NOT NULL, `carbs` INTEGER NOT NULL, `fat` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `lifeos_mealtracker_saved_meals` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `mealType` TEXT NOT NULL, `calories` INTEGER NOT NULL, `protein` INTEGER NOT NULL, `carbs` INTEGER NOT NULL, `fat` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `lifeos_mealtracker_weight_entries` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT NOT NULL, `weight` REAL NOT NULL, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '850bf7ff8b5a199cdf0e40ab60de19c7')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `lifeos_mealtracker_meal_entries`");
        db.execSQL("DROP TABLE IF EXISTS `lifeos_mealtracker_saved_meals`");
        db.execSQL("DROP TABLE IF EXISTS `lifeos_mealtracker_weight_entries`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsLifeosMealtrackerMealEntries = new HashMap<String, TableInfo.Column>(9);
        _columnsLifeosMealtrackerMealEntries.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLifeosMealtrackerMealEntries.put("date", new TableInfo.Column("date", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLifeosMealtrackerMealEntries.put("mealType", new TableInfo.Column("mealType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLifeosMealtrackerMealEntries.put("name", new TableInfo.Column("name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLifeosMealtrackerMealEntries.put("calories", new TableInfo.Column("calories", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLifeosMealtrackerMealEntries.put("protein", new TableInfo.Column("protein", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLifeosMealtrackerMealEntries.put("carbs", new TableInfo.Column("carbs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLifeosMealtrackerMealEntries.put("fat", new TableInfo.Column("fat", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLifeosMealtrackerMealEntries.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysLifeosMealtrackerMealEntries = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesLifeosMealtrackerMealEntries = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoLifeosMealtrackerMealEntries = new TableInfo("lifeos_mealtracker_meal_entries", _columnsLifeosMealtrackerMealEntries, _foreignKeysLifeosMealtrackerMealEntries, _indicesLifeosMealtrackerMealEntries);
        final TableInfo _existingLifeosMealtrackerMealEntries = TableInfo.read(db, "lifeos_mealtracker_meal_entries");
        if (!_infoLifeosMealtrackerMealEntries.equals(_existingLifeosMealtrackerMealEntries)) {
          return new RoomOpenHelper.ValidationResult(false, "lifeos_mealtracker_meal_entries(com.lifeos.modules.lifeos_mealtracker.data.local.MealEntryEntity).\n"
                  + " Expected:\n" + _infoLifeosMealtrackerMealEntries + "\n"
                  + " Found:\n" + _existingLifeosMealtrackerMealEntries);
        }
        final HashMap<String, TableInfo.Column> _columnsLifeosMealtrackerSavedMeals = new HashMap<String, TableInfo.Column>(7);
        _columnsLifeosMealtrackerSavedMeals.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLifeosMealtrackerSavedMeals.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLifeosMealtrackerSavedMeals.put("mealType", new TableInfo.Column("mealType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLifeosMealtrackerSavedMeals.put("calories", new TableInfo.Column("calories", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLifeosMealtrackerSavedMeals.put("protein", new TableInfo.Column("protein", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLifeosMealtrackerSavedMeals.put("carbs", new TableInfo.Column("carbs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLifeosMealtrackerSavedMeals.put("fat", new TableInfo.Column("fat", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysLifeosMealtrackerSavedMeals = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesLifeosMealtrackerSavedMeals = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoLifeosMealtrackerSavedMeals = new TableInfo("lifeos_mealtracker_saved_meals", _columnsLifeosMealtrackerSavedMeals, _foreignKeysLifeosMealtrackerSavedMeals, _indicesLifeosMealtrackerSavedMeals);
        final TableInfo _existingLifeosMealtrackerSavedMeals = TableInfo.read(db, "lifeos_mealtracker_saved_meals");
        if (!_infoLifeosMealtrackerSavedMeals.equals(_existingLifeosMealtrackerSavedMeals)) {
          return new RoomOpenHelper.ValidationResult(false, "lifeos_mealtracker_saved_meals(com.lifeos.modules.lifeos_mealtracker.data.local.SavedMealEntity).\n"
                  + " Expected:\n" + _infoLifeosMealtrackerSavedMeals + "\n"
                  + " Found:\n" + _existingLifeosMealtrackerSavedMeals);
        }
        final HashMap<String, TableInfo.Column> _columnsLifeosMealtrackerWeightEntries = new HashMap<String, TableInfo.Column>(4);
        _columnsLifeosMealtrackerWeightEntries.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLifeosMealtrackerWeightEntries.put("date", new TableInfo.Column("date", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLifeosMealtrackerWeightEntries.put("weight", new TableInfo.Column("weight", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLifeosMealtrackerWeightEntries.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysLifeosMealtrackerWeightEntries = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesLifeosMealtrackerWeightEntries = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoLifeosMealtrackerWeightEntries = new TableInfo("lifeos_mealtracker_weight_entries", _columnsLifeosMealtrackerWeightEntries, _foreignKeysLifeosMealtrackerWeightEntries, _indicesLifeosMealtrackerWeightEntries);
        final TableInfo _existingLifeosMealtrackerWeightEntries = TableInfo.read(db, "lifeos_mealtracker_weight_entries");
        if (!_infoLifeosMealtrackerWeightEntries.equals(_existingLifeosMealtrackerWeightEntries)) {
          return new RoomOpenHelper.ValidationResult(false, "lifeos_mealtracker_weight_entries(com.lifeos.modules.lifeos_mealtracker.data.local.WeightEntryEntity).\n"
                  + " Expected:\n" + _infoLifeosMealtrackerWeightEntries + "\n"
                  + " Found:\n" + _existingLifeosMealtrackerWeightEntries);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "850bf7ff8b5a199cdf0e40ab60de19c7", "67bf143dd056f39536f737d321162ad0");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "lifeos_mealtracker_meal_entries","lifeos_mealtracker_saved_meals","lifeos_mealtracker_weight_entries");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `lifeos_mealtracker_meal_entries`");
      _db.execSQL("DELETE FROM `lifeos_mealtracker_saved_meals`");
      _db.execSQL("DELETE FROM `lifeos_mealtracker_weight_entries`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(MealEntryDao.class, MealEntryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SavedMealDao.class, SavedMealDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(WeightEntryDao.class, WeightEntryDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public MealEntryDao mealEntryDao() {
    if (_mealEntryDao != null) {
      return _mealEntryDao;
    } else {
      synchronized(this) {
        if(_mealEntryDao == null) {
          _mealEntryDao = new MealEntryDao_Impl(this);
        }
        return _mealEntryDao;
      }
    }
  }

  @Override
  public SavedMealDao savedMealDao() {
    if (_savedMealDao != null) {
      return _savedMealDao;
    } else {
      synchronized(this) {
        if(_savedMealDao == null) {
          _savedMealDao = new SavedMealDao_Impl(this);
        }
        return _savedMealDao;
      }
    }
  }

  @Override
  public WeightEntryDao weightEntryDao() {
    if (_weightEntryDao != null) {
      return _weightEntryDao;
    } else {
      synchronized(this) {
        if(_weightEntryDao == null) {
          _weightEntryDao = new WeightEntryDao_Impl(this);
        }
        return _weightEntryDao;
      }
    }
  }
}
