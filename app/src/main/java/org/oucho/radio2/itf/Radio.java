/**
 *  Radio for android, internet radio.
 *
 * Copyright (C) 2016 Old Geek
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.oucho.radio2.itf;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class Radio implements PlayableItem {
	private final String url;
	private final String name;

	public Radio(String url, String name) {
		this.url = url;
		this.name = name;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getName() {
		return name;
	}
	
	public static ArrayList<Radio> getRadios() {
		RadiosDatabase radiosDatabase = new RadiosDatabase();
		SQLiteDatabase db = radiosDatabase.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT url, name FROM WebRadio ORDER BY NAME", null);
        ArrayList<Radio> radios = new ArrayList<>();
        while (cursor.moveToNext()) {
        	Radio radio = new Radio(cursor.getString(0), cursor.getString(1));
        	radios.add(radio);
        }
        db.close();
        cursor.close();
        return radios;
	}
	
	public static void addRadio(Radio radio) {
		RadiosDatabase radiosDatabase = new RadiosDatabase();
		ContentValues values = new ContentValues();
		values.put("url", radio.url);
		values.put("name", radio.name);
		try (SQLiteDatabase db = radiosDatabase.getWritableDatabase()) {
			db.insertOrThrow("WebRadio", null, values);
		} catch (Exception ignored) {
		}
	}
	
	public static void deleteRadio(Radio radio) {
		RadiosDatabase radiosDatabase = new RadiosDatabase();
		SQLiteDatabase db = radiosDatabase.getWritableDatabase();
		db.delete("WebRadio", "url = '" + radio.getUrl() + "'", null);
		db.close();
	}

	@Override
	public String getTitle() {
		return name;
	}

	@Override
	public String getPlayableUri() {
		return url;
	}

}
