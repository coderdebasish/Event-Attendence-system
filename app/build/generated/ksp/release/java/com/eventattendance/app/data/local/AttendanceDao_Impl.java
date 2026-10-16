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
import com.eventattendance.app.data.model.AttendanceRecord;
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
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AttendanceDao_Impl implements AttendanceDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AttendanceRecord> __insertionAdapterOfAttendanceRecord;

  private final SharedSQLiteStatement __preparedStmtOfUpdateStatus;

  private final SharedSQLiteStatement __preparedStmtOfClearAttendanceForEvent;

  public AttendanceDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAttendanceRecord = new EntityInsertionAdapter<AttendanceRecord>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `attendance_records` (`id`,`eventId`,`eventStudentId`,`enrollmentNo`,`markedBy`,`operatorName`,`deviceId`,`status`,`markedAt`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AttendanceRecord entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getEventId());
        statement.bindString(3, entity.getEventStudentId());
        statement.bindString(4, entity.getEnrollmentNo());
        if (entity.getMarkedBy() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getMarkedBy());
        }
        if (entity.getOperatorName() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getOperatorName());
        }
        if (entity.getDeviceId() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getDeviceId());
        }
        statement.bindString(8, entity.getStatus());
        statement.bindString(9, entity.getMarkedAt());
        if (entity.getCreatedAt() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getCreatedAt());
        }
      }
    };
    this.__preparedStmtOfUpdateStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE attendance_records SET status = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearAttendanceForEvent = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM attendance_records WHERE eventId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertAttendance(final AttendanceRecord record,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfAttendanceRecord.insert(record);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAllAttendance(final List<AttendanceRecord> records,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfAttendanceRecord.insert(records);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateStatus(final String id, final String status,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateStatus.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, status);
        _argIndex = 2;
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
          __preparedStmtOfUpdateStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAttendanceForEvent(final String eventId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAttendanceForEvent.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, eventId);
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
          __preparedStmtOfClearAttendanceForEvent.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getAttendance(final String eventId, final String enrollmentNo,
      final Continuation<? super AttendanceRecord> $completion) {
    final String _sql = "SELECT * FROM attendance_records WHERE eventId = ? AND enrollmentNo = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, eventId);
    _argIndex = 2;
    _statement.bindString(_argIndex, enrollmentNo);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<AttendanceRecord>() {
      @Override
      @Nullable
      public AttendanceRecord call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "eventId");
          final int _cursorIndexOfEventStudentId = CursorUtil.getColumnIndexOrThrow(_cursor, "eventStudentId");
          final int _cursorIndexOfEnrollmentNo = CursorUtil.getColumnIndexOrThrow(_cursor, "enrollmentNo");
          final int _cursorIndexOfMarkedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "markedBy");
          final int _cursorIndexOfOperatorName = CursorUtil.getColumnIndexOrThrow(_cursor, "operatorName");
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceId");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfMarkedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "markedAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final AttendanceRecord _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpEventId;
            _tmpEventId = _cursor.getString(_cursorIndexOfEventId);
            final String _tmpEventStudentId;
            _tmpEventStudentId = _cursor.getString(_cursorIndexOfEventStudentId);
            final String _tmpEnrollmentNo;
            _tmpEnrollmentNo = _cursor.getString(_cursorIndexOfEnrollmentNo);
            final String _tmpMarkedBy;
            if (_cursor.isNull(_cursorIndexOfMarkedBy)) {
              _tmpMarkedBy = null;
            } else {
              _tmpMarkedBy = _cursor.getString(_cursorIndexOfMarkedBy);
            }
            final String _tmpOperatorName;
            if (_cursor.isNull(_cursorIndexOfOperatorName)) {
              _tmpOperatorName = null;
            } else {
              _tmpOperatorName = _cursor.getString(_cursorIndexOfOperatorName);
            }
            final String _tmpDeviceId;
            if (_cursor.isNull(_cursorIndexOfDeviceId)) {
              _tmpDeviceId = null;
            } else {
              _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpMarkedAt;
            _tmpMarkedAt = _cursor.getString(_cursorIndexOfMarkedAt);
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            _result = new AttendanceRecord(_tmpId,_tmpEventId,_tmpEventStudentId,_tmpEnrollmentNo,_tmpMarkedBy,_tmpOperatorName,_tmpDeviceId,_tmpStatus,_tmpMarkedAt,_tmpCreatedAt);
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
  public Flow<AttendanceRecord> observeAttendance(final String eventId, final String enrollmentNo) {
    final String _sql = "SELECT * FROM attendance_records WHERE eventId = ? AND enrollmentNo = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, eventId);
    _argIndex = 2;
    _statement.bindString(_argIndex, enrollmentNo);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"attendance_records"}, new Callable<AttendanceRecord>() {
      @Override
      @Nullable
      public AttendanceRecord call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "eventId");
          final int _cursorIndexOfEventStudentId = CursorUtil.getColumnIndexOrThrow(_cursor, "eventStudentId");
          final int _cursorIndexOfEnrollmentNo = CursorUtil.getColumnIndexOrThrow(_cursor, "enrollmentNo");
          final int _cursorIndexOfMarkedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "markedBy");
          final int _cursorIndexOfOperatorName = CursorUtil.getColumnIndexOrThrow(_cursor, "operatorName");
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceId");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfMarkedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "markedAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final AttendanceRecord _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpEventId;
            _tmpEventId = _cursor.getString(_cursorIndexOfEventId);
            final String _tmpEventStudentId;
            _tmpEventStudentId = _cursor.getString(_cursorIndexOfEventStudentId);
            final String _tmpEnrollmentNo;
            _tmpEnrollmentNo = _cursor.getString(_cursorIndexOfEnrollmentNo);
            final String _tmpMarkedBy;
            if (_cursor.isNull(_cursorIndexOfMarkedBy)) {
              _tmpMarkedBy = null;
            } else {
              _tmpMarkedBy = _cursor.getString(_cursorIndexOfMarkedBy);
            }
            final String _tmpOperatorName;
            if (_cursor.isNull(_cursorIndexOfOperatorName)) {
              _tmpOperatorName = null;
            } else {
              _tmpOperatorName = _cursor.getString(_cursorIndexOfOperatorName);
            }
            final String _tmpDeviceId;
            if (_cursor.isNull(_cursorIndexOfDeviceId)) {
              _tmpDeviceId = null;
            } else {
              _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpMarkedAt;
            _tmpMarkedAt = _cursor.getString(_cursorIndexOfMarkedAt);
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            _result = new AttendanceRecord(_tmpId,_tmpEventId,_tmpEventStudentId,_tmpEnrollmentNo,_tmpMarkedBy,_tmpOperatorName,_tmpDeviceId,_tmpStatus,_tmpMarkedAt,_tmpCreatedAt);
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
  public Flow<List<AttendanceRecord>> getAllAttendanceForEvent(final String eventId) {
    final String _sql = "SELECT * FROM attendance_records WHERE eventId = ? ORDER BY markedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, eventId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"attendance_records"}, new Callable<List<AttendanceRecord>>() {
      @Override
      @NonNull
      public List<AttendanceRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "eventId");
          final int _cursorIndexOfEventStudentId = CursorUtil.getColumnIndexOrThrow(_cursor, "eventStudentId");
          final int _cursorIndexOfEnrollmentNo = CursorUtil.getColumnIndexOrThrow(_cursor, "enrollmentNo");
          final int _cursorIndexOfMarkedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "markedBy");
          final int _cursorIndexOfOperatorName = CursorUtil.getColumnIndexOrThrow(_cursor, "operatorName");
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceId");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfMarkedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "markedAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<AttendanceRecord> _result = new ArrayList<AttendanceRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AttendanceRecord _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpEventId;
            _tmpEventId = _cursor.getString(_cursorIndexOfEventId);
            final String _tmpEventStudentId;
            _tmpEventStudentId = _cursor.getString(_cursorIndexOfEventStudentId);
            final String _tmpEnrollmentNo;
            _tmpEnrollmentNo = _cursor.getString(_cursorIndexOfEnrollmentNo);
            final String _tmpMarkedBy;
            if (_cursor.isNull(_cursorIndexOfMarkedBy)) {
              _tmpMarkedBy = null;
            } else {
              _tmpMarkedBy = _cursor.getString(_cursorIndexOfMarkedBy);
            }
            final String _tmpOperatorName;
            if (_cursor.isNull(_cursorIndexOfOperatorName)) {
              _tmpOperatorName = null;
            } else {
              _tmpOperatorName = _cursor.getString(_cursorIndexOfOperatorName);
            }
            final String _tmpDeviceId;
            if (_cursor.isNull(_cursorIndexOfDeviceId)) {
              _tmpDeviceId = null;
            } else {
              _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpMarkedAt;
            _tmpMarkedAt = _cursor.getString(_cursorIndexOfMarkedAt);
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            _item = new AttendanceRecord(_tmpId,_tmpEventId,_tmpEventStudentId,_tmpEnrollmentNo,_tmpMarkedBy,_tmpOperatorName,_tmpDeviceId,_tmpStatus,_tmpMarkedAt,_tmpCreatedAt);
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
  public Object getAttendanceListForEvent(final String eventId,
      final Continuation<? super List<AttendanceRecord>> $completion) {
    final String _sql = "SELECT * FROM attendance_records WHERE eventId = ? ORDER BY markedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, eventId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AttendanceRecord>>() {
      @Override
      @NonNull
      public List<AttendanceRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "eventId");
          final int _cursorIndexOfEventStudentId = CursorUtil.getColumnIndexOrThrow(_cursor, "eventStudentId");
          final int _cursorIndexOfEnrollmentNo = CursorUtil.getColumnIndexOrThrow(_cursor, "enrollmentNo");
          final int _cursorIndexOfMarkedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "markedBy");
          final int _cursorIndexOfOperatorName = CursorUtil.getColumnIndexOrThrow(_cursor, "operatorName");
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceId");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfMarkedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "markedAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<AttendanceRecord> _result = new ArrayList<AttendanceRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AttendanceRecord _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpEventId;
            _tmpEventId = _cursor.getString(_cursorIndexOfEventId);
            final String _tmpEventStudentId;
            _tmpEventStudentId = _cursor.getString(_cursorIndexOfEventStudentId);
            final String _tmpEnrollmentNo;
            _tmpEnrollmentNo = _cursor.getString(_cursorIndexOfEnrollmentNo);
            final String _tmpMarkedBy;
            if (_cursor.isNull(_cursorIndexOfMarkedBy)) {
              _tmpMarkedBy = null;
            } else {
              _tmpMarkedBy = _cursor.getString(_cursorIndexOfMarkedBy);
            }
            final String _tmpOperatorName;
            if (_cursor.isNull(_cursorIndexOfOperatorName)) {
              _tmpOperatorName = null;
            } else {
              _tmpOperatorName = _cursor.getString(_cursorIndexOfOperatorName);
            }
            final String _tmpDeviceId;
            if (_cursor.isNull(_cursorIndexOfDeviceId)) {
              _tmpDeviceId = null;
            } else {
              _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpMarkedAt;
            _tmpMarkedAt = _cursor.getString(_cursorIndexOfMarkedAt);
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            _item = new AttendanceRecord(_tmpId,_tmpEventId,_tmpEventStudentId,_tmpEnrollmentNo,_tmpMarkedBy,_tmpOperatorName,_tmpDeviceId,_tmpStatus,_tmpMarkedAt,_tmpCreatedAt);
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
  public Flow<Integer> getAttendanceCountForEvent(final String eventId) {
    final String _sql = "SELECT COUNT(*) FROM attendance_records WHERE eventId = ? AND status = 'present'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, eventId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"attendance_records"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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
