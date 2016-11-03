package com.ucl.news.adaptation.main;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import main.java.org.mcsoxford.rss.RSSItem;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.ucl.news.adaptation.adapters.RowsAdapterDippers;
import com.ucl.news.adaptation.adapters.TrackersFragment;
import com.ucl.news.adaptation.dao.LatestReadArticlesDAO;
import com.ucl.news.api.NavigationDAO;
import com.ucl.news.api.Session;
import com.ucl.news.data.NewsDbHelper;
import com.ucl.news.main.LoginActivity;
import com.ucl.news.reader.News;
import com.ucl.news.reader.RetrieveFeedTask;
import com.ucl.news.reader.RetrieveFeedTask.AsyncResponse;
import com.ucl.news.utils.AutoLogin;
import com.ucl.news.utils.Dialogs;
import com.ucl.news.utils.NetworkConnection;
import com.ucl.newsreader.R;

public class MainActivityTrackers extends FragmentActivity implements AsyncResponse {
	private LatestReadArticlesDAO mLatestReadArticlesDAO;
	private ArrayList<News> news;
	private ProgressBar progress;

	private RetrieveFeedTask asyncTask;
	private String[] categories = { "Top Stories", "World", "UK", "Business",
			"Sports", "Politics", "Health", "Education & Family",
			"Science & Environment", "Technology", "Entertainment & Arts" };
	private NetworkConnection network = new NetworkConnection(MainActivityTrackers.this);
	private Intent logger;
	public static Session userSession = new Session();
	public static ArrayList<NavigationDAO> navigationDAO = new ArrayList<NavigationDAO>();
	public static boolean activitySwitchFlag = false;
	// public static File scrollPositionFile;
	// public static File runningAppsFile;
	// public static File navigationalDataFile;
	private Intent newsAppsService;
	public static boolean CallingFromArticleActivity = false;
	private ViewPager pager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		System.out.println("ON create called now2");
		setContentView(R.layout.viewpager_main_trackers);

		if (network.haveNetworkConnection()) {

			// Retrieve user class. Implement this for adaptation
			/**
			 * IF user is A then IF user is B then IF user is C then
			 */

			progress = (ProgressBar) findViewById(R.id.progressBar);

			pager = (ViewPager)findViewById(R.id.trackers_pager);

			news = new ArrayList<News>();
			
			//Get latest articles
			mLatestReadArticlesDAO = new LatestReadArticlesDAO(this);
			// if(!AutoLogin.getIsLoggedIN(AutoLogin.getSettingsFile(getApplicationContext())))
			fetchRSS("*");
			CallingFromArticleActivity = false;

			/*
			 * Get GPS Location

			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			LocationListener locationListener = new GPSLocation(
					getApplicationContext());
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 5000, 2, locationListener);

			this.newsAppsService = new Intent(NewsAppsService.class.getName());
			this.startService(newsAppsService);

			LOCATION DETAILS SHOWN*/

			// /* Commented coz everything is stored in the server
			// * Create dir for storing scroll position
			// */
			// if (!Environment.getExternalStorageState().equals(
			// Environment.MEDIA_MOUNTED)) {
			// // handle case of no SDCARD present
			// } else {
			// String dir = Environment.getExternalStorageDirectory()
			// + File.separator + "HabitoNews_Study";
			// // create folder
			// File folder = new File(dir); // folder name
			// if (!folder.exists()) {
			// folder.mkdir();
			// }
			// // create ScrollPosition file
			// scrollPositionFile = new File(dir, "scroll_position.txt");
			// try {
			// scrollPositionFile.createNewFile();
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			//
			// // create RunningAppsFile file
			// runningAppsFile = new File(dir, "news_runningApps.txt");
			// try {
			// runningAppsFile.createNewFile();
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			//
			// // create Navigational Data file
			// navigationalDataFile = new File(dir, "navigational_data.txt");
			// try {
			// navigationalDataFile.createNewFile();
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			//
			// }

		} else {
			new Dialogs().createDialogINTERNET(MainActivityTrackers.this,
					getApplicationContext());
		}

	}

	public News getNewsPage(int indx) {
		return news.get(indx);
	}

	public void fetchRSS(String searchKey) {
		progress.setVisibility(View.VISIBLE);
		asyncTask = new RetrieveFeedTask(getApplicationContext(), searchKey);
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
		asyncTask.delegate = (com.ucl.news.reader.RetrieveFeedTask.AsyncResponse) MainActivityTrackers.this;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_sync:

			fetchRSS("*");
			return true;
		case R.id.action_logout:
			logout();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void processFinish(ArrayList<List<RSSItem>> outputFeed) {
		// TODO Auto-generated method stub

		// Initial stage
		if (news.size() != categories.length) {
			for (int i = 0; i < outputFeed.size(); i++) {
				news.add(new News(categories[i], outputFeed.get(i)));
			}
			pager.setAdapter(new ArticlesPagerAdapter(getSupportFragmentManager(), news));
		} else {
			for (int j = 0; j < news.size(); j++) {
				news.get(j).setContent(outputFeed.get(j));
			}
		}

		updateLatestReadArticles();
		progress.setVisibility(View.INVISIBLE);
	}

	public void logout() {
		String updateCredentials;
		updateCredentials = "NO"
				+ ";"
				+ AutoLogin.getUserID(AutoLogin
						.getSettingsFile(getApplicationContext()))
						+ ";"
						+ AutoLogin.getUserSession(AutoLogin
								.getSettingsFile(getApplicationContext())) + ";";
		AutoLogin.saveSettingsFile(getApplicationContext(), updateCredentials);

		System.out.println("logout: "
				+ AutoLogin.getSettingsFile(getApplicationContext()));
		Intent i = new Intent(MainActivityTrackers.this, LoginActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(i);
		super.finish();
		this.finish();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onPause() {
		super.onPause();
		CallingFromArticleActivity = false;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Log.d("CDA", "onBackPressed Called");
		Intent setIntent = new Intent(Intent.ACTION_MAIN);
		setIntent.addCategory(Intent.CATEGORY_HOME);
		setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(setIntent);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!CallingFromArticleActivity) {
			String updateCredentials;
			updateCredentials = "YES"
					+ ";"
					+ AutoLogin.getUserID(AutoLogin
							.getSettingsFile(getApplicationContext())) + ";"
							+ UUID.randomUUID().toString() + ";";
			AutoLogin.saveSettingsFile(getApplicationContext(),
					updateCredentials);
			System.out.println("resume from outside, update session");
		} else {
			System.out
			.println("resume from articleactivity, do not update session");
		}
		
		updateLatestReadArticles();
	}
	
	// UPDATE TRACKER (latest read) ARTICLES
	private void updateLatestReadArticles() {
		if (news.size() == 0)return;

		List<String> latestReadArticles = new ArrayList<String>();
		Cursor cursor = mLatestReadArticlesDAO.getLatestReadArticles(new String[]{NewsDbHelper.LATEST_READ_ARTICLES_TITLE}, 10);

		int nameIndex = cursor.getColumnIndex(NewsDbHelper.LATEST_READ_ARTICLES_TITLE);

		if (cursor.moveToFirst()) {
			do {
				latestReadArticles.add(cursor.getString(nameIndex));
			} while (cursor.moveToNext());
		} else {
			return;
		}

		List<RSSItem> items = new ArrayList<RSSItem>();
		List<String> addToCarousel = new ArrayList<String>();
		for (News category : news) {
			for (RSSItem item : category.getContent()) {
				if (isInSet(item.getTitle(), latestReadArticles) &&
						!isInSet(item.getTitle(), addToCarousel)) {
					
					items.add(item);
					addToCarousel.add(item.getTitle());
					Log.d("MainActivityTrackers", item.getTitle());
				}
			}
		}
		
		List<News> newsHack = new ArrayList<News>();
		newsHack.add(new News("Tracked Articles", items));
		
		ListView lv = (ListView)findViewById(R.id.tracked_articles);
		lv.setAdapter(new RowsAdapterDippers(this,
				R.layout.viewpager_main_dippers,
				newsHack, false));
		
		// SHOW TEXT IF NO ARTICLES in viewpager_main_trackers.xml
		findViewById(R.id.no_articles_message).setVisibility(View.GONE);
		
		lv.setVisibility(View.VISIBLE);
	}

	private boolean isInSet(String name, List<String> set) {
		for (String s : set) {
			if (s.equals(name))return true;
		}

		return false;
	}

	public static class ArticlesPagerAdapter extends FragmentStatePagerAdapter {
		private List<News> newsList;

		public ArticlesPagerAdapter(FragmentManager fm, List<News> newsList) {
			super(fm);
			this.newsList = newsList;
		}

		@Override
		public Fragment getItem(int arg0) {
			return TrackersFragment.newInstance(arg0);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return newsList.size();
		}
	}

}
