package com.ucl.news.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import com.ucl.news.api.LoggingNewsAppsData;
import com.ucl.news.api.LoggingReadingScroll;
import com.ucl.news.dao.RunningAppsDAO;
import com.ucl.news.dao.RunningAppsMetaDAO;
import com.ucl.news.main.MainActivity;
import com.ucl.news.utils.AutoLogin;
import com.ucl.news.utils.GetRunningAppsCategory;
import com.ucl.news.utils.WellKnownNewsApps;
import com.ucl.news.utils.GetRunningAppsCategory.AsyncRunningAppsResponse;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.ucl.newsreader.R;

public class NewsAppsService extends Service implements
		ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<Status> {

	private static final String TAG = NewsAppsService.class.getSimpleName();
	private static final int FIVE_SECONDS = 5000;
	private ActivityManager am;
	private GetRunningAppsCategory asyncGetRunningApps;
	private static final String NEWS_MAGAZINES = "News & Magazines";
	private GPSTracker gps;
	private Handler handler;
	private static Timer timer;
	private double lat;
	private double lon;
	private TimerTask tt;

	public String userActivity = "Unknown Activity";
	public String userActivityConfidenceLevel = "0";
	public int maxUserActivityConfidenceLevel = -1;

	protected ActivityDetectionBroadcastReceiver mBroadcastReceiver;
	protected GoogleApiClient mGoogleApiClient;
	private ArrayList<DetectedActivity> mDetectedActivities;


	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Service creating");
		handler = new Handler();


		// Kick off the request to build GoogleApiClient.
		buildGoogleApiClient();

		mGoogleApiClient.connect();

		mBroadcastReceiver = new ActivityDetectionBroadcastReceiver();

		LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
				new IntentFilter(Constants.BROADCAST_ACTION));

		mDetectedActivities = new ArrayList<DetectedActivity>();

//		// Set the confidence level of each monitored activity to zero.
//		for (int i = 0; i < Constants.MONITORED_ACTIVITIES.length; i++) {
//			mDetectedActivities.add(new DetectedActivity(Constants.MONITORED_ACTIVITIES[i], 0));
//		}
//



		if (timer != null) {
			timer.cancel();
		} else {
			timer = new Timer();
		}

		tt = new TimerTask() {
			@Override
			public void run() {
				Log.i(TAG, "running");
				handleCommand();
			}
		};

		timer.scheduleAtFixedRate(tt, 0, FIVE_SECONDS);


	}

	/**
	 * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
	 * ActivityRecognition API.
	 */
	protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(ActivityRecognition.API)
				.build();
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "Service destroying");
		mGoogleApiClient.disconnect();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
		tt.cancel();
		timer.cancel();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		//return START_NOT_STICKY;
		return START_NOT_STICKY;
	}

	private void handleCommand() {

		System.out.println("screen mode: " + getScreenMode());
		System.out.println("isRunning: "
				+ isMyServiceRunning(NewsAppsService.class));
		if (getScreenMode() && isMyServiceRunning(NewsAppsService.class)) {

			am = (ActivityManager) getApplicationContext().getSystemService(
					Activity.ACTIVITY_SERVICE);

			String packageName = am.getRunningTasks(1).get(0).topActivity
					.getPackageName();
			System.out.println("packageName: " + packageName);

			System.out.println("packageName look for this one");

			PackageManager pm = getApplicationContext().getPackageManager();
			ApplicationInfo ai;
			try {
				ai = pm.getApplicationInfo(packageName, 0);
			} catch (final NameNotFoundException e) {
				ai = null;
			}
			final String applicationName = (String) (ai != null ? pm
					.getApplicationLabel(ai) : "(unknown)");

			SimpleDateFormat dateformat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss.SSS");
			String dateTimeStr = dateformat.format(new Date().getTime());

			/**
			 * Check if the app is a news app from the WellKnow list of news
			 * apps
			 */
			System.out.println("check for packageName: " + packageName);
			if (WellKnownNewsApps.getInstance().isNewsAppExist(packageName)) {

				RunningAppsDAO raDAORow = new RunningAppsDAO();

				raDAORow.setUserID(AutoLogin.getUserID(AutoLogin
						.getSettingsFile(getApplicationContext())));
				raDAORow.setUserSession(AutoLogin.getUserSession(AutoLogin
						.getSettingsFile(getApplicationContext())));
				raDAORow.setAppName(applicationName);
				raDAORow.setPackageName(packageName);
				raDAORow.setCategoryName(NEWS_MAGAZINES);
				raDAORow.setStartTime(dateTimeStr);

				Runnable runnable = new Runnable() {
		            @Override
		            public void run() {
		                handler.post(new Runnable() { // This thread runs in the UI
		                    @Override
		                    public void run() {
		                    	// Get Location of accessed news app
		        				gps = new GPSTracker(getApplicationContext());

		        				// check if GPS enabled
		        				if (gps.canGetLocation()) {

		        					lat = gps.getLatitude();
		        					lon = gps.getLongitude();
		        					
		        					System.out.println("Your Location is - \nLat: " + lat
		        							+ "\nLong: " + lon);
		        				} else {
		        					// can't get location
		        					// GPS or Network is not enabled
		        					// Ask user to enable GPS/network in settings
		        					// gps.showSettingsAlert();
		        					System.out.println("can't get location");
		        				}

		                    }
		                });
		            }
		        };
		        new Thread(runnable).start();
		        
		        raDAORow.setLat(lat);
				raDAORow.setLon(lon);

				raDAORow.setUserActivity(userActivity);
				raDAORow.setUserActivityConfidence(userActivityConfidenceLevel);

				if (AutoLogin.getIsLoggedIN(AutoLogin
						.getSettingsFile(getApplicationContext()))) {
					
					System.out.println("HERE PROCESS FINISH 1");
					
					// Commented coz runningNewsApps is stored in the server
					//appendRunningAppsInLocalFile(raDAORow);

					//Store RunningNewsApps and Location in the server
					LoggingNewsAppsData loggingNewsAppsData = new LoggingNewsAppsData(getApplicationContext(), this, raDAORow);


					Log.e("raDaoActivity", raDAORow.getUserActivity() + ", " + raDAORow.getUserActivityConfidence());

					Log.e("herelocation: ", raDAORow.getLat() + ", " + raDAORow.getLon());
//					System.out.println("This app should be logged");
//					System.out.println("finish userID: "
//							+ AutoLogin.getUserID(AutoLogin
//									.getSettingsFile(getApplicationContext())));
//					System.out.println("finish userSession: "
//							+ AutoLogin.getUserSession(AutoLogin
//									.getSettingsFile(getApplicationContext())));
//					System.out.println("finish appName: "
//							+ raDAORow.getAppName());
//					System.out.println("finish packageName: "
//							+ raDAORow.getPackageName());
//					System.out.println("finish categoryName: "
//							+ raDAORow.getCategoryName());
//
//					System.out.println("finish categoryName: "
//							+ raDAORow.getCategoryName());
//					System.out.println("finish startTime: "
//							+ raDAORow.getStartTime());
				}

			} else {
				System.out.println("app not found in the list");
			}

			// asyncGetRunningApps = new GetRunningAppsCategory(
			// getApplicationContext());
			// asyncGetRunningApps.execute(applicationName, packageName,
			// dateTimeStr);
			// asyncGetRunningApps.delegate = NewsAppsService.this;

		} else {
			System.out.println("service not killed but doesn't log");
		}
	}

	private boolean getScreenMode() {
		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		return powerManager.isScreenOn();
	}

//	@Override
//	public void onProcessFinish(RunningAppsMetaDAO ramDAO) {
//		// TODO Auto-generated method stub
//
//		System.out.println("HERE PROCESS FINISH 2");
//		/*
//		 * Discard non News & Magazines categories, log only news apps
//		 */
//		if (ramDAO != null) {
//			if (ramDAO.getCategoryName() != null
//					&& ramDAO.getCategoryName().equals(NEWS_MAGAZINES)) {
//
//				System.out.println("this is news app");
//				RunningAppsDAO raDAORow = new RunningAppsDAO();
//
//				raDAORow.setUserID(AutoLogin.getUserID(AutoLogin
//						.getSettingsFile(getApplicationContext())));
//				raDAORow.setUserSession(AutoLogin.getUserSession(AutoLogin
//						.getSettingsFile(getApplicationContext())));
//				raDAORow.setAppName(ramDAO.getAppName());
//				raDAORow.setPackageName(ramDAO.getPackageName());
//				raDAORow.setCategoryName(ramDAO.getCategoryName());
//				raDAORow.setStartTime(ramDAO.getStartTime());
//
//				// raDAO.add(raDAORow);
//
//				// Get Location of accessed news app
//
//				gps = new GPSTracker(getApplicationContext());
//
//				// check if GPS enabled
//				if (gps.canGetLocation()) {
//
//					double latitude = gps.getLatitude();
//					double longitude = gps.getLongitude();
//					raDAORow.setLat(latitude);
//					raDAORow.setLon(longitude);
//					System.out.println("Your Location is - \nLat: " + latitude
//							+ "\nLong: ");
//					// \n is for new line
//					// Toast.makeText(getApplicationContext(),
//					// "Your Location is - \nLat: " + latitude + "\nLong: " +
//					// longitude, Toast.LENGTH_LONG).show();
//				} else {
//					// can't get location
//					// GPS or Network is not enabled
//					// Ask user to enable GPS/network in settings
//					// gps.showSettingsAlert();
//				}
//
//				if (AutoLogin.getIsLoggedIN(AutoLogin
//						.getSettingsFile(getApplicationContext()))) {
//
//					appendRunningAppsInLocalFile(raDAORow);
//
//					System.out.println("This app should be logged");
//					System.out.println("finish userID: "
//							+ AutoLogin.getUserID(AutoLogin
//									.getSettingsFile(getApplicationContext())));
//					System.out.println("finish userSession: "
//							+ AutoLogin.getUserSession(AutoLogin
//									.getSettingsFile(getApplicationContext())));
//					System.out.println("finish appName: "
//							+ raDAORow.getAppName());
//					System.out.println("finish packageName: "
//							+ raDAORow.getPackageName());
//					System.out.println("finish categoryName: "
//							+ raDAORow.getCategoryName());
//
//					System.out.println("finish categoryName: "
//							+ raDAORow.getCategoryName());
//					System.out.println("finish startTime: "
//							+ raDAORow.getStartTime());
//				}
//
//			}
//		}
//
//	}

	private void appendRunningAppsInLocalFile(RunningAppsDAO raDAO) {

		File newsAppsFile = new File(Environment.getExternalStorageDirectory()
				+ File.separator + "HabitoNews_Study/news_runningApps.txt");
		if (newsAppsFile.exists()) {
			System.out.println("file found");
			try {
				BufferedWriter bW;

				bW = new BufferedWriter(new FileWriter(newsAppsFile, true));

				String delimeter = ";";
				String row = raDAO.getUserID() + delimeter
						+ raDAO.getUserSession() + delimeter
						+ raDAO.getAppName() + delimeter
						+ raDAO.getPackageName() + delimeter
						+ raDAO.getCategoryName() + delimeter + raDAO.getLat()
						+ delimeter + raDAO.getLon() + delimeter
						+ raDAO.getStartTime() + delimeter;

				bW.write(row);
				bW.newLine();
				bW.flush();

				bW.close();
			} catch (Exception e) {

			}
		} else {
			// Do something else.
			System.out.println("news_runningApps file not found");
		}
	}

	private boolean isMyServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets a PendingIntent to be sent for each activity detection.
	 */
	private PendingIntent getActivityDetectionPendingIntent() {
		Intent intent = new Intent(this, DetectedActivitiesIntentService.class);

		// We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
		// requestActivityUpdates() and removeActivityUpdates().
		return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	/**
	 * Retrieves a SharedPreference object used to store or read values in this app. If a
	 * preferences file passed as the first argument to {@link #getSharedPreferences}
	 * does not exist, it is created when {@link SharedPreferences.Editor} is used to commit
	 * data.
	 */
	private SharedPreferences getSharedPreferencesInstance() {
		return getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
	}

	/**
	 * Retrieves the boolean from SharedPreferences that tracks whether we are requesting activity
	 * updates.
	 */
	private boolean getUpdatesRequestedState() {
		return getSharedPreferencesInstance()
				.getBoolean(Constants.ACTIVITY_UPDATES_REQUESTED_KEY, false);
	}

	/**
	 * Sets the boolean in SharedPreferences that tracks whether we are requesting activity
	 * updates.
	 */
	private void setUpdatesRequestedState(boolean requestingUpdates) {
		getSharedPreferencesInstance()
				.edit()
				.putBoolean(Constants.ACTIVITY_UPDATES_REQUESTED_KEY, requestingUpdates)
				.commit();
	}


	/**
	 * Processes the list of freshly detected activities. Asks the adapter to update its list of
	 * DetectedActivities with new {@code DetectedActivity} objects reflecting the latest detected
	 * activities.
	 */
	protected void updateDetectedActivitiesList(ArrayList<DetectedActivity> detectedActivities) {
		//mAdapter.updateActivities(detectedActivities);

		Log.e("hereac: ", detectedActivities.size()+"");
	}

	/**
	 * Runs when a GoogleApiClient object successfully connects.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {


		ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient,Constants.DETECTION_INTERVAL_IN_MILLISECONDS, getActivityDetectionPendingIntent());
//
//		Intent intent = new Intent(this, DetectedActivitiesIntentService.class);
//		intent.setAction(Long.toString(System.currentTimeMillis()));
//		PendingIntent mActivityRecognitionPendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//		ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, Constants.DETECTION_INTERVAL_IN_MILLISECONDS,
//				mActivityRecognitionPendingIntent).setResultCallback(this);
//		//startService(intent);   // this should start the DetectedActivitiesIntentService

		Log.i(TAG, "Connected to GoogleApiClient");
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// Refer to the javadoc for ConnectionResult to see what error codes might be returned in
		// onConnectionFailed.
		Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
		mGoogleApiClient.connect();
	}

	@Override
	public void onConnectionSuspended(int cause) {
		// The connection to Google Play services was lost for some reason. We call connect() to
		// attempt to re-establish the connection.
		Log.i(TAG, "Connection suspended");
		mGoogleApiClient.connect();
	}


	public void onResult(Status status) {
		if (status.isSuccess()) {
			// Toggle the status of activity updates requested, and save in shared preferences.
			boolean requestingUpdates = !getUpdatesRequestedState();
			setUpdatesRequestedState(requestingUpdates);


			Toast.makeText(
					this,
					getString(requestingUpdates ? R.string.activity_updates_added :
							R.string.activity_updates_removed),
					Toast.LENGTH_SHORT
			).show();
		} else {
			Log.e(TAG, "Error adding or removing activity detection: " + status.getStatusMessage());
		}
	}

	/**
	 * Receiver for intents sent by DetectedActivitiesIntentService via a sendBroadcast().
	 * Receives a list of one or more DetectedActivity objects associated with the current state of
	 * the device.
	 */
	public class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {
		protected static final String TAG = "activity-detection-response-receiver";

		@Override
		public void onReceive(Context context, Intent intent) {

			ArrayList<DetectedActivity> updatedActivities =
					intent.getParcelableArrayListExtra(Constants.ACTIVITY_EXTRA);

		//	Toast.makeText(context, updatedActivities.get(0).getType(), Toast.LENGTH_LONG).show();

			Log.e("updateda: ", updatedActivities.size() + "");


			maxUserActivityConfidenceLevel = updatedActivities.get(0).getConfidence();
			userActivity = Constants.getActivityString(getApplicationContext(), updatedActivities.get(0).getType());

			if(updatedActivities.size() > 1){
				for (DetectedActivity activity : updatedActivities) {

					Log.e("activi", activity.getType()+", " + activity.getConfidence());

					if(activity.getConfidence() > maxUserActivityConfidenceLevel){
						Log.e("confidence", activity.getConfidence()+"");

						maxUserActivityConfidenceLevel = activity.getConfidence();
						userActivity = Constants.getActivityString(getApplicationContext(), activity.getType());


					}

					Log.e("activitydetected:", activity.getType() + ", " + activity.getConfidence());
				}
			}

			userActivityConfidenceLevel = String.valueOf(maxUserActivityConfidenceLevel);





			updateDetectedActivitiesList(updatedActivities);
		}
	}
}