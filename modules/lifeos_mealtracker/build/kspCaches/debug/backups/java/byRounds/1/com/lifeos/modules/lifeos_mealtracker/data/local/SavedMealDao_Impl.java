package com.lifeos.modules.lifeos_mealtracker.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.lifeos.modules.lifeos_mealtracker.domain.model.MealType;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SavedMealDao_Impl implements SavedMealDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SavedMealEntity> __insertionAdapterOfSavedMealEntity;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<SavedMealEntity> __deletionAdapterOfSavedMealEntity;

  private final EntityDeletionOrUpdateAdapter<SavedMealEntity> __updateAdapterOfSavedMealEntity;

  public SavedMealDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSavedMealEntity = new EntityInsertionAdapter<SavedMealEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `lifeos_mealtracker_saved_meals` (`id`,`name`,`mealType`,`calories`,`protein`,`carbs`,`fat`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SavedMealEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        final String _tmp = __converters.fromMealType(entity.getMealType());
        statement.bindString(3, _tmp);
        statement.bindLong(4, entity.getCalories());
        statement.bindLong(5, entity.getProtein());
        statement.bindLong(6, entity.getCarbs());
        statement.bindLong(7, entity.getFat());
      }
    };
    this.__deletionAdapterOfSavedMealEntity = new EntityDeletionOrUpdateAdapter<SavedMealEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `lifeos_mealtracker_saved_meals` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SavedMealEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfSavedMealEntity = new EntityDeletionOrUpdateAdapter<SavedMealEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `lifeos_mealtracker_saved_meals` SET `id` = ?,`name` = ?,`mealType` = ?,`calories` = ?,`protein` = ?,`carbs` = ?,`fat` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SavedMealEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        final String _tmp = __converters.fromMealType(entity.getMealType());
        statement.bindString(3, _tmp);
        statement.bindLong(4, entity.getCalories());
        statement.bindLong(5, entity.getProtein());
        statement.bindLong(6, entity.getCarbs());
        statement.bindLong(7, entity.getFat());
        statement.bindLong(8, entity.getId());
      }
    };
  }

  @Override
  public Object insertSavedMeal(final SavedMealEntity meal,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfSavedMealEntity.insertAndReturnId(meal);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSavedMeal(final SavedMealEntity meal,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfSavedMealEntity.handle(meal);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSavedMeal(final SavedMealEntity meal,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfSavedMealEntity.handle(meal);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<SavedMealEntity>> getAllSavedMeals() {
    final String _sql = "SELECT * FROM lifeos_mealtracker_saved_meals ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"lifeos_mealtracker_saved_meals"}, new Callable<List<SavedMealEntity>>() {
      @Override
      @NonNull
      public List<SavedMealEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfMealType = CursorUtil.getColumnIndexOrThrow(_cursor, "mealType");
          final int _cursorIndexOfCalories = CursorUtil.getColumnIndexOrThrow(_cursor, "calories");
          final int _cursorIndexOfProtein = CursorUtil.getColumnIndexOrThrow(_cursor, "protein");
          final int _cursorIndexOfCarbs = CursorUtil.getColumnIndexOrThrow(_cursor, "carbs");
          final int _cursorIndexOfFat = CursorUtil.getColumnIndexOrThrow(_cursor, "fat");
          final List<SavedMealEntity> _result = new ArrayList<SavedMealEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SavedMealEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final MealType _tmpMealType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfMealType);
            _tmpMealType = __converters.toMealType(_tmp);
            final int _tmpCalories;
            _tmpCalories = _cursor.getInt(_cursorIndexOfCalories);
            final int _tmpProtein;
            _tmpProtein = _cursor.getInt(_cursorIndexOfProtein);
            final int _tmpCarbs;
            _tmpCarbs = _cursor.getInt(_cursorIndexOfCarbs);
            final int _tmpFat;
            _tmpFat = _cursor.getInt(_cursorIndexOfFat);
            _item = new SavedMealEntity(_tmpId,_tmpName,_tmpMealType,_tmpCalories,_tmpProtein,_tmpCarbs,_tmpFat);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getSavedMealById(final long id,
      final Continuation<? super SavedMealEntity> $completion) {
    final String _sql = "SELECT * FROM lifeos_mealtracker_saved_meals WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SavedMealEntity>() {
      @Override
      @Nullable
      public SavedMealEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfMealType = CursorUtil.getColumnIndexOrThrow(_cursor, "mealType");
          final int _cursorIndexOfCalories = CursorUtil.getColumnIndexOrThrow(_cursor, "calories");
          final int _cursorIndexOfProtein = CursorUtil.getColumnIndexOrThrow(_cursor, "protein");
          final int _cursorIndexOfCarbs = CursorUtil.getColumnIndexOrThrow(_cursor, "carbs");
          final int _cursorIndexOfFat = CursorUtil.getColumnIndexOrThrow(_cursor, "fat");
          final SavedMealEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final MealType _tmpMealType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfMealType);
            _tmpMealType = __converters.toMealType(_tmp);
            final int _tmpCalories;
            _tmpCalories = _cursor.getInt(_cursorIndexOfCalories);
            final int _tmpProtein;
            _tmpProtein = _cursor.getInt(_cursorIndexOfProtein);
            final int _tmpCarbs;
            _tmpCarbs = _cursor.getInt(_cursorIndexOfCarbs);
            final int _tmpFat;
            _tmpFat = _cursor.getInt(_cursorIndexOfFat);
            _result = new SavedMealEntity(_tmpId,_tmpName,_tmpMealType,_tmpCalories,_tmpProtein,_tmpCarbs,_tmpFat);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
