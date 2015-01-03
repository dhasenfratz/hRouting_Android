package ch.ethz.tik.hrouting.providers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import ch.ethz.tik.hrouting.util.HistoryDBContract.HistoryEntry;

public class HistoryDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 7;
    public static final String DATABASE_NAME = "PathHistory.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String BLOB_TYPE = " BLOB";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE "
            + HistoryEntry.TABLE_NAME + " (" + HistoryEntry._ID
            + " INTEGER PRIMARY KEY" + COMMA_SEP
            + HistoryEntry.COLUMN_FROM_NAME + TEXT_TYPE + COMMA_SEP
            + HistoryEntry.COLUMN_TO_NAME + TEXT_TYPE + COMMA_SEP
            + HistoryEntry.COLUMN_ROUTE + BLOB_TYPE + COMMA_SEP
            + HistoryEntry.COLUMN_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP"
            + " )";

    private static final String SQL_DELETE_HISTORY = "DELETE FROM "
            + HistoryEntry.TABLE_NAME;

    private static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS "
            + HistoryEntry.TABLE_NAME;

    public HistoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Right now we drop the table and re-create it.
        db.execSQL(SQL_DROP_TABLE);
        onCreate(db);

    }

    public void deleteHistory() {
        getWritableDatabase().execSQL(SQL_DELETE_HISTORY);
    }

    public Cursor getHistory() {
        return getReadableDatabase().rawQuery(
                "select * from " + HistoryEntry.TABLE_NAME + " order by "
                        + HistoryEntry.COLUMN_DATE + " desc", null);
    }

    public void removeDuplicate(String from, String to) {
        String where = "(" + HistoryEntry.COLUMN_FROM_NAME + "=?" + " and "
                + HistoryEntry.COLUMN_TO_NAME + "=?) or ("
                + HistoryEntry.COLUMN_FROM_NAME + "=?" + " and "
                + HistoryEntry.COLUMN_TO_NAME + "=?)";
        String[] whereArgs = { from, to, to, from };
        getReadableDatabase().delete(HistoryEntry.TABLE_NAME, where, whereArgs);
    }

    public long getNrOfElements() {
        String sql = "SELECT COUNT(*) FROM " + HistoryEntry.TABLE_NAME;
        SQLiteStatement statement = getReadableDatabase().compileStatement(sql);
        return statement.simpleQueryForLong();
    }

    public void removeOldest() {
        String where = HistoryEntry.COLUMN_DATE + " = (select min("
                + HistoryEntry.COLUMN_DATE + ") from "
                + HistoryEntry.TABLE_NAME + ")";
        getReadableDatabase().delete(HistoryEntry.TABLE_NAME, where, null);
    }
}
