//
//  CustomCursorAdapter.java
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

package ch.ethz.tik.hrouting.providers;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import ch.ethz.tik.hrouting.R;
import ch.ethz.tik.hrouting.util.HistoryDBContract.HistoryEntry;
import ch.ethz.tik.graphgenerator.util.Constants;

public class CustomCursorAdapter extends CursorAdapter {

    private LayoutInflater inflater;

    public CustomCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.history_entry, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String from = cursor.getString(cursor.getColumnIndex(HistoryEntry.COLUMN_FROM_NAME));
        String to = cursor.getString(cursor.getColumnIndex(HistoryEntry.COLUMN_TO_NAME));
        long time = cursor.getLong(cursor.getColumnIndex(HistoryEntry.COLUMN_DATE));
        String date = displayFormat.format(new Date(time));

        TextView nameView = (TextView) view.findViewById(R.id.entry_name);
        nameView.setText(from + Constants.ARROW +  to);
        TextView dateView = (TextView) view.findViewById(R.id.entry_date);
        dateView.setText(date);
    }
}
