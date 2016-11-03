package com.ucl.news.adaptation.main;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import main.java.org.mcsoxford.rss.RSSItem;
import android.content.Context;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.ucl.news.adaptation.adapters.BaselineFragment;
import com.ucl.news.adaptation.adapters.BaselineFragment.OnCategoryItemClickListener;
import com.ucl.news.adaptation.adapters.ReviewersFragment;
import com.ucl.news.adapters.RowsAdapter;
import com.ucl.news.api.NavigationDAO;
import com.ucl.news.api.Session;
import com.ucl.news.main.LoginActivity;
import com.ucl.news.reader.News;
import com.ucl.news.reader.RetrieveFeedTask;
import com.ucl.news.reader.RetrieveFeedTask.AsyncResponse;
import com.ucl.news.services.NewsAppsService;
import com.ucl.news.utils.AutoLogin;
import com.ucl.news.utils.Dialogs;
import com.ucl.news.utils.GPSLocation;
import com.ucl.news.utils.NetworkConnection;
import com.ucl.newsreader.R;

public class MainActivityReviewers extends FragmentActivity 
implements AsyncResponse, OnCategoryItemClickListener {

	private static final String TAG = MainActivityReviewers.class.getSimpleName();
	private ArrayList<News> news;
	private ProgressBar progress;
	private RowsAdapter rowsAdapter;
	private ListView entriesListView;
	private RetrieveFeedTask asyncTask;
	private String[] categories = { "Top Stories", "World", "UK", "Business",
			"Sports", "Politics", "Health", "Education & Family",
			"Science & Environment", "Technology", "Entertainment & Arts" };
	private NetworkConnection network = new NetworkConnection(MainActivityReviewers.this);
	private Intent logger;
	public static Session userSession = new Session();
	public static ArrayList<NavigationDAO> navigationDAO = new ArrayList<NavigationDAO>();
	public static boolean activitySwitchFlag = false;
	// public static File scrollPositionFile;
	// public static File runningAppsFile;
	// public static File navigationalDataFile;
	private Intent newsAppsService;
	public static boolean CallingFromArticleActivity = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		Log.d(TAG, "MainActivityReviews");
		setContentView(R.layout.activity_main_reviewers);

		if (network.haveNetworkConnection()) {

			progress = (ProgressBar) findViewById(R.id.progressBar);

			int resourceID = R.layout.viewpager_main;
			//entriesListView = (ListView) findViewById(R.id.mainVerticalList);
			news = new ArrayList<News>();
			//rowsAdapter = new RowsAdapter(this, resourceID, news, this);
			//entriesListView.setAdapter(rowsAdapter);

			// if(!AutoLogin.getIsLoggedIN(AutoLogin.getSettingsFile(getApplicationContext())))
			fetchRSS("*");
			CallingFromArticleActivity = false;

			/*
			 * Get GPS Location
			 */
			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			LocationListener locationListener = new GPSLocation(
					getApplicationContext());
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 5000, 2, locationListener);

			this.newsAppsService = new Intent(NewsAppsService.class.getName());
			this.startService(newsAppsService);

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
			new Dialogs().createDialogINTERNET(MainActivityReviewers.this,
					getApplicationContext());
		}

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
		asyncTask.delegate = MainActivityReviewers.this;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);

		return super.onCreateOptionsMenu(menu);
	}

	

	@Override
	public void processFinish(ArrayList<List<RSSItem>> outputFeed) {
		// TODO Auto-generated method stub

		// Initial stage
		if (news.size() != categories.length) {
			for (int i = 0; i < outputFeed.size(); i++) {
				news.add(new News(categories[i], outputFeed.get(i)));
				// for (RSSItem item : outputFeed.get(i)) {
				// Log.e("RSS in list :", "" + i + ", " + item.getTitle());
				// }
				//rowsAdapter.notifyDataSetChanged();
			}
			
			// Create fragments
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			
			BaselineFragment fragment = new BaselineFragment();
			ft.replace(R.id.baseline_container, fragment);
			
			ft.commit();
		} else {
			for (int j = 0; j < news.size(); j++) {
				news.get(j).setContent(outputFeed.get(j));
			}
		}

		progress.setVisibility(View.GONE);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_sync:
			// Refresh articles
			fetchRSS("*");
			return true;
		case R.id.action_logout:
			logout();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
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
		Intent i = new Intent(MainActivityReviewers.this, LoginActivity.class);
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
		//Removed this to add proper back navigation between layout "states"s
		/*Intent setIntent = new Intent(Intent.ACTION_MAIN);
		setIntent.addCategory(Intent.CATEGORY_HOME);
		setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(setIntent);*/
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
	}

	@Override
	public void onItemClick(int index) {
		//Called when the user taps the category name
		//We remove the baseline fragment and add the trackers fragment
 
		FragmentManager fm = getSupportFragmentManager();
		
		Fragment baseLineFragment = fm.findFragmentById(R.id.baseline_container);
		FragmentTransaction ft = fm.beginTransaction();
		
		
		ft.remove(baseLineFragment);
		ft.replace(R.id.trackers_container, ReviewersFragment.newInstance(index));
		ft.addToBackStack(null);
		ft.commit();
	}

	public ArrayList<News> getNews() {
		return news;
	}
	
	public News getNewsPage(int index) {
		return news.get(index);
	}

}
