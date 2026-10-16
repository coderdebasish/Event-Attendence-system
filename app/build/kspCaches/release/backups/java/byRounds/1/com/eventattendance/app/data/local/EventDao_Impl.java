package com.eventattendance.app.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.eventattendance.app.data.model.Event;
import java.lang.Class;
import java.lang.Exception;
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
public final class EventDao_Impl implements EventDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Event> __insertionAdapterOfEvent;

  private final SharedSQLiteStatement __preparedStmtOfDeleteEvent;

  public EventDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfEvent = new EntityInsertionAdapter<Event>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `events` (`id`,`title`,`description`,`eventDate`,`status`,`allowAttendance`,`createdBy`,`createdAt`,`registrationCount`,`attendanceCount`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Event entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        if (entity.getDescription() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getDescription());
        }
        statement.bindString(4, entity.getEventDate());
        statement.bindString(5, entity.getStatus());
        final int _tmp = entity.getAllowAttendance() ? 1 : 0;
        statement.bindLong(6, _tmp);
        if (entity.getCreatedBy() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getCreatedBy());
        }
        if (entity.getCreatedAt() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getCreatedAt());
        }
        statement.bindLong(9, entity.getRegistrationCount());
        statement.bindLong(10, entity.getAttendanceCount());
      }
    };
    this.__preparedStmtOfDeleteEvent = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM events WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertEvent(final Event event, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfEvent.insert(event);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAllEvents(final List<Event> events,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfEvent.insert(events);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteEvent(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteEvent.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfDeleteEvent.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Event>> getAllEvents() {
    final String _sql = "SELECT * FROM events ORDER BY eventDate DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"events"}, new Callable<List<Event>>() {
      @Override
      @NonNull
      public List<Event> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfEventDate = CursorUtil.getColumnIndexOrThrow(_cursor, "eventDate");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfAllowAttendance = CursorUtil.getColumnIndexOrThrow(_cursor, "allowAttendance");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfRegistrationCount = CursorUtil.getColumnIndexOrThrow(_cursor, "registrationCount");
          final int _cursorIndexOfAttendanceCount = CursorUtil.getColumnIndexOrThrow(_cursor, "attendanceCount");
          final List<Event> _result = new ArrayList<Event>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Event _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpEventDate;
            _tmpEventDate = _cursor.getString(_cursorIndexOfEventDate);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final boolean _tmpAllowAttendance;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAllowAttendance);
            _tmpAllowAttendance = _tmp != 0;
            final String _tmpCreatedBy;
            if (_cursor.isNull(_cursorIndexOfCreatedBy)) {
              _tmpCreatedBy = null;
            } else {
              _tmpCreatedBy = _cursor.getString(_cursorIndexOfCreatedBy);
            }
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            final int _tmpRegistrationCount;
            _tmpRegistrationCount = _cursor.getInt(_cursorIndexOfRegistrationCount);
            final int _tmpAttendanceCount;
            _tmpAttendanceCount = _cursor.getInt(_cursorIndexOfAttendanceCount);
            _item = new Event(_tmpId,_tmpTitle,_tmpDescription,_tmpEventDate,_tmpStatus,_tmpAllowAttendance,_tmpCreatedBy,_tmpCreatedAt,_tmpRegistrationCount,_tmpAttendanceCount);
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
  public Flow<Event> getEventById(final String id) {
    final String _sql = "SELECT * FROM events WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"events"}, new Callable<Event>() {
      @Override
      @Nullable
      public Event call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfEventDate = CursorUtil.getColumnIndexOrThrow(_cursor, "eventDate");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfAllowAttendance = CursorUtil.getColumnIndexOrThrow(_cursor, "allowAttendance");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfRegistrationCount = CursorUtil.getColumnIndexOrThrow(_cursor, "registrationCount");
          final int _cursorIndexOfAttendanceCount = CursorUtil.getColumnIndexOrThrow(_cursor, "attendanceCount");
          final Event _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpEventDate;
            _tmpEventDate = _cursor.getString(_cursorIndexOfEventDate);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final boolean _tmpAllowAttendance;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAllowAttendance);
            _tmpAllowAttendance = _tmp != 0;
            final String _tmpCreatedBy;
            if (_cursor.isNull(_cursorIndexOfCreatedBy)) {
              _tmpCreatedBy = null;
            } else {
              _tmpCreatedBy = _cursor.getString(_cursorIndexOfCreatedBy);
            }
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            final int _tmpRegistrationCount;
            _tmpRegistrationCount = _cursor.getInt(_cursorIndexOfRegistrationCount);
            final int _tmpAttendanceCount;
            _tmpAttendanceCount = _cursor.getInt(_cursorIndexOfAttendanceCount);
            _result = new Event(_tmpId,_tmpTitle,_tmpDescription,_tmpEventDate,_tmpStatus,_tmpAllowAttendance,_tmpCreatedBy,_tmpCreatedAt,_tmpRegistrationCount,_tmpAttendanceCount);
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
  public Object getEventByIdSync(final String id, final Continuation<? super Event> $completion) {
    final String _sql = "SELECT * FROM events WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Event>() {
      @Override
      @Nullable
      public Event call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfEventDate = CursorUtil.getColumnIndexOrThrow(_cursor, "eventDate");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfAllowAttendance = CursorUtil.getColumnIndexOrThrow(_cursor, "allowAttendance");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfRegistrationCount = CursorUtil.getColumnIndexOrThrow(_cursor, "registrationCount");
          final int _cursorIndexOfAttendanceCount = CursorUtil.getColumnIndexOrThrow(_cursor, "attendanceCount");
          final Event _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpEventDate;
            _tmpEventDate = _cursor.getString(_cursorIndexOfEventDate);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final boolean _tmpAllowAttendance;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAllowAttendance);
            _tmpAllowAttendance = _tmp != 0;
            final String _tmpCreatedBy;
            if (_cursor.isNull(_cursorIndexOfCreatedBy)) {
              _tmpCreatedBy = null;
            } else {
              _tmpCreatedBy = _cursor.getString(_cursorIndexOfCreatedBy);
            }
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            final int _tmpRegistrationCount;
            _tmpRegistrationCount = _cursor.getInt(_cursorIndexOfRegistrationCount);
            final int _tmpAttendanceCount;
            _tmpAttendanceCount = _cursor.getInt(_cursorIndexOfAttendanceCount);
            _result = new Event(_tmpId,_tmpTitle,_tmpDescription,_tmpEventDate,_tmpStatus,_tmpAllowAttendance,_tmpCreatedBy,_tmpCreatedAt,_tmpRegistrationCount,_tmpAttendanceCount);
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
