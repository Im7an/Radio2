package org.oucho.radio2.itf;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;

import org.oucho.radio2.MainActivity;
import org.oucho.radio2.R;

import java.util.ArrayList;
import java.util.Random;

public class Radio implements PlayableItem {
	private String url;
	private String name;

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
        return radios;
	}
	
	public static void addRadio(Radio radio) {
		RadiosDatabase radiosDatabase = new RadiosDatabase();
		SQLiteDatabase db = radiosDatabase.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("url", radio.url);
		values.put("name", radio.name);
		try {
			db.insertOrThrow("WebRadio", null, values);
		} catch(Exception e) {
		} finally {
			db.close();
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
	public String getArtist() {
		return "[" + MainActivity.getContext().getResources().getString(R.string.app_name) +"]";
	}

	@Override
	public String getPlayableUri() {
		return url;
	}
	
	@Override
	public boolean hasImage() {
		return false;
	}
	
	@Override
	public Bitmap getImage() {
		return null;
	}

	@Override
	public PlayableItem getNext(boolean repeatAll) {
		return null;
	}

	@Override
	public PlayableItem getPrevious() {
		return null;
	}

	@Override
	public PlayableItem getRandom(Random random) {
		return null;
	}

	@Override
	public boolean isLengthAvailable() {
		return false;
	}
	
	@Override
	public ArrayList<Information> getInformation() {
		ArrayList<Information> info = new ArrayList<>();
		info.add(new Information(R.string.radio_name, name));
		info.add(new Information(R.string.url, url));
		return info;
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Radio)) return false;
		Radio r2 = (Radio)o;
		return url.equals(r2.getUrl());
	}
}
