package ch.ethz.tik.hrouting.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;

import ch.ethz.tik.graphgenerator.elements.Route;
import ch.ethz.tik.hrouting.providers.HistoryDbHelper;

/**
 * The contract for the history view database table.
 */
public final class HistoryDBContract {

    private HistoryDBContract() {
    }

    public static abstract class HistoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "history";
        public static final String COLUMN_FROM_NAME = "fromName";
        public static final String COLUMN_TO_NAME = "toName";
        public static final String COLUMN_ROUTE = "route";
        public static final String COLUMN_DATE = "lastModified";

        public static void addHistoryEntry(HistoryDbHelper dbHelper,
                                           Route route, Context context) {
            dbHelper.removeDuplicate(route.getFrom().getName(), route.getTo().getName());
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COLUMN_FROM_NAME, route.getFrom().getName());
            values.put(COLUMN_TO_NAME, route.getTo().getName());
            values.put(COLUMN_ROUTE,serialize(route));
            values.put(COLUMN_DATE, Calendar.getInstance().getTime().getTime());
            db.insert(TABLE_NAME, null, values);

            checkHistorySize(dbHelper, context);

        }

        public static void checkHistorySize(HistoryDbHelper dbHelper, Context context) {
            int maxSize = PreferenceManager.getDefaultSharedPreferences(context)
                    .getInt("size_history", 20);
            int currSize = dbHelper.getNrOfElements();
            for (int i = 0; i < currSize-maxSize; ++i) {
                dbHelper.removeOldest();
            }
        }

        private static byte[] serialize(Route route) {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
            try {
                ObjectOutputStream o = new ObjectOutputStream(b);
                o.writeObject(route);
                o.flush();
                o.close();
                b.close();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            return b.toByteArray();
        }

        public static Route deserialize(byte[] bytes) {
            Route route;
            try {
                ByteArrayInputStream b = new ByteArrayInputStream(bytes);
                ObjectInputStream o = new ObjectInputStream(b);
                route = (Route)o.readObject();
                o.close();
                b.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
            return route;
        }
    }
}