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
