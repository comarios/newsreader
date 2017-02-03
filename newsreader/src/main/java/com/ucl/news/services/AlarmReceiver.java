package com.ucl.news.services;

/**
 * Created by danyaalmasood on 26/01/2017.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ucl.news.main.ArticleActivity;
import com.ucl.news.reader.RetrieveFeedTask;
import com.ucl.news.utils.UriInOut;
import com.ucl.newsreader.R;

import java.util.ArrayList;
import java.util.List;

import main.java.org.mcsoxford.rss.RSSItem;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.ucl.news.main.MainActivity.PREFS_NAME;


public class AlarmReceiver extends BroadcastReceiver implements RetrieveFeedTask.AsyncResponse {
    private RetrieveFeedTask asyncTask;
    private Context context;
    private List<RSSItem> trackedArticles;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        boolean trackerFlag = settings.getBoolean("trackerFlag", false);
        String json = settings.getString("trackedArticles", "");
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriInOut())
                .create();
        trackedArticles = gson.fromJson(json, new TypeToken<List<RSSItem>>() {}.getType());
        if (trackerFlag && !json.equals("") && trackedArticles != null) {
            fetchRSS("*");
        }
    }

    public void fetchRSS(String searchKey) {
        asyncTask = new RetrieveFeedTask(context, searchKey);
        asyncTask.execute("http://feeds.bbci.co.uk/news/rss.xml",
                "http://feeds.bbci.co.uk/news/world/rss.xml",
                "http://feeds.bbci.co.uk/news/uk/rss.xml",
                "http://feeds.bbci.co.uk/news/business/rss.xml",
                "http://feeds.bbci.co.uk/sport/0/rss.xml",
                "http://feeds.bbci.co.uk/news/politics/rss.xml",
                "http://feeds.bbci.co.uk/news/health/rss.xml",
                "http://feeds.bbci.co.uk/news/education/rss.xml",
                "http://feeds.bbci.co.uk/news/science_and_environment/rss.xml",
                "http://feeds.bbci.co.uk/news/technology/rss.xml",
                "http://feeds.bbci.co.uk/news/entertainment_and_arts/rss.xml");
        asyncTask.delegate = this;
    }

    @Override
    public void processFinish(ArrayList<List<RSSItem>> outputFeed) {
        for (List<RSSItem> out : outputFeed) {
            for (int i = 0; i < out.size(); i++) {
                for (int j = 0; j < trackedArticles.size(); j++) {
                    if (out.get(i).getTitle().equals(trackedArticles.get(j).getTitle())) {
                        if (!(out.get(i).getPubDate().equals(trackedArticles.get(j).getPubDate()))) {
                            generatePushNotification(trackedArticles.get(j));
                        }
                    }
                }
            }
        }
    }

    public void generatePushNotification(RSSItem item) {
        Log.v("InAlarm", "push");
        Intent intent = new Intent(context, ArticleActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, 0);
        Notification noti = new Notification.Builder(context)
                .setContentTitle(item.getTitle())
                .setContentText(item.getDescription())
                .setSmallIcon(R.drawable.habitonews_logo_header)
                .setContentIntent(pIntent)
                .build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, noti);
    }
}