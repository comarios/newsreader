package com.ucl.news.utils;

import android.content.Context;
import android.util.Log;

import com.ucl.news.adaptation.dao.LatestReadArticlesDAO;

public class Util {
	public static void articleRead(String articleName, Context context) {
		long timeStamp = System.currentTimeMillis();
		
		LatestReadArticlesDAO latestReadArticlesDao = new LatestReadArticlesDAO(context);
		latestReadArticlesDao.insertOrUpdateLatestReadArticle(articleName, timeStamp);
		
		Log.d(Util.class.getSimpleName(), "Tracked article " + articleName);
	}
}
