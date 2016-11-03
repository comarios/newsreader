package com.ucl.news.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NewsDbHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "news.db";
	
	public static final String LATEST_READ_ARTICLES_TABLE = "latest_read_articles";
	public static final String LATEST_READ_ARTICLES_ID = "_id";
	public static final String LATEST_READ_ARTICLES_TITLE = "title";
	public static final String LATEST_READ_ARTICLES_TIMESTAMP = "timestamp";
	
	public  NewsDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		final String SQL_CREATE_LATEST_READ_ARTICLES_TABLE = "CREATE TABLE " + LATEST_READ_ARTICLES_TABLE + "(" +
				LATEST_READ_ARTICLES_ID + " INTEGER PRIMARY KEY, " +
				LATEST_READ_ARTICLES_TITLE + " TEXT NOT NULL, " +
				LATEST_READ_ARTICLES_TIMESTAMP + " INTEGER NOT NULL," +
				" UNIQUE (" + LATEST_READ_ARTICLES_TITLE + ") ON CONFLICT REPLACE);";
		
		db.execSQL(SQL_CREATE_LATEST_READ_ARTICLES_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + LATEST_READ_ARTICLES_TABLE);
		onCreate(db);
	}
}
