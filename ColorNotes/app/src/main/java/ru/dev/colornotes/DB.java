package ru.dev.colornotes;

import android.appwidget.AppWidgetManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import java.io.Serializable;

/**
 * Класс для работы с БД
 */
public class DB implements Serializable {

    private static final String DB_NAME = "db_color_notes";
    private static final int DB_VERSION = 5;
    private static final String TABLE_NOTES = "notes";

    public static final String COLUMN_ID_NOTE = "_id";
    public static final String COLUMN_TEXT_NOTE = "text";
    public static final String COLUMN_DATE_ADD = "date_add";
    public static final String COLUMN_COLOR = "color";
    public static final String COLUMN_ID_WIDGET = "id_widget";

    // sql-код создания таблицы TABLE_NOTES
    private static final String  CREATE_TABLE_NOTES =
            "CREATE TABLE " + TABLE_NOTES + " (" +
                    COLUMN_ID_NOTE + " integer primary key, " +
                    COLUMN_TEXT_NOTE + " text," +
                COLUMN_DATE_ADD + " integer DEFAULT 0, " +
                COLUMN_COLOR + " integer DEFAULT 0, " +
                COLUMN_ID_WIDGET + " integer DEFAULT 0 " +
            ");";

    public Context context;
    private DBHelper dbHelper;
    private SQLiteDatabase db;

    private DB(Context context) {
        this.context = context;
    }

    private static DB instance;

    public static DB init(Context context) {
        if (instance == null) {
            instance = new DB(context);
        }
        return instance;
    }

    public static DB getInstance() throws Exception {
        if (instance == null) {
            throw new Exception("Instance is null. Call init before.");
        }
        return instance;
    }

    /**
     * Открытие соединения с БД
     */
    public void openConnection() {
        if (db != null && db.isOpen()) {
            return;
        }
        dbHelper = new DBHelper(context, DB_NAME, null, DB_VERSION);
        db = dbHelper.getWritableDatabase();
    }

    /**
     * Закрытие соединения с БД
     */
    public void closeConnection() {
        try {
            if (dbHelper != null) dbHelper.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Проверка соединения с БД, если оно закрыто, то открываем
     */
    private void checkConnection() {
        if (!db.isOpen()) {
            openConnection();
        }
    }

    /**
     * Добавление записи в таблицу TABLE_NOTES
     * @param text текст заметки
     * @param color цвет заметки
     * @return long ИД вставленной записи
     * @throws Exception
     */
    public long addNote(String text, int color) throws Exception {
        int time = (int)((new java.util.Date()).getTime()/1000);
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TEXT_NOTE, text);
        cv.put(COLUMN_DATE_ADD, time);
        cv.put(COLUMN_COLOR, color);

        checkConnection();
        long id = db.insert(TABLE_NOTES, null, cv);
        if (id <= 0) {
            throw new Exception("Error insert data.");
        }
        return id;
    }

    /**
     * Удаление записи из таблицы TABLE_NOTES
     * @param id ИД заметки
     */
    public void deleteNote(long id) {
        checkConnection();
        db.delete(TABLE_NOTES, COLUMN_ID_NOTE + " = '" + id + "'", null);
    }

    /**
     * Множественное удаление заметок
     * @param ids массив идентификаторов заметок
     */
    public void multiDeleteNotes(Object[] ids) {
        checkConnection();
        db.delete(TABLE_NOTES, COLUMN_ID_NOTE + " IN (" + TextUtils.join(",", ids) + ")", null);
    }

    /**
     * Изменение записи
     * @param id ИД заметки
     * @param text текст заметки
     * @param color цвет заметки
     */
    public void changeNote(long id, String text, int color) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TEXT_NOTE, text);
        cv.put(COLUMN_COLOR, color);

        checkConnection();
        db.update(TABLE_NOTES, cv, COLUMN_ID_NOTE + " = '" + id + "'", null);

        // обновляем виджет, если такой имеется для отредактированной заметки
        WidgetNote.updateWidgetByIdNote(context, id);
    }

    /**
     * Изменение цвета заметки
     * @param id ИД заметки
     * @param color цвет заметки
     */
    public void changeColorNote(long id, int color) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_COLOR, color);

        checkConnection();
        db.update(TABLE_NOTES, cv, COLUMN_ID_NOTE + " = '" + id + "'", null);

        // обновляем виджет, если такой имеется для отредактированной заметки
        WidgetNote.updateWidgetByIdNote(context, id);
    }

    /**
     * Получение всех записей
     * @return Cursor
     */
    public Cursor getAllNotes() {
        String columns[] = new String[] { COLUMN_ID_NOTE, COLUMN_TEXT_NOTE, COLUMN_COLOR,
                "strftime('%d-%m-%Y %H:%M', " + COLUMN_DATE_ADD + ", 'unixepoch', 'localtime') as " + COLUMN_DATE_ADD, COLUMN_ID_WIDGET };

        checkConnection();
        return db.query(TABLE_NOTES, columns, null, null, null, null, null);
    }

    /**
     * Получение заметок, которые не имеют виджета
     * @return Cursor
     */
    public Cursor getNotesNotWidget() {
        String columns[] = new String[] { COLUMN_ID_NOTE, COLUMN_TEXT_NOTE, COLUMN_COLOR, COLUMN_ID_WIDGET,
                "strftime('%d-%m-%Y %H:%M', " + COLUMN_DATE_ADD + ", 'unixepoch', 'localtime') as " + COLUMN_DATE_ADD };

        checkConnection();
        return db.query(TABLE_NOTES, columns, COLUMN_ID_WIDGET + " = " + AppWidgetManager.INVALID_APPWIDGET_ID, null, null, null, null);
    }

    /**
     * Получение данных заметки по ИД виджета, привязанного к ней
     * @param idWidget ИД виджета
     * @return Cursor
     */
    public Cursor getNoteInfoByIdWidget(int idWidget) {
        String columns[] = new String[] { COLUMN_ID_NOTE, COLUMN_TEXT_NOTE, COLUMN_COLOR, COLUMN_ID_WIDGET,
                "strftime('%d-%m-%Y %H:%M', " + COLUMN_DATE_ADD + ", 'unixepoch', 'localtime') as " + COLUMN_DATE_ADD };

        checkConnection();
        return db.query(TABLE_NOTES, columns, COLUMN_ID_WIDGET + " = " + idWidget, null, null, null, null, "1");
    }

    /**
     * Получение данных заметки по ИД
     * @param id ИД заметки
     * @return Cursor
     */
    public Cursor getNoteInfoById(long id) {
        String columns[] = new String[] { COLUMN_ID_NOTE, COLUMN_TEXT_NOTE, COLUMN_COLOR, COLUMN_ID_WIDGET,
                "strftime('%d-%m-%Y %H:%M', " + COLUMN_DATE_ADD + ", 'unixepoch', 'localtime') as " + COLUMN_DATE_ADD };

        checkConnection();
        return db.query(TABLE_NOTES, columns, COLUMN_ID_NOTE + " = " + id, null, null, null, null, "1");
    }

    /**
     * Устанавливаем флаг наличия виджета
     * @param id ИД заметки
     * @param idWidget ID виджета
     */
    public void setIdWidget(long id, int idWidget) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ID_WIDGET, idWidget);

        checkConnection();

        db.update(TABLE_NOTES, cv, COLUMN_ID_NOTE + " = '" + id + "'", null);
    }

    /**
     * Удаление привязки виджета к какой-либо заметке
     * @param idWidget ИД виджета
     */
    public void unSetWidget(int idWidget) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ID_WIDGET, AppWidgetManager.INVALID_APPWIDGET_ID);

        checkConnection();

        try {
            db.update(TABLE_NOTES, cv, COLUMN_ID_WIDGET + " = " + idWidget, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Класс по созданию и управлению БД
    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String dbName, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, dbName, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // создаем таблицу TABLE_NOTES
            db.execSQL(CREATE_TABLE_NOTES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String sql;
            try {
                sql = "CREATE TABLE new_" + TABLE_NOTES + " (" +
                        COLUMN_ID_NOTE + " integer primary key, " +
                        COLUMN_TEXT_NOTE + " text," +
                        COLUMN_DATE_ADD + " integer DEFAULT 0, " +
                        COLUMN_COLOR + " integer DEFAULT 0, " +
                        COLUMN_ID_WIDGET + " integer DEFAULT 0 " +
                        ");";
                db.execSQL(sql);
                sql = "INSERT INTO new_" + TABLE_NOTES + " SELECT " +
                        COLUMN_ID_NOTE + ", " +
                        COLUMN_TEXT_NOTE + ", " +
                        COLUMN_DATE_ADD + ", " +
                        COLUMN_COLOR + "," +
                        "0 as " + COLUMN_ID_WIDGET + " FROM " + TABLE_NOTES + ";";
                db.execSQL(sql);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES + ";");
                db.execSQL("ALTER TABLE new_" + TABLE_NOTES + " RENAME TO " + TABLE_NOTES + ";");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
