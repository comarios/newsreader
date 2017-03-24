package com.ucl.news.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import main.java.org.mcsoxford.rss.RSSItem;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.ucl.adaptationmechanism.AdaptInterfaceActivity;
import com.ucl.adaptationmechanism.RuleLoader;
import com.ucl.news.adaptation.main.MainActivityDippers;
import com.ucl.news.adaptation.main.MainActivityReviewers;
import com.ucl.news.adaptation.main.MainActivityTrackers;
import com.ucl.news.adapters.RowsAdapter;
import com.ucl.news.api.NavigationDAO;
import com.ucl.news.api.Session;
import com.ucl.news.reader.News;
import com.ucl.news.reader.RetrieveFeedTask;
import com.ucl.news.reader.RetrieveFeedTask.AsyncResponse;
import com.ucl.news.services.AlarmReceiver;
import com.ucl.news.services.NewsAppsService;
import com.ucl.news.utils.AutoLogin;
import com.ucl.news.utils.Dialogs;
import com.ucl.news.utils.GPSLocation;
import com.ucl.news.utils.NetworkConnection;
import com.ucl.news.utils.SnackbarWrapper;
import com.ucl.newsreader.R;

import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

public class MainActivity extends AppCompatActivity implements AsyncResponse {

    private ArrayList<News> news;
    private ProgressBar progress;
    private Toolbar toolbarMainActivity;
    private CoordinatorLayout coordinatorLayout;
    private RowsAdapter rowsAdapter;
    private ListView entriesListView;
    private RetrieveFeedTask asyncTask;
    private Spinner adaptiveVariantsSpinner;
    private String[] categories = {"Top Stories", "World", "UK", "Business",
            "Sports", "Politics", "Health", "Education & Family",
            "Science & Environment", "Technology", "Entertainment & Arts"};


    private NetworkConnection network = new NetworkConnection(MainActivity.this);
    private Intent logger;
    public static Session userSession = new Session();
    public static ArrayList<NavigationDAO> navigationDAO = new ArrayList<NavigationDAO>();
    public static boolean activitySwitchFlag = false;
    // public static File scrollPositionFile;
    // public static File runningAppsFile;
    // public static File navigationalDataFile;
    private Intent newsAppsService;
    public static boolean CallingFromArticleActivity = false;
    private PendingIntent pendingIntent;
    public static List<String> featureList;
    public static final String PREFS_NAME = "PrefsFile";
    private int lineCounter = 0;
    private List<String> csvContents;
    private boolean adapted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        initViews();

        if (network.haveNetworkConnection()) {

            news = new ArrayList<News>();
            rowsAdapter = new RowsAdapter(this, R.layout.viewpager_main, news, this);
            entriesListView.setAdapter(rowsAdapter);

            // if(!AutoLogin.getIsLoggedIN(AutoLogin.getSettingsFile(getApplicationContext())))
            fetchRSS("*");

            CallingFromArticleActivity = false;

			/*
             * Get GPS Location
			 */

            if (!((LocationManager) getSystemService(Context.LOCATION_SERVICE))
                    .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                //prompt user to enable gps
                turnOnGPS();
            }

            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            LocationListener locationListener = new GPSLocation(
                    getApplicationContext());

            if (Build.VERSION.SDK_INT >= 23 && getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }

            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 5000, 2, locationListener);


//            this.newsAppsService = new Intent(NewsAppsService.class.getName());
//            this.startService(newsAppsService);


            startService(new Intent(this, NewsAppsService.class));

            /***************ADAPTIVE*****************/

            //Test file in assets folder
            csvContents = handleCSV();

            //Timer called every minute that adapts to the next UI (Used for demo)
            //Comment out timer code and leave handleAdaptation(csvContents, lineCounter) to prevent UI changing
            //Comment out whole block for non-adaptive variant
            Timer myTimer = new Timer();
            myTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (lineCounter < csvContents.size()) {
                        handleAdaptation(csvContents, lineCounter);
                        if (adapted) {
                            displayRevertMessage();
                        }
                        lineCounter++;
                        adapted = true;
                    }
                }
            }, 0, 55000);

            //addListenerOnAdaptiveVariants();

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
            noNetworkConnectionError();
        }
    }

    //Once UI changes, user is given the option to revert to the previous interface (Used in demo)
    public void displayRevertMessage() {
        runOnUiThread(new Runnable() {
            public void run() {
                final SnackbarWrapper snackbarWrapper = SnackbarWrapper.make(getApplicationContext(),
                        "Revert to Previous Layout?", 10000);
                snackbarWrapper.setAction("Revert",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                adapted = false;
                                handleAdaptation(csvContents, lineCounter - 2);
                            }
                        });
                snackbarWrapper.show();
            }
        });
    }

    //Reads CSV file and stores contents into a List
    public List<String> handleCSV() {
        List<String> csvContents = new ArrayList<>();
        InputStreamReader is = null;
        try {
            is = new InputStreamReader(getAssets().open("AMTestData.csv"));
            BufferedReader reader = new BufferedReader(is);
            String line;
            while ((line = reader.readLine()) != null) {
                csvContents.add(line.replaceAll("\\p{C}", ""));
            }
        } catch (IOException e) {
            displaySnackbarMessageReload("A problem occurred, please reload the application");
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                displaySnackbarMessageReload("A problem occurred, please reload the application");
            }
        }
        return csvContents;
    }

    //Adapts the UI based on user type percentages
    public void handleAdaptation(List<String> csvContents, int lineCounter) {

        //Load the rules from rules.xml
        RuleLoader rl = new RuleLoader(this);
        List<Double> percentages = new ArrayList<>();

        Double[] typePercentages = parseCSVContent(csvContents, lineCounter);

        double trackerPercent = typePercentages[0];
        double reviewerPercent = typePercentages[1];
        double dipperPercent = typePercentages[2];

        boolean trackerTop = false;
        boolean pushNotifications = false;

        percentages.add(trackerPercent);
        percentages.add(reviewerPercent);
        percentages.add(dipperPercent);

        try {
            List<RuleLoader.Rule> rules = rl.getRules();

            //Compare the rules and stored percentages to find the matching rule
            RuleLoader.Rule r = matchRule(rules, percentages);
            featureList = r.getFeatureList();

            //Check if the rule contains a trackerTop and push notifications
            for (String feature : featureList) {
                Log.v("Percentages", feature);
                if (feature.equals("trackerTop")) {
                    trackerTop = true;
                } else if (feature.equals("pushNotifications")) {
                    pushNotifications = true;
                }
            }

            //If true, set up an alarm every 15 minutes to query news updates
            if (trackerTop && pushNotifications) {
                Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
                pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);

                AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                int interval = 900000; //15 minutes

                manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
            }

            //Using the feature list from the matched rule, start the adaptation
            Intent i = new Intent(MainActivity.this, AdaptInterfaceActivity.class);
            i.putExtra("featureList", r.getFeatureList());
            startActivity(i);
        } catch (IOException | XmlPullParserException e){
            displaySnackbarMessageReload("A problem occurred, please reload the application");
        }
    }

    //Convert the user type percentages to doubles for rule matching
    public Double[] parseCSVContent(List<String> csvContents, int lineCounter) {

        Double[] rowData = new Double[3];

        String[] row = csvContents.get(lineCounter).split(",");
        for (int i = 0; i < row.length; i++) {
            rowData[i] = Double.parseDouble(row[i]) * 100;
        }
        return convertToRuleFormat(rowData);
    }

    //Converts the user type percentages into a format that the rules in rules.xml can understand
    public Double[] convertToRuleFormat(Double[] rowData) {

        double trackerPercent = rowData[0];
        double reviewerPercent = rowData[1];
        double dipperPercent = rowData[2];

        //If over 70%, then assign pure types otherwise mix between reviewer and (tracker/dipper)
        if (trackerPercent >= 70.0) {
            rowData[0] = 100.0;
            rowData[1] = 0.0;
            rowData[2] = 0.0;
        } else if (reviewerPercent >= 70.0) {
            rowData[0] = 0.0;
            rowData[1] = 100.0;
            rowData[2] = 0.0;
        } else if (dipperPercent >= 70.0) {
            rowData[0] = 0.0;
            rowData[1] = 0.0;
            rowData[2] = 100.0;
        } else {
            rowData[1] = 50.0;
            if (trackerPercent > dipperPercent) {
                rowData[0] = 50.0;
                rowData[2] = 0.0;
            } else {
                rowData[2] = 50.0;
                rowData[0] = 0.0;
            }
        }
        return rowData;
    }

    //Matches the converted percentages with the correct rule and returns it
    public static RuleLoader.Rule matchRule(List<RuleLoader.Rule> rules, List<Double> percentages) {
        boolean match = false;

        //Iterate through each rule
        for (RuleLoader.Rule r : rules) {
            List<Double> ranges = r.getRanges();
            //Check if every percentage falls within the rule percentages
            for (int i = 0; i < 3; i++) {
                if (percentages.get(i).equals(ranges.get(i))) {
                    match = true;
                } else {
                    match = false;
                    break;
                }
            }
            if (match) {
                return r;
            }
        }
        return null;
    }

    private void turnOnGPS() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);

        // set title
        alertDialogBuilder.setTitle("Enable Location Services");

        // set dialog message
        alertDialogBuilder
                .setMessage("Allow Habito News to access your location. The app requires to grant location services permission.")
                .setCancelable(true)
                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                        startActivity(new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("Don't Allow", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void noNetworkConnectionError() {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "No internet connection!", Snackbar.LENGTH_LONG)
                .setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                    }
                });

        snackbar.setActionTextColor(Color.RED);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    //Snackbar prompting user to reload the app if a major error occurs
    private void displaySnackbarMessageReload(String message) {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, message, Snackbar.LENGTH_LONG)
                .setAction("Reload", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                });

        snackbar.setActionTextColor(Color.RED);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    private void initViews() {
        /*toolbarMainActivity = (Toolbar) findViewById(R.id.toolbarMainActivity);
//        toolbar.setContentInsetsAbsolute(0,0);
//        toolbar.setPadding(0,0,0,0);
        setSupportActionBar(toolbarMainActivity);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");*/

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id
                .coordinatorLayout);
        progress = (ProgressBar) findViewById(R.id.progressBar);
        entriesListView = (ListView) findViewById(R.id.mainVerticalList);
    }

//    public void addListenerOnAdaptiveVariants() {
//
//        adaptiveVariantsSpinner = (Spinner) findViewById(R.id.adaptiveVariantSpinner);
//
//        adaptiveVariantsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
//
//            @Override
//            public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
//                                       int position, long id) {
//                // TODO Auto-generated method stub
//                String variant = String.valueOf(parentView.getItemAtPosition(position).toString());
////
//                System.out.println("variant: " + variant + " position: " + position);
//
//                if (variant.equals("Trackers")) {
//                    Intent ia = new Intent(getApplicationContext(), MainActivityTrackers.class);
//                    ia.putExtra("ref", "WelcomeScreenCaller");
//                    startActivity(ia);
//                } else if (variant.equals("Reviewers")) {
//                    Intent ib = new Intent(getApplicationContext(), MainActivityReviewers.class);
//                    ib.putExtra("ref", "WelcomeScreenCaller");
//                    startActivity(ib);
//                } else if (variant.equals("Dippers")) {
//                    Intent ic = new Intent(getApplicationContext(), MainActivityDippers.class);
//                    ic.putExtra("ref", "WelcomeScreenCaller");
//                    startActivity(ic);
//                }
////				switch (position) {
////				case 1:
////					Intent ia = new Intent(getApplicationContext(), MainActivityTrackers.class);
////					ia.putExtra("ref", "WelcomeScreenCaller");
////					startActivity(ia);
////					break;
////				case 2:
////					Intent ib = new Intent(getApplicationContext(), MainActivityReviewers.class);
////					ib.putExtra("ref", "WelcomeScreenCaller");
////					startActivity(ib);
////					break;
////				case 3:
////					Intent ic = new Intent(getApplicationContext(), MainActivityDippers.class);
////					ic.putExtra("ref", "WelcomeScreenCaller");
////					startActivity(ic);
////					break;
////				default:
////					Intent ibaseline = new Intent(getApplicationContext(), MainActivity.class);
////					ibaseline.putExtra("ref", "WelcomeScreenCaller");
////					startActivity(ibaseline);
////					break;
////				}
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parentView) {
//                // TODO Auto-generated method stub
//            }
//        });
//    }

    public void fetchRSS(String searchKey) {
        progress.setVisibility(View.VISIBLE);
        asyncTask = new RetrieveFeedTask(getApplicationContext(), searchKey);
        asyncTask.execute("http://feeds.bbci.co.uk/news/rss.xml",
                "http://feeds.bbci.co.uk/news/world/rss.xml",
                "http://feeds.bbci.co.uk/news/uk/rss.xml",
                "http://feeds.bbci.co.uk/news/business/rss.xml",
//                "http://feeds.bbci.co.uk/sport/0/rss.xml",
                "http://feeds.bbci.co.uk/sport/football/rss.xml",
                "http://feeds.bbci.co.uk/news/politics/rss.xml",
                "http://feeds.bbci.co.uk/news/health/rss.xml",
                "http://feeds.bbci.co.uk/news/education/rss.xml",
                "http://feeds.bbci.co.uk/news/science_and_environment/rss.xml",
                "http://feeds.bbci.co.uk/news/technology/rss.xml",
                "http://feeds.bbci.co.uk/news/entertainment_and_arts/rss.xml");
        asyncTask.delegate = MainActivity.this;
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
                rowsAdapter.clear();
                fetchRSS("*");
                return true;
            case R.id.action_logout:
                logout();
                return true;
            case R.id.action_Trackers:
                Intent ia = new Intent(getApplicationContext(), MainActivityTrackers.class);
                ia.putExtra("ref", "WelcomeScreenCaller");
                startActivity(ia);
                return true;
            case R.id.action_Reviewers:
                Intent ib = new Intent(getApplicationContext(), MainActivityReviewers.class);
                ib.putExtra("ref", "WelcomeScreenCaller");
                startActivity(ib);
                return true;
            case R.id.action_Dippers:
                Intent ic = new Intent(getApplicationContext(), MainActivityDippers.class);
                ic.putExtra("ref", "WelcomeScreenCaller");
                startActivity(ic);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void processFinish(ArrayList<List<RSSItem>> outputFeed) {
        // TODO Auto-generated method stub

        // start tracing to "/sdcard/calc.trace"
        // Debug.startMethodTracing("calc");

        // Initial stage
        if (news.size() != categories.length) {
            for (int i = 0; i < outputFeed.size(); i++) {
                news.add(new News(categories[i], outputFeed.get(i)));
                // for (RSSItem item : outputFeed.get(i)) {
                // Log.e("RSS in list :", "" + i + ", " + item.getTitle());
                // }
                rowsAdapter.notifyDataSetChanged();
            }
        } else {
            for (int j = 0; j < news.size(); j++) {
                news.get(j).setContent(outputFeed.get(j));
            }
        }
        // stop tracing
        // Debug.stopMethodTracing();

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
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
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
    }
}
