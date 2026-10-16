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
import com.eventattendance.app.data.model.EventStudent;
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
public final class StudentDao_Impl implements StudentDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<EventStudent> __insertionAdapterOfEventStudent;

  private final SharedSQLiteStatement __preparedStmtOfClearRosterForEvent;

  public StudentDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfEventStudent = new EntityInsertionAdapter<EventStudent>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `event_students` (`id`,`eventId`,`studentId`,`name`,`enrollmentNo`,`section`,`rollNo`,`email`,`mobile`,`status`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EventStudent entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getEventId());
        if (entity.getStudentId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getStudentId());
        }
        statement.bindString(4, entity.getName());
        statement.bindString(5, entity.getEnrollmentNo());
        if (entity.getSection() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getSection());
        }
        if (entity.getRollNo() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getRollNo());
        }
        if (entity.getEmail() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getEmail());
        }
        if (entity.getMobile() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getMobile());
        }
        statement.bindString(10, entity.getStatus());
        if (entity.getCreatedAt() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getCreatedAt());
        }
      }
    };
    this.__preparedStmtOfClearRosterForEvent = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM event_students WHERE eventId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertStudents(final List<EventStudent> students,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfEventStudent.insert(students);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertStudent(final EventStudent student,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfEventStudent.insert(student);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearRosterForEvent(final String eventId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearRosterForEvent.acquire();
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
          __preparedStmtOfClearRosterForEvent.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getStudentByEnrollment(final String eventId, final String enrollmentNo,
      final Continuation<? super EventStudent> $completion) {
    final String _sql = "SELECT * FROM event_students WHERE eventId = ? AND enrollmentNo = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, eventId);
    _argIndex = 2;
    _statement.bindString(_argIndex, enrollmentNo);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<EventStudent>() {
      @Override
      @Nullable
      public EventStudent call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "eventId");
          final int _cursorIndexOfStudentId = CursorUtil.getColumnIndexOrThrow(_cursor, "studentId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfEnrollmentNo = CursorUtil.getColumnIndexOrThrow(_cursor, "enrollmentNo");
          final int _cursorIndexOfSection = CursorUtil.getColumnIndexOrThrow(_cursor, "section");
          final int _cursorIndexOfRollNo = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNo");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfMobile = CursorUtil.getColumnIndexOrThrow(_cursor, "mobile");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final EventStudent _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpEventId;
            _tmpEventId = _cursor.getString(_cursorIndexOfEventId);
            final String _tmpStudentId;
            if (_cursor.isNull(_cursorIndexOfStudentId)) {
              _tmpStudentId = null;
            } else {
              _tmpStudentId = _cursor.getString(_cursorIndexOfStudentId);
            }
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpEnrollmentNo;
            _tmpEnrollmentNo = _cursor.getString(_cursorIndexOfEnrollmentNo);
            final String _tmpSection;
            if (_cursor.isNull(_cursorIndexOfSection)) {
              _tmpSection = null;
            } else {
              _tmpSection = _cursor.getString(_cursorIndexOfSection);
            }
            final String _tmpRollNo;
            if (_cursor.isNull(_cursorIndexOfRollNo)) {
              _tmpRollNo = null;
            } else {
              _tmpRollNo = _cursor.getString(_cursorIndexOfRollNo);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final String _tmpMobile;
            if (_cursor.isNull(_cursorIndexOfMobile)) {
              _tmpMobile = null;
            } else {
              _tmpMobile = _cursor.getString(_cursorIndexOfMobile);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            _result = new EventStudent(_tmpId,_tmpEventId,_tmpStudentId,_tmpName,_tmpEnrollmentNo,_tmpSection,_tmpRollNo,_tmpEmail,_tmpMobile,_tmpStatus,_tmpCreatedAt);
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
  public Flow<EventStudent> observeStudentByEnrollment(final String eventId,
      final String enrollmentNo) {
    final String _sql = "SELECT * FROM event_students WHERE eventId = ? AND enrollmentNo = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, eventId);
    _argIndex = 2;
    _statement.bindString(_argIndex, enrollmentNo);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"event_students"}, new Callable<EventStudent>() {
      @Override
      @Nullable
      public EventStudent call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "eventId");
          final int _cursorIndexOfStudentId = CursorUtil.getColumnIndexOrThrow(_cursor, "studentId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfEnrollmentNo = CursorUtil.getColumnIndexOrThrow(_cursor, "enrollmentNo");
          final int _cursorIndexOfSection = CursorUtil.getColumnIndexOrThrow(_cursor, "section");
          final int _cursorIndexOfRollNo = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNo");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfMobile = CursorUtil.getColumnIndexOrThrow(_cursor, "mobile");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final EventStudent _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpEventId;
            _tmpEventId = _cursor.getString(_cursorIndexOfEventId);
            final String _tmpStudentId;
            if (_cursor.isNull(_cursorIndexOfStudentId)) {
              _tmpStudentId = null;
            } else {
              _tmpStudentId = _cursor.getString(_cursorIndexOfStudentId);
            }
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpEnrollmentNo;
            _tmpEnrollmentNo = _cursor.getString(_cursorIndexOfEnrollmentNo);
            final String _tmpSection;
            if (_cursor.isNull(_cursorIndexOfSection)) {
              _tmpSection = null;
            } else {
              _tmpSection = _cursor.getString(_cursorIndexOfSection);
            }
            final String _tmpRollNo;
            if (_cursor.isNull(_cursorIndexOfRollNo)) {
              _tmpRollNo = null;
            } else {
              _tmpRollNo = _cursor.getString(_cursorIndexOfRollNo);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final String _tmpMobile;
            if (_cursor.isNull(_cursorIndexOfMobile)) {
              _tmpMobile = null;
            } else {
              _tmpMobile = _cursor.getString(_cursorIndexOfMobile);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            _result = new EventStudent(_tmpId,_tmpEventId,_tmpStudentId,_tmpName,_tmpEnrollmentNo,_tmpSection,_tmpRollNo,_tmpEmail,_tmpMobile,_tmpStatus,_tmpCreatedAt);
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
  public Flow<List<EventStudent>> searchByEnrollmentPrefix(final String eventId,
      final String prefix) {
    final String _sql = "\n"
            + "        SELECT * FROM event_students \n"
            + "        WHERE eventId = ? AND enrollmentNo LIKE ? || '%' \n"
            + "        ORDER BY enrollmentNo ASC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, eventId);
    _argIndex = 2;
    _statement.bindString(_argIndex, prefix);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"event_students"}, new Callable<List<EventStudent>>() {
      @Override
      @NonNull
      public List<EventStudent> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "eventId");
          final int _cursorIndexOfStudentId = CursorUtil.getColumnIndexOrThrow(_cursor, "studentId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfEnrollmentNo = CursorUtil.getColumnIndexOrThrow(_cursor, "enrollmentNo");
          final int _cursorIndexOfSection = CursorUtil.getColumnIndexOrThrow(_cursor, "section");
          final int _cursorIndexOfRollNo = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNo");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfMobile = CursorUtil.getColumnIndexOrThrow(_cursor, "mobile");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<EventStudent> _result = new ArrayList<EventStudent>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EventStudent _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpEventId;
            _tmpEventId = _cursor.getString(_cursorIndexOfEventId);
            final String _tmpStudentId;
            if (_cursor.isNull(_cursorIndexOfStudentId)) {
              _tmpStudentId = null;
            } else {
              _tmpStudentId = _cursor.getString(_cursorIndexOfStudentId);
            }
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpEnrollmentNo;
            _tmpEnrollmentNo = _cursor.getString(_cursorIndexOfEnrollmentNo);
            final String _tmpSection;
            if (_cursor.isNull(_cursorIndexOfSection)) {
              _tmpSection = null;
            } else {
              _tmpSection = _cursor.getString(_cursorIndexOfSection);
            }
            final String _tmpRollNo;
            if (_cursor.isNull(_cursorIndexOfRollNo)) {
              _tmpRollNo = null;
            } else {
              _tmpRollNo = _cursor.getString(_cursorIndexOfRollNo);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final String _tmpMobile;
            if (_cursor.isNull(_cursorIndexOfMobile)) {
              _tmpMobile = null;
            } else {
              _tmpMobile = _cursor.getString(_cursorIndexOfMobile);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            _item = new EventStudent(_tmpId,_tmpEventId,_tmpStudentId,_tmpName,_tmpEnrollmentNo,_tmpSection,_tmpRollNo,_tmpEmail,_tmpMobile,_tmpStatus,_tmpCreatedAt);
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
  public Flow<List<EventStudent>> searchByEnrollmentSubstring(final String eventId,
      final String query) {
    final String _sql = "\n"
            + "        SELECT * FROM event_students \n"
            + "        WHERE eventId = ? AND enrollmentNo LIKE '%' || ? || '%' \n"
            + "        ORDER BY enrollmentNo ASC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, eventId);
    _argIndex = 2;
    _statement.bindString(_argIndex, query);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"event_students"}, new Callable<List<EventStudent>>() {
      @Override
      @NonNull
      public List<EventStudent> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "eventId");
          final int _cursorIndexOfStudentId = CursorUtil.getColumnIndexOrThrow(_cursor, "studentId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfEnrollmentNo = CursorUtil.getColumnIndexOrThrow(_cursor, "enrollmentNo");
          final int _cursorIndexOfSection = CursorUtil.getColumnIndexOrThrow(_cursor, "section");
          final int _cursorIndexOfRollNo = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNo");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfMobile = CursorUtil.getColumnIndexOrThrow(_cursor, "mobile");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<EventStudent> _result = new ArrayList<EventStudent>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EventStudent _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpEventId;
            _tmpEventId = _cursor.getString(_cursorIndexOfEventId);
            final String _tmpStudentId;
            if (_cursor.isNull(_cursorIndexOfStudentId)) {
              _tmpStudentId = null;
            } else {
              _tmpStudentId = _cursor.getString(_cursorIndexOfStudentId);
            }
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpEnrollmentNo;
            _tmpEnrollmentNo = _cursor.getString(_cursorIndexOfEnrollmentNo);
            final String _tmpSection;
            if (_cursor.isNull(_cursorIndexOfSection)) {
              _tmpSection = null;
            } else {
              _tmpSection = _cursor.getString(_cursorIndexOfSection);
            }
            final String _tmpRollNo;
            if (_cursor.isNull(_cursorIndexOfRollNo)) {
              _tmpRollNo = null;
            } else {
              _tmpRollNo = _cursor.getString(_cursorIndexOfRollNo);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final String _tmpMobile;
            if (_cursor.isNull(_cursorIndexOfMobile)) {
              _tmpMobile = null;
            } else {
              _tmpMobile = _cursor.getString(_cursorIndexOfMobile);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            _item = new EventStudent(_tmpId,_tmpEventId,_tmpStudentId,_tmpName,_tmpEnrollmentNo,_tmpSection,_tmpRollNo,_tmpEmail,_tmpMobile,_tmpStatus,_tmpCreatedAt);
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
  public Flow<List<EventStudent>> searchByName(final String eventId, final String query) {
    final String _sql = "\n"
            + "        SELECT * FROM event_students \n"
            + "        WHERE eventId = ? AND name LIKE '%' || ? || '%' \n"
            + "        ORDER BY name ASC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, eventId);
    _argIndex = 2;
    _statement.bindString(_argIndex, query);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"event_students"}, new Callable<List<EventStudent>>() {
      @Override
      @NonNull
      public List<EventStudent> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "eventId");
          final int _cursorIndexOfStudentId = CursorUtil.getColumnIndexOrThrow(_cursor, "studentId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfEnrollmentNo = CursorUtil.getColumnIndexOrThrow(_cursor, "enrollmentNo");
          final int _cursorIndexOfSection = CursorUtil.getColumnIndexOrThrow(_cursor, "section");
          final int _cursorIndexOfRollNo = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNo");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfMobile = CursorUtil.getColumnIndexOrThrow(_cursor, "mobile");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<EventStudent> _result = new ArrayList<EventStudent>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EventStudent _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpEventId;
            _tmpEventId = _cursor.getString(_cursorIndexOfEventId);
            final String _tmpStudentId;
            if (_cursor.isNull(_cursorIndexOfStudentId)) {
              _tmpStudentId = null;
            } else {
              _tmpStudentId = _cursor.getString(_cursorIndexOfStudentId);
            }
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpEnrollmentNo;
            _tmpEnrollmentNo = _cursor.getString(_cursorIndexOfEnrollmentNo);
            final String _tmpSection;
            if (_cursor.isNull(_cursorIndexOfSection)) {
              _tmpSection = null;
            } else {
              _tmpSection = _cursor.getString(_cursorIndexOfSection);
            }
            final String _tmpRollNo;
            if (_cursor.isNull(_cursorIndexOfRollNo)) {
              _tmpRollNo = null;
            } else {
              _tmpRollNo = _cursor.getString(_cursorIndexOfRollNo);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final String _tmpMobile;
            if (_cursor.isNull(_cursorIndexOfMobile)) {
              _tmpMobile = null;
            } else {
              _tmpMobile = _cursor.getString(_cursorIndexOfMobile);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            _item = new EventStudent(_tmpId,_tmpEventId,_tmpStudentId,_tmpName,_tmpEnrollmentNo,_tmpSection,_tmpRollNo,_tmpEmail,_tmpMobile,_tmpStatus,_tmpCreatedAt);
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
  public Flow<List<EventStudent>> searchCombined(final String eventId, final String query) {
    final String _sql = "\n"
            + "        SELECT * FROM event_students \n"
            + "        WHERE eventId = ? AND (enrollmentNo LIKE '%' || ? || '%' OR name LIKE '%' || ? || '%')\n"
            + "        ORDER BY name ASC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, eventId);
    _argIndex = 2;
    _statement.bindString(_argIndex, query);
    _argIndex = 3;
    _statement.bindString(_argIndex, query);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"event_students"}, new Callable<List<EventStudent>>() {
      @Override
      @NonNull
      public List<EventStudent> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "eventId");
          final int _cursorIndexOfStudentId = CursorUtil.getColumnIndexOrThrow(_cursor, "studentId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfEnrollmentNo = CursorUtil.getColumnIndexOrThrow(_cursor, "enrollmentNo");
          final int _cursorIndexOfSection = CursorUtil.getColumnIndexOrThrow(_cursor, "section");
          final int _cursorIndexOfRollNo = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNo");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfMobile = CursorUtil.getColumnIndexOrThrow(_cursor, "mobile");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<EventStudent> _result = new ArrayList<EventStudent>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EventStudent _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpEventId;
            _tmpEventId = _cursor.getString(_cursorIndexOfEventId);
            final String _tmpStudentId;
            if (_cursor.isNull(_cursorIndexOfStudentId)) {
              _tmpStudentId = null;
            } else {
              _tmpStudentId = _cursor.getString(_cursorIndexOfStudentId);
            }
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpEnrollmentNo;
            _tmpEnrollmentNo = _cursor.getString(_cursorIndexOfEnrollmentNo);
            final String _tmpSection;
            if (_cursor.isNull(_cursorIndexOfSection)) {
              _tmpSection = null;
            } else {
              _tmpSection = _cursor.getString(_cursorIndexOfSection);
            }
            final String _tmpRollNo;
            if (_cursor.isNull(_cursorIndexOfRollNo)) {
              _tmpRollNo = null;
            } else {
              _tmpRollNo = _cursor.getString(_cursorIndexOfRollNo);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final String _tmpMobile;
            if (_cursor.isNull(_cursorIndexOfMobile)) {
              _tmpMobile = null;
            } else {
              _tmpMobile = _cursor.getString(_cursorIndexOfMobile);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            _item = new EventStudent(_tmpId,_tmpEventId,_tmpStudentId,_tmpName,_tmpEnrollmentNo,_tmpSection,_tmpRollNo,_tmpEmail,_tmpMobile,_tmpStatus,_tmpCreatedAt);
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
  public Flow<List<EventStudent>> getAllStudentsForEvent(final String eventId) {
    final String _sql = "SELECT * FROM event_students WHERE eventId = ? ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, eventId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"event_students"}, new Callable<List<EventStudent>>() {
      @Override
      @NonNull
      public List<EventStudent> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "eventId");
          final int _cursorIndexOfStudentId = CursorUtil.getColumnIndexOrThrow(_cursor, "studentId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfEnrollmentNo = CursorUtil.getColumnIndexOrThrow(_cursor, "enrollmentNo");
          final int _cursorIndexOfSection = CursorUtil.getColumnIndexOrThrow(_cursor, "section");
          final int _cursorIndexOfRollNo = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNo");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfMobile = CursorUtil.getColumnIndexOrThrow(_cursor, "mobile");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<EventStudent> _result = new ArrayList<EventStudent>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EventStudent _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpEventId;
            _tmpEventId = _cursor.getString(_cursorIndexOfEventId);
            final String _tmpStudentId;
            if (_cursor.isNull(_cursorIndexOfStudentId)) {
              _tmpStudentId = null;
            } else {
              _tmpStudentId = _cursor.getString(_cursorIndexOfStudentId);
            }
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpEnrollmentNo;
            _tmpEnrollmentNo = _cursor.getString(_cursorIndexOfEnrollmentNo);
            final String _tmpSection;
            if (_cursor.isNull(_cursorIndexOfSection)) {
              _tmpSection = null;
            } else {
              _tmpSection = _cursor.getString(_cursorIndexOfSection);
            }
            final String _tmpRollNo;
            if (_cursor.isNull(_cursorIndexOfRollNo)) {
              _tmpRollNo = null;
            } else {
              _tmpRollNo = _cursor.getString(_cursorIndexOfRollNo);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final String _tmpMobile;
            if (_cursor.isNull(_cursorIndexOfMobile)) {
              _tmpMobile = null;
            } else {
              _tmpMobile = _cursor.getString(_cursorIndexOfMobile);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            _item = new EventStudent(_tmpId,_tmpEventId,_tmpStudentId,_tmpName,_tmpEnrollmentNo,_tmpSection,_tmpRollNo,_tmpEmail,_tmpMobile,_tmpStatus,_tmpCreatedAt);
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
  public Object getStudentsListForEvent(final String eventId,
      final Continuation<? super List<EventStudent>> $completion) {
    final String _sql = "SELECT * FROM event_students WHERE eventId = ? ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, eventId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<EventStudent>>() {
      @Override
      @NonNull
      public List<EventStudent> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "eventId");
          final int _cursorIndexOfStudentId = CursorUtil.getColumnIndexOrThrow(_cursor, "studentId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfEnrollmentNo = CursorUtil.getColumnIndexOrThrow(_cursor, "enrollmentNo");
          final int _cursorIndexOfSection = CursorUtil.getColumnIndexOrThrow(_cursor, "section");
          final int _cursorIndexOfRollNo = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNo");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfMobile = CursorUtil.getColumnIndexOrThrow(_cursor, "mobile");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<EventStudent> _result = new ArrayList<EventStudent>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EventStudent _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpEventId;
            _tmpEventId = _cursor.getString(_cursorIndexOfEventId);
            final String _tmpStudentId;
            if (_cursor.isNull(_cursorIndexOfStudentId)) {
              _tmpStudentId = null;
            } else {
              _tmpStudentId = _cursor.getString(_cursorIndexOfStudentId);
            }
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpEnrollmentNo;
            _tmpEnrollmentNo = _cursor.getString(_cursorIndexOfEnrollmentNo);
            final String _tmpSection;
            if (_cursor.isNull(_cursorIndexOfSection)) {
              _tmpSection = null;
            } else {
              _tmpSection = _cursor.getString(_cursorIndexOfSection);
            }
            final String _tmpRollNo;
            if (_cursor.isNull(_cursorIndexOfRollNo)) {
              _tmpRollNo = null;
            } else {
              _tmpRollNo = _cursor.getString(_cursorIndexOfRollNo);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final String _tmpMobile;
            if (_cursor.isNull(_cursorIndexOfMobile)) {
              _tmpMobile = null;
            } else {
              _tmpMobile = _cursor.getString(_cursorIndexOfMobile);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            _item = new EventStudent(_tmpId,_tmpEventId,_tmpStudentId,_tmpName,_tmpEnrollmentNo,_tmpSection,_tmpRollNo,_tmpEmail,_tmpMobile,_tmpStatus,_tmpCreatedAt);
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
  public Flow<Integer> getStudentCountForEvent(final String eventId) {
    final String _sql = "SELECT COUNT(*) FROM event_students WHERE eventId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, eventId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"event_students"}, new Callable<Integer>() {
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
