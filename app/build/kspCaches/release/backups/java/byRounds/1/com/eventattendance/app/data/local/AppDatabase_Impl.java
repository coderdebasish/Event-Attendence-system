package com.eventattendance.app.data.local;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile EventDao _eventDao;

  private volatile StudentDao _studentDao;

  private volatile AttendanceDao _attendanceDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `events` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `description` TEXT, `eventDate` TEXT NOT NULL, `status` TEXT NOT NULL, `allowAttendance` INTEGER NOT NULL, `createdBy` TEXT, `createdAt` TEXT, `registrationCount` INTEGER NOT NULL, `attendanceCount` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `event_students` (`id` TEXT NOT NULL, `eventId` TEXT NOT NULL, `studentId` TEXT, `name` TEXT NOT NULL, `enrollmentNo` TEXT NOT NULL, `section` TEXT, `rollNo` TEXT, `email` TEXT, `mobile` TEXT, `status` TEXT NOT NULL, `createdAt` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_event_students_eventId_enrollmentNo` ON `event_students` (`eventId`, `enrollmentNo`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_event_students_eventId_name` ON `event_students` (`eventId`, `name`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_event_students_enrollmentNo` ON `event_students` (`enrollmentNo`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `attendance_records` (`id` TEXT NOT NULL, `eventId` TEXT NOT NULL, `eventStudentId` TEXT NOT NULL, `enrollmentNo` TEXT NOT NULL, `markedBy` TEXT, `operatorName` TEXT, `deviceId` TEXT, `status` TEXT NOT NULL, `markedAt` TEXT NOT NULL, `createdAt` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_attendance_records_eventId_enrollmentNo` ON `attendance_records` (`eventId`, `enrollmentNo`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_attendance_records_eventId` ON `attendance_records` (`eventId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_attendance_records_markedAt` ON `attendance_records` (`markedAt`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '9d11ec49e1d6e8dbf50be078cc463044')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `events`");
        db.execSQL("DROP TABLE IF EXISTS `event_students`");
        db.execSQL("DROP TABLE IF EXISTS `attendance_records`");
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
        final HashMap<String, TableInfo.Column> _columnsEvents = new HashMap<String, TableInfo.Column>(10);
        _columnsEvents.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEvents.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEvents.put("description", new TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEvents.put("eventDate", new TableInfo.Column("eventDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEvents.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEvents.put("allowAttendance", new TableInfo.Column("allowAttendance", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEvents.put("createdBy", new TableInfo.Column("createdBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEvents.put("createdAt", new TableInfo.Column("createdAt", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEvents.put("registrationCount", new TableInfo.Column("registrationCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEvents.put("attendanceCount", new TableInfo.Column("attendanceCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysEvents = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesEvents = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoEvents = new TableInfo("events", _columnsEvents, _foreignKeysEvents, _indicesEvents);
        final TableInfo _existingEvents = TableInfo.read(db, "events");
        if (!_infoEvents.equals(_existingEvents)) {
          return new RoomOpenHelper.ValidationResult(false, "events(com.eventattendance.app.data.model.Event).\n"
                  + " Expected:\n" + _infoEvents + "\n"
                  + " Found:\n" + _existingEvents);
        }
        final HashMap<String, TableInfo.Column> _columnsEventStudents = new HashMap<String, TableInfo.Column>(11);
        _columnsEventStudents.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEventStudents.put("eventId", new TableInfo.Column("eventId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEventStudents.put("studentId", new TableInfo.Column("studentId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEventStudents.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEventStudents.put("enrollmentNo", new TableInfo.Column("enrollmentNo", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEventStudents.put("section", new TableInfo.Column("section", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEventStudents.put("rollNo", new TableInfo.Column("rollNo", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEventStudents.put("email", new TableInfo.Column("email", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEventStudents.put("mobile", new TableInfo.Column("mobile", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEventStudents.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEventStudents.put("createdAt", new TableInfo.Column("createdAt", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysEventStudents = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesEventStudents = new HashSet<TableInfo.Index>(3);
        _indicesEventStudents.add(new TableInfo.Index("index_event_students_eventId_enrollmentNo", true, Arrays.asList("eventId", "enrollmentNo"), Arrays.asList("ASC", "ASC")));
        _indicesEventStudents.add(new TableInfo.Index("index_event_students_eventId_name", false, Arrays.asList("eventId", "name"), Arrays.asList("ASC", "ASC")));
        _indicesEventStudents.add(new TableInfo.Index("index_event_students_enrollmentNo", false, Arrays.asList("enrollmentNo"), Arrays.asList("ASC")));
        final TableInfo _infoEventStudents = new TableInfo("event_students", _columnsEventStudents, _foreignKeysEventStudents, _indicesEventStudents);
        final TableInfo _existingEventStudents = TableInfo.read(db, "event_students");
        if (!_infoEventStudents.equals(_existingEventStudents)) {
          return new RoomOpenHelper.ValidationResult(false, "event_students(com.eventattendance.app.data.model.EventStudent).\n"
                  + " Expected:\n" + _infoEventStudents + "\n"
                  + " Found:\n" + _existingEventStudents);
        }
        final HashMap<String, TableInfo.Column> _columnsAttendanceRecords = new HashMap<String, TableInfo.Column>(10);
        _columnsAttendanceRecords.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAttendanceRecords.put("eventId", new TableInfo.Column("eventId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAttendanceRecords.put("eventStudentId", new TableInfo.Column("eventStudentId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAttendanceRecords.put("enrollmentNo", new TableInfo.Column("enrollmentNo", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAttendanceRecords.put("markedBy", new TableInfo.Column("markedBy", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAttendanceRecords.put("operatorName", new TableInfo.Column("operatorName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAttendanceRecords.put("deviceId", new TableInfo.Column("deviceId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAttendanceRecords.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAttendanceRecords.put("markedAt", new TableInfo.Column("markedAt", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAttendanceRecords.put("createdAt", new TableInfo.Column("createdAt", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAttendanceRecords = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAttendanceRecords = new HashSet<TableInfo.Index>(3);
        _indicesAttendanceRecords.add(new TableInfo.Index("index_attendance_records_eventId_enrollmentNo", true, Arrays.asList("eventId", "enrollmentNo"), Arrays.asList("ASC", "ASC")));
        _indicesAttendanceRecords.add(new TableInfo.Index("index_attendance_records_eventId", false, Arrays.asList("eventId"), Arrays.asList("ASC")));
        _indicesAttendanceRecords.add(new TableInfo.Index("index_attendance_records_markedAt", false, Arrays.asList("markedAt"), Arrays.asList("ASC")));
        final TableInfo _infoAttendanceRecords = new TableInfo("attendance_records", _columnsAttendanceRecords, _foreignKeysAttendanceRecords, _indicesAttendanceRecords);
        final TableInfo _existingAttendanceRecords = TableInfo.read(db, "attendance_records");
        if (!_infoAttendanceRecords.equals(_existingAttendanceRecords)) {
          return new RoomOpenHelper.ValidationResult(false, "attendance_records(com.eventattendance.app.data.model.AttendanceRecord).\n"
                  + " Expected:\n" + _infoAttendanceRecords + "\n"
                  + " Found:\n" + _existingAttendanceRecords);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "9d11ec49e1d6e8dbf50be078cc463044", "6422c1154c22531356ccccb399e76daf");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "events","event_students","attendance_records");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `events`");
      _db.execSQL("DELETE FROM `event_students`");
      _db.execSQL("DELETE FROM `attendance_records`");
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
    _typeConvertersMap.put(EventDao.class, EventDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(StudentDao.class, StudentDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AttendanceDao.class, AttendanceDao_Impl.getRequiredConverters());
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
  public EventDao eventDao() {
    if (_eventDao != null) {
      return _eventDao;
    } else {
      synchronized(this) {
        if(_eventDao == null) {
          _eventDao = new EventDao_Impl(this);
        }
        return _eventDao;
      }
    }
  }

  @Override
  public StudentDao studentDao() {
    if (_studentDao != null) {
      return _studentDao;
    } else {
      synchronized(this) {
        if(_studentDao == null) {
          _studentDao = new StudentDao_Impl(this);
        }
        return _studentDao;
      }
    }
  }

  @Override
  public AttendanceDao attendanceDao() {
    if (_attendanceDao != null) {
      return _attendanceDao;
    } else {
      synchronized(this) {
        if(_attendanceDao == null) {
          _attendanceDao = new AttendanceDao_Impl(this);
        }
        return _attendanceDao;
      }
    }
  }
}
