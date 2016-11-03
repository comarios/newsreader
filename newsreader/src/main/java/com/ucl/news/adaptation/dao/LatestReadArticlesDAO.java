package com.ucl.news.adaptation.dao;

import com.ucl.news.data.NewsDbHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class LatestReadArticlesDAO {
	
	private Context mContext;
	private NewsDbHelper mDbHelper;
	
	public LatestReadArticlesDAO(Context context) {
		mContext = context;
		mDbHelper = new NewsDbHelper(mContext);
	}
	
	public Cursor getLatestReadArticles(String[] columns, int limitTo) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		
		Cursor cursor = db.query(NewsDbHelper.LATEST_READ_ARTICLES_TABLE,
				columns, null, null, null, null, NewsDbHelper.LATEST_READ_ARTICLES_TIMESTAMP + " DESC",
				String.valueOf(limitTo));
		
		//db.close();
		return cursor;
	}
	
	public long insertOrUpdateLatestReadArticle(String title, long timestamp) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(NewsDbHelper.LATEST_READ_ARTICLES_TITLE, title);
		contentValues.put(NewsDbHelper.LATEST_READ_ARTICLES_TIMESTAMP, timestamp);
		
		long id = db.insert(NewsDbHelper.LATEST_READ_ARTICLES_TABLE, null, contentValues);
		
		//db.close();
		
		return id;
	}
}
