package es.uniovi.recetasasturianas.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import es.uniovi.recetasasturianas.data.model.Recipe;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
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

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class RecipeDao_Impl implements RecipeDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Recipe> __insertionAdapterOfRecipe;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public RecipeDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRecipe = new EntityInsertionAdapter<Recipe>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `recipes` (`id`,`name`,`restaurant`,`preparationHtml`,`ingredientsHtml`,`imageUrl`,`restaurantUrl`,`timeMinutes`,`tipsHtml`,`notesHtml`,`cachedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Recipe entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getRestaurant());
        statement.bindString(4, entity.getPreparationHtml());
        statement.bindString(5, entity.getIngredientsHtml());
        if (entity.getImageUrl() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getImageUrl());
        }
        if (entity.getRestaurantUrl() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getRestaurantUrl());
        }
        if (entity.getTimeMinutes() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getTimeMinutes());
        }
        if (entity.getTipsHtml() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getTipsHtml());
        }
        if (entity.getNotesHtml() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getNotesHtml());
        }
        statement.bindLong(11, entity.getCachedAt());
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM recipes";
        return _query;
      }
    };
  }

  @Override
  public Object insertAll(final List<Recipe> recipes,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfRecipe.insert(recipes);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insert(final Recipe recipe, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfRecipe.insert(recipe);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public LiveData<List<Recipe>> getAll() {
    final String _sql = "SELECT * FROM recipes ORDER BY name COLLATE NOCASE ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"recipes"}, false, new Callable<List<Recipe>>() {
      @Override
      @Nullable
      public List<Recipe> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRestaurant = CursorUtil.getColumnIndexOrThrow(_cursor, "restaurant");
          final int _cursorIndexOfPreparationHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "preparationHtml");
          final int _cursorIndexOfIngredientsHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "ingredientsHtml");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfRestaurantUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "restaurantUrl");
          final int _cursorIndexOfTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timeMinutes");
          final int _cursorIndexOfTipsHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "tipsHtml");
          final int _cursorIndexOfNotesHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "notesHtml");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cachedAt");
          final List<Recipe> _result = new ArrayList<Recipe>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Recipe _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpRestaurant;
            _tmpRestaurant = _cursor.getString(_cursorIndexOfRestaurant);
            final String _tmpPreparationHtml;
            _tmpPreparationHtml = _cursor.getString(_cursorIndexOfPreparationHtml);
            final String _tmpIngredientsHtml;
            _tmpIngredientsHtml = _cursor.getString(_cursorIndexOfIngredientsHtml);
            final String _tmpImageUrl;
            if (_cursor.isNull(_cursorIndexOfImageUrl)) {
              _tmpImageUrl = null;
            } else {
              _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            }
            final String _tmpRestaurantUrl;
            if (_cursor.isNull(_cursorIndexOfRestaurantUrl)) {
              _tmpRestaurantUrl = null;
            } else {
              _tmpRestaurantUrl = _cursor.getString(_cursorIndexOfRestaurantUrl);
            }
            final Integer _tmpTimeMinutes;
            if (_cursor.isNull(_cursorIndexOfTimeMinutes)) {
              _tmpTimeMinutes = null;
            } else {
              _tmpTimeMinutes = _cursor.getInt(_cursorIndexOfTimeMinutes);
            }
            final String _tmpTipsHtml;
            if (_cursor.isNull(_cursorIndexOfTipsHtml)) {
              _tmpTipsHtml = null;
            } else {
              _tmpTipsHtml = _cursor.getString(_cursorIndexOfTipsHtml);
            }
            final String _tmpNotesHtml;
            if (_cursor.isNull(_cursorIndexOfNotesHtml)) {
              _tmpNotesHtml = null;
            } else {
              _tmpNotesHtml = _cursor.getString(_cursorIndexOfNotesHtml);
            }
            final long _tmpCachedAt;
            _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
            _item = new Recipe(_tmpId,_tmpName,_tmpRestaurant,_tmpPreparationHtml,_tmpIngredientsHtml,_tmpImageUrl,_tmpRestaurantUrl,_tmpTimeMinutes,_tmpTipsHtml,_tmpNotesHtml,_tmpCachedAt);
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
  public Object getAllSync(final Continuation<? super List<Recipe>> $completion) {
    final String _sql = "SELECT * FROM recipes ORDER BY name COLLATE NOCASE ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Recipe>>() {
      @Override
      @NonNull
      public List<Recipe> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRestaurant = CursorUtil.getColumnIndexOrThrow(_cursor, "restaurant");
          final int _cursorIndexOfPreparationHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "preparationHtml");
          final int _cursorIndexOfIngredientsHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "ingredientsHtml");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfRestaurantUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "restaurantUrl");
          final int _cursorIndexOfTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timeMinutes");
          final int _cursorIndexOfTipsHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "tipsHtml");
          final int _cursorIndexOfNotesHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "notesHtml");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cachedAt");
          final List<Recipe> _result = new ArrayList<Recipe>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Recipe _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpRestaurant;
            _tmpRestaurant = _cursor.getString(_cursorIndexOfRestaurant);
            final String _tmpPreparationHtml;
            _tmpPreparationHtml = _cursor.getString(_cursorIndexOfPreparationHtml);
            final String _tmpIngredientsHtml;
            _tmpIngredientsHtml = _cursor.getString(_cursorIndexOfIngredientsHtml);
            final String _tmpImageUrl;
            if (_cursor.isNull(_cursorIndexOfImageUrl)) {
              _tmpImageUrl = null;
            } else {
              _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            }
            final String _tmpRestaurantUrl;
            if (_cursor.isNull(_cursorIndexOfRestaurantUrl)) {
              _tmpRestaurantUrl = null;
            } else {
              _tmpRestaurantUrl = _cursor.getString(_cursorIndexOfRestaurantUrl);
            }
            final Integer _tmpTimeMinutes;
            if (_cursor.isNull(_cursorIndexOfTimeMinutes)) {
              _tmpTimeMinutes = null;
            } else {
              _tmpTimeMinutes = _cursor.getInt(_cursorIndexOfTimeMinutes);
            }
            final String _tmpTipsHtml;
            if (_cursor.isNull(_cursorIndexOfTipsHtml)) {
              _tmpTipsHtml = null;
            } else {
              _tmpTipsHtml = _cursor.getString(_cursorIndexOfTipsHtml);
            }
            final String _tmpNotesHtml;
            if (_cursor.isNull(_cursorIndexOfNotesHtml)) {
              _tmpNotesHtml = null;
            } else {
              _tmpNotesHtml = _cursor.getString(_cursorIndexOfNotesHtml);
            }
            final long _tmpCachedAt;
            _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
            _item = new Recipe(_tmpId,_tmpName,_tmpRestaurant,_tmpPreparationHtml,_tmpIngredientsHtml,_tmpImageUrl,_tmpRestaurantUrl,_tmpTimeMinutes,_tmpTipsHtml,_tmpNotesHtml,_tmpCachedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public LiveData<List<Recipe>> search(final String query) {
    final String _sql = "\n"
            + "        SELECT * FROM recipes\n"
            + "        WHERE name LIKE '%' || ? || '%'\n"
            + "           OR restaurant LIKE '%' || ? || '%'\n"
            + "           OR ingredientsHtml LIKE '%' || ? || '%'\n"
            + "        ORDER BY name COLLATE NOCASE ASC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
    _argIndex = 2;
    _statement.bindString(_argIndex, query);
    _argIndex = 3;
    _statement.bindString(_argIndex, query);
    return __db.getInvalidationTracker().createLiveData(new String[] {"recipes"}, false, new Callable<List<Recipe>>() {
      @Override
      @Nullable
      public List<Recipe> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRestaurant = CursorUtil.getColumnIndexOrThrow(_cursor, "restaurant");
          final int _cursorIndexOfPreparationHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "preparationHtml");
          final int _cursorIndexOfIngredientsHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "ingredientsHtml");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfRestaurantUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "restaurantUrl");
          final int _cursorIndexOfTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timeMinutes");
          final int _cursorIndexOfTipsHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "tipsHtml");
          final int _cursorIndexOfNotesHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "notesHtml");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cachedAt");
          final List<Recipe> _result = new ArrayList<Recipe>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Recipe _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpRestaurant;
            _tmpRestaurant = _cursor.getString(_cursorIndexOfRestaurant);
            final String _tmpPreparationHtml;
            _tmpPreparationHtml = _cursor.getString(_cursorIndexOfPreparationHtml);
            final String _tmpIngredientsHtml;
            _tmpIngredientsHtml = _cursor.getString(_cursorIndexOfIngredientsHtml);
            final String _tmpImageUrl;
            if (_cursor.isNull(_cursorIndexOfImageUrl)) {
              _tmpImageUrl = null;
            } else {
              _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            }
            final String _tmpRestaurantUrl;
            if (_cursor.isNull(_cursorIndexOfRestaurantUrl)) {
              _tmpRestaurantUrl = null;
            } else {
              _tmpRestaurantUrl = _cursor.getString(_cursorIndexOfRestaurantUrl);
            }
            final Integer _tmpTimeMinutes;
            if (_cursor.isNull(_cursorIndexOfTimeMinutes)) {
              _tmpTimeMinutes = null;
            } else {
              _tmpTimeMinutes = _cursor.getInt(_cursorIndexOfTimeMinutes);
            }
            final String _tmpTipsHtml;
            if (_cursor.isNull(_cursorIndexOfTipsHtml)) {
              _tmpTipsHtml = null;
            } else {
              _tmpTipsHtml = _cursor.getString(_cursorIndexOfTipsHtml);
            }
            final String _tmpNotesHtml;
            if (_cursor.isNull(_cursorIndexOfNotesHtml)) {
              _tmpNotesHtml = null;
            } else {
              _tmpNotesHtml = _cursor.getString(_cursorIndexOfNotesHtml);
            }
            final long _tmpCachedAt;
            _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
            _item = new Recipe(_tmpId,_tmpName,_tmpRestaurant,_tmpPreparationHtml,_tmpIngredientsHtml,_tmpImageUrl,_tmpRestaurantUrl,_tmpTimeMinutes,_tmpTipsHtml,_tmpNotesHtml,_tmpCachedAt);
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
  public LiveData<Recipe> getById(final int id) {
    final String _sql = "SELECT * FROM recipes WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    return __db.getInvalidationTracker().createLiveData(new String[] {"recipes"}, false, new Callable<Recipe>() {
      @Override
      @Nullable
      public Recipe call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRestaurant = CursorUtil.getColumnIndexOrThrow(_cursor, "restaurant");
          final int _cursorIndexOfPreparationHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "preparationHtml");
          final int _cursorIndexOfIngredientsHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "ingredientsHtml");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfRestaurantUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "restaurantUrl");
          final int _cursorIndexOfTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timeMinutes");
          final int _cursorIndexOfTipsHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "tipsHtml");
          final int _cursorIndexOfNotesHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "notesHtml");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cachedAt");
          final Recipe _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpRestaurant;
            _tmpRestaurant = _cursor.getString(_cursorIndexOfRestaurant);
            final String _tmpPreparationHtml;
            _tmpPreparationHtml = _cursor.getString(_cursorIndexOfPreparationHtml);
            final String _tmpIngredientsHtml;
            _tmpIngredientsHtml = _cursor.getString(_cursorIndexOfIngredientsHtml);
            final String _tmpImageUrl;
            if (_cursor.isNull(_cursorIndexOfImageUrl)) {
              _tmpImageUrl = null;
            } else {
              _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            }
            final String _tmpRestaurantUrl;
            if (_cursor.isNull(_cursorIndexOfRestaurantUrl)) {
              _tmpRestaurantUrl = null;
            } else {
              _tmpRestaurantUrl = _cursor.getString(_cursorIndexOfRestaurantUrl);
            }
            final Integer _tmpTimeMinutes;
            if (_cursor.isNull(_cursorIndexOfTimeMinutes)) {
              _tmpTimeMinutes = null;
            } else {
              _tmpTimeMinutes = _cursor.getInt(_cursorIndexOfTimeMinutes);
            }
            final String _tmpTipsHtml;
            if (_cursor.isNull(_cursorIndexOfTipsHtml)) {
              _tmpTipsHtml = null;
            } else {
              _tmpTipsHtml = _cursor.getString(_cursorIndexOfTipsHtml);
            }
            final String _tmpNotesHtml;
            if (_cursor.isNull(_cursorIndexOfNotesHtml)) {
              _tmpNotesHtml = null;
            } else {
              _tmpNotesHtml = _cursor.getString(_cursorIndexOfNotesHtml);
            }
            final long _tmpCachedAt;
            _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
            _result = new Recipe(_tmpId,_tmpName,_tmpRestaurant,_tmpPreparationHtml,_tmpIngredientsHtml,_tmpImageUrl,_tmpRestaurantUrl,_tmpTimeMinutes,_tmpTipsHtml,_tmpNotesHtml,_tmpCachedAt);
          } else {
            _result = null;
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
  public Object getByIdSync(final int id, final Continuation<? super Recipe> $completion) {
    final String _sql = "SELECT * FROM recipes WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Recipe>() {
      @Override
      @Nullable
      public Recipe call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRestaurant = CursorUtil.getColumnIndexOrThrow(_cursor, "restaurant");
          final int _cursorIndexOfPreparationHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "preparationHtml");
          final int _cursorIndexOfIngredientsHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "ingredientsHtml");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfRestaurantUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "restaurantUrl");
          final int _cursorIndexOfTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timeMinutes");
          final int _cursorIndexOfTipsHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "tipsHtml");
          final int _cursorIndexOfNotesHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "notesHtml");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cachedAt");
          final Recipe _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpRestaurant;
            _tmpRestaurant = _cursor.getString(_cursorIndexOfRestaurant);
            final String _tmpPreparationHtml;
            _tmpPreparationHtml = _cursor.getString(_cursorIndexOfPreparationHtml);
            final String _tmpIngredientsHtml;
            _tmpIngredientsHtml = _cursor.getString(_cursorIndexOfIngredientsHtml);
            final String _tmpImageUrl;
            if (_cursor.isNull(_cursorIndexOfImageUrl)) {
              _tmpImageUrl = null;
            } else {
              _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            }
            final String _tmpRestaurantUrl;
            if (_cursor.isNull(_cursorIndexOfRestaurantUrl)) {
              _tmpRestaurantUrl = null;
            } else {
              _tmpRestaurantUrl = _cursor.getString(_cursorIndexOfRestaurantUrl);
            }
            final Integer _tmpTimeMinutes;
            if (_cursor.isNull(_cursorIndexOfTimeMinutes)) {
              _tmpTimeMinutes = null;
            } else {
              _tmpTimeMinutes = _cursor.getInt(_cursorIndexOfTimeMinutes);
            }
            final String _tmpTipsHtml;
            if (_cursor.isNull(_cursorIndexOfTipsHtml)) {
              _tmpTipsHtml = null;
            } else {
              _tmpTipsHtml = _cursor.getString(_cursorIndexOfTipsHtml);
            }
            final String _tmpNotesHtml;
            if (_cursor.isNull(_cursorIndexOfNotesHtml)) {
              _tmpNotesHtml = null;
            } else {
              _tmpNotesHtml = _cursor.getString(_cursorIndexOfNotesHtml);
            }
            final long _tmpCachedAt;
            _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
            _result = new Recipe(_tmpId,_tmpName,_tmpRestaurant,_tmpPreparationHtml,_tmpIngredientsHtml,_tmpImageUrl,_tmpRestaurantUrl,_tmpTimeMinutes,_tmpTipsHtml,_tmpNotesHtml,_tmpCachedAt);
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

  @Override
  public LiveData<List<Recipe>> getAllByRestaurant() {
    final String _sql = "SELECT * FROM recipes ORDER BY restaurant COLLATE NOCASE ASC, name COLLATE NOCASE ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"recipes"}, false, new Callable<List<Recipe>>() {
      @Override
      @Nullable
      public List<Recipe> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRestaurant = CursorUtil.getColumnIndexOrThrow(_cursor, "restaurant");
          final int _cursorIndexOfPreparationHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "preparationHtml");
          final int _cursorIndexOfIngredientsHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "ingredientsHtml");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfRestaurantUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "restaurantUrl");
          final int _cursorIndexOfTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timeMinutes");
          final int _cursorIndexOfTipsHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "tipsHtml");
          final int _cursorIndexOfNotesHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "notesHtml");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cachedAt");
          final List<Recipe> _result = new ArrayList<Recipe>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Recipe _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpRestaurant;
            _tmpRestaurant = _cursor.getString(_cursorIndexOfRestaurant);
            final String _tmpPreparationHtml;
            _tmpPreparationHtml = _cursor.getString(_cursorIndexOfPreparationHtml);
            final String _tmpIngredientsHtml;
            _tmpIngredientsHtml = _cursor.getString(_cursorIndexOfIngredientsHtml);
            final String _tmpImageUrl;
            if (_cursor.isNull(_cursorIndexOfImageUrl)) {
              _tmpImageUrl = null;
            } else {
              _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            }
            final String _tmpRestaurantUrl;
            if (_cursor.isNull(_cursorIndexOfRestaurantUrl)) {
              _tmpRestaurantUrl = null;
            } else {
              _tmpRestaurantUrl = _cursor.getString(_cursorIndexOfRestaurantUrl);
            }
            final Integer _tmpTimeMinutes;
            if (_cursor.isNull(_cursorIndexOfTimeMinutes)) {
              _tmpTimeMinutes = null;
            } else {
              _tmpTimeMinutes = _cursor.getInt(_cursorIndexOfTimeMinutes);
            }
            final String _tmpTipsHtml;
            if (_cursor.isNull(_cursorIndexOfTipsHtml)) {
              _tmpTipsHtml = null;
            } else {
              _tmpTipsHtml = _cursor.getString(_cursorIndexOfTipsHtml);
            }
            final String _tmpNotesHtml;
            if (_cursor.isNull(_cursorIndexOfNotesHtml)) {
              _tmpNotesHtml = null;
            } else {
              _tmpNotesHtml = _cursor.getString(_cursorIndexOfNotesHtml);
            }
            final long _tmpCachedAt;
            _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
            _item = new Recipe(_tmpId,_tmpName,_tmpRestaurant,_tmpPreparationHtml,_tmpIngredientsHtml,_tmpImageUrl,_tmpRestaurantUrl,_tmpTimeMinutes,_tmpTipsHtml,_tmpNotesHtml,_tmpCachedAt);
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
  public LiveData<List<Recipe>> getByMaxTime(final int maxMinutes) {
    final String _sql = "SELECT * FROM recipes WHERE timeMinutes IS NOT NULL AND timeMinutes <= ? ORDER BY name COLLATE NOCASE ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, maxMinutes);
    return __db.getInvalidationTracker().createLiveData(new String[] {"recipes"}, false, new Callable<List<Recipe>>() {
      @Override
      @Nullable
      public List<Recipe> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRestaurant = CursorUtil.getColumnIndexOrThrow(_cursor, "restaurant");
          final int _cursorIndexOfPreparationHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "preparationHtml");
          final int _cursorIndexOfIngredientsHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "ingredientsHtml");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfRestaurantUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "restaurantUrl");
          final int _cursorIndexOfTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timeMinutes");
          final int _cursorIndexOfTipsHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "tipsHtml");
          final int _cursorIndexOfNotesHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "notesHtml");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cachedAt");
          final List<Recipe> _result = new ArrayList<Recipe>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Recipe _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpRestaurant;
            _tmpRestaurant = _cursor.getString(_cursorIndexOfRestaurant);
            final String _tmpPreparationHtml;
            _tmpPreparationHtml = _cursor.getString(_cursorIndexOfPreparationHtml);
            final String _tmpIngredientsHtml;
            _tmpIngredientsHtml = _cursor.getString(_cursorIndexOfIngredientsHtml);
            final String _tmpImageUrl;
            if (_cursor.isNull(_cursorIndexOfImageUrl)) {
              _tmpImageUrl = null;
            } else {
              _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            }
            final String _tmpRestaurantUrl;
            if (_cursor.isNull(_cursorIndexOfRestaurantUrl)) {
              _tmpRestaurantUrl = null;
            } else {
              _tmpRestaurantUrl = _cursor.getString(_cursorIndexOfRestaurantUrl);
            }
            final Integer _tmpTimeMinutes;
            if (_cursor.isNull(_cursorIndexOfTimeMinutes)) {
              _tmpTimeMinutes = null;
            } else {
              _tmpTimeMinutes = _cursor.getInt(_cursorIndexOfTimeMinutes);
            }
            final String _tmpTipsHtml;
            if (_cursor.isNull(_cursorIndexOfTipsHtml)) {
              _tmpTipsHtml = null;
            } else {
              _tmpTipsHtml = _cursor.getString(_cursorIndexOfTipsHtml);
            }
            final String _tmpNotesHtml;
            if (_cursor.isNull(_cursorIndexOfNotesHtml)) {
              _tmpNotesHtml = null;
            } else {
              _tmpNotesHtml = _cursor.getString(_cursorIndexOfNotesHtml);
            }
            final long _tmpCachedAt;
            _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
            _item = new Recipe(_tmpId,_tmpName,_tmpRestaurant,_tmpPreparationHtml,_tmpIngredientsHtml,_tmpImageUrl,_tmpRestaurantUrl,_tmpTimeMinutes,_tmpTipsHtml,_tmpNotesHtml,_tmpCachedAt);
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
  public LiveData<List<Recipe>> getWithTime() {
    final String _sql = "SELECT * FROM recipes WHERE timeMinutes IS NOT NULL ORDER BY name COLLATE NOCASE ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"recipes"}, false, new Callable<List<Recipe>>() {
      @Override
      @Nullable
      public List<Recipe> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRestaurant = CursorUtil.getColumnIndexOrThrow(_cursor, "restaurant");
          final int _cursorIndexOfPreparationHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "preparationHtml");
          final int _cursorIndexOfIngredientsHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "ingredientsHtml");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfRestaurantUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "restaurantUrl");
          final int _cursorIndexOfTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timeMinutes");
          final int _cursorIndexOfTipsHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "tipsHtml");
          final int _cursorIndexOfNotesHtml = CursorUtil.getColumnIndexOrThrow(_cursor, "notesHtml");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cachedAt");
          final List<Recipe> _result = new ArrayList<Recipe>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Recipe _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpRestaurant;
            _tmpRestaurant = _cursor.getString(_cursorIndexOfRestaurant);
            final String _tmpPreparationHtml;
            _tmpPreparationHtml = _cursor.getString(_cursorIndexOfPreparationHtml);
            final String _tmpIngredientsHtml;
            _tmpIngredientsHtml = _cursor.getString(_cursorIndexOfIngredientsHtml);
            final String _tmpImageUrl;
            if (_cursor.isNull(_cursorIndexOfImageUrl)) {
              _tmpImageUrl = null;
            } else {
              _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            }
            final String _tmpRestaurantUrl;
            if (_cursor.isNull(_cursorIndexOfRestaurantUrl)) {
              _tmpRestaurantUrl = null;
            } else {
              _tmpRestaurantUrl = _cursor.getString(_cursorIndexOfRestaurantUrl);
            }
            final Integer _tmpTimeMinutes;
            if (_cursor.isNull(_cursorIndexOfTimeMinutes)) {
              _tmpTimeMinutes = null;
            } else {
              _tmpTimeMinutes = _cursor.getInt(_cursorIndexOfTimeMinutes);
            }
            final String _tmpTipsHtml;
            if (_cursor.isNull(_cursorIndexOfTipsHtml)) {
              _tmpTipsHtml = null;
            } else {
              _tmpTipsHtml = _cursor.getString(_cursorIndexOfTipsHtml);
            }
            final String _tmpNotesHtml;
            if (_cursor.isNull(_cursorIndexOfNotesHtml)) {
              _tmpNotesHtml = null;
            } else {
              _tmpNotesHtml = _cursor.getString(_cursorIndexOfNotesHtml);
            }
            final long _tmpCachedAt;
            _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
            _item = new Recipe(_tmpId,_tmpName,_tmpRestaurant,_tmpPreparationHtml,_tmpIngredientsHtml,_tmpImageUrl,_tmpRestaurantUrl,_tmpTimeMinutes,_tmpTipsHtml,_tmpNotesHtml,_tmpCachedAt);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
