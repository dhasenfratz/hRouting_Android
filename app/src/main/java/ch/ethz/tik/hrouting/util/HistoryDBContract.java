//
//  HistoryDBContract.java
//  hRouting
//
//  Created by Ivo de Concini, David Hasenfratz on 08/01/15.
//  Copyright (c) 2015 TIK, ETH Zurich. All rights reserved.
//
//  hRouting is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  hRouting is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with hRouting.  If not, see <http://www.gnu.org/licenses/>.
//

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