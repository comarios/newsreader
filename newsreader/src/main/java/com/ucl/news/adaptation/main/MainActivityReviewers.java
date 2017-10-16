package com.ucl.news.adaptation.main;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import main.java.org.mcsoxford.rss.RSSItem;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ucl.news.adaptation.adapters.BaselineFragment;
import com.ucl.news.adaptation.adapters.BaselineFragment.OnCategoryItemClickListener;
import com.ucl.news.adaptation.adapters.ReviewersFragment;
import com.ucl.news.adaptation.adapters.RowsAdapterDippers;
import com.ucl.news.adaptation.dao.LatestReadArticlesDAO;
import com.ucl.news.dao.NavigationDAO;
import com.ucl.news.dao.Session;
import com.ucl.news.data.NewsDbHelper;
import com.ucl.news.download.BitmapManager;
import com.ucl.news.main.ArticleActivity;
import com.ucl.news.main.LoginActivity;
import com.ucl.news.reader.News;
import com.ucl.news.reader.RSSItems;
import com.ucl.news.reader.RetrieveFeedTask;
import com.ucl.news.reader.RetrieveFeedTask.AsyncResponse;
import com.ucl.news.utils.AutoLogin;
import com.ucl.news.utils.Dialogs;
import com.ucl.news.utils.NetworkConnection;
import com.ucl.news.utils.UriInOut;
import com.ucl.newsreader.R;

import static com.ucl.news.main.MainActivity.PREFS_NAME;

public class MainActivityReviewers extends FragmentActivity 
implements AsyncResponse, OnCategoryItemClickListener {

	private static final String TAG = MainActivityReviewers.class.getSimpleName();
	private ArrayList<News> news;
	private ProgressBar progress;
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
	private ViewPager pager;
	private boolean trackerFlag;
	private boolean dipperFlag;
	private ViewGroup reviewersLayout;
	private Button mClearSearchButton;
	private EditText mSearchBoxEt;
	public final static String EXTRA_MESSAGE = "com.ucl.news.adapters.EXTRA_MESSAGE";
	private MainActivityReviewers.FilteredListAdapter mFilteredAdapter;
	private ListView entriesListView;
	private RowsAdapterDippers rowsAdapter;
	private static Context context;
    private LatestReadArticlesDAO mLatestReadArticlesDAO;
    private List<RSSItem> trackedItems;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

        setContentView(R.layout.reviewer_layout);

        //Check which flag is set (search box || tracked articles || none)
        Intent i = getIntent();
        trackerFlag = i.getExtras().getBoolean("trackerflag");
        dipperFlag = i.getExtras().getBoolean("dipperflag");
        reviewersLayout = (ViewGroup) findViewById(R.id.reviewer_layout);
        context = getApplicationContext();
        mLatestReadArticlesDAO = new LatestReadArticlesDAO(this);

		if (network.haveNetworkConnection()) {

			progress = (ProgressBar) findViewById(R.id.progressBar);

			int resourceID = R.layout.viewpager_main;
			//entriesListView = (ListView) findViewById(R.id.mainVerticalList);
			news = new ArrayList<News>();
			//rowsAdapter = new RowsAdapter(this, resourceID, news, this);
			//entriesListView.setAdapter(rowsAdapter);

            //If trackerFlag set, attach tracked articles view to base layout
            if(trackerFlag){
                RelativeLayout rl = (RelativeLayout)findViewById(R.id.reviewers_lay);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)rl.getLayoutParams();
                params.addRule(RelativeLayout.BELOW, R.id.tracked_articles_placeholder);
                rl.setLayoutParams(params);

                View trackerTop = LayoutInflater.from(this).inflate(R.layout.top_static_tracker, reviewersLayout, false);
                reviewersLayout.addView(trackerTop);
            }

            //If dipperFlag set, attach search box view to base layout
            if(dipperFlag){
                View dipperTop = LayoutInflater.from(this).inflate(R.layout.top_static_dipper, reviewersLayout, false);
                reviewersLayout.addView(dipperTop);
                mClearSearchButton = (Button) findViewById(R.id.clear_search_et);
                mSearchBoxEt = (EditText) findViewById(R.id.searchText);
                mSearchBoxEt.addTextChangedListener(mTextChangeListener);
            }
			// if(!AutoLogin.getIsLoggedIN(AutoLogin.getSettingsFile(getApplicationContext())))
			fetchRSS("*");
			CallingFromArticleActivity = false;

			/*
			 * Get GPS Location
			 */
			/*LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			LocationListener locationListener = new GPSLocation(
					getApplicationContext());
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 5000, 2, locationListener);

			this.newsAppsService = new Intent(NewsAppsService.class.getName());
			this.startService(newsAppsService);*/

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

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_search_et:
                // Clear search box
                mSearchBoxEt.setText("");
                // Close keyboard
                InputMethodManager imm = (InputMethodManager)getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mSearchBoxEt.getWindowToken(), 0);
                //pager.setAdapter(new MainActivityTrackers.ArticlesPagerAdapter(getSupportFragmentManager(), news));
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                BaselineFragment fragment = new BaselineFragment();
                ft.replace(R.id.baseline_container, fragment);
                ft.commit();
                // Refresh articles
                entriesListView.setVisibility(View.GONE);
                //pager.setAdapter(new ArticlesPagerAdapter(getSupportFragmentManager(), news));
                //rowsAdapter.clear();
                findViewById(R.id.header).setVisibility(View.GONE);
                fetchRSS("*");
                break;
            default:
                break;
        }
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

        if(dipperFlag) {
            findViewById(R.id.header).setVisibility(View.VISIBLE);
        }
		if(trackerFlag) {
            updateLatestReadArticles();
        }
		progress.setVisibility(View.GONE);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_sync:
            if(dipperFlag){
                // Clear search box
                mSearchBoxEt.setText("");
                // Close keyboard
                InputMethodManager imm = (InputMethodManager)getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mSearchBoxEt.getWindowToken(), 0);
                // Refresh articles
                //entriesListView.setAdapter(rowsAdapter);
                //rowsAdapter.clear();
                findViewById(R.id.header).setVisibility(View.GONE);
            }
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
        if(trackerFlag) {
            updateLatestReadArticles();
        }
	}

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

        trackedItems = new ArrayList<RSSItem>();
        List<String> addToCarousel = new ArrayList<String>();
        for (News category : news) {
            for (RSSItem item : category.getContent()) {
                if (isInSet(item.getTitle(), latestReadArticles) &&
                        !isInSet(item.getTitle(), addToCarousel)) {

                    trackedItems.add(item);
                    addToCarousel.add(item.getTitle());
                }
            }
        }

        List<News> newsHack = new ArrayList<News>();
        newsHack.add(new News("Tracked Articles", trackedItems));

        ListView lv = (ListView)findViewById(R.id.tracked_articles);
        lv.setAdapter(new RowsAdapterDippers(this,
                R.layout.viewpager_main_dippers,
                newsHack, false));

        findViewById(R.id.no_articles_message).setVisibility(View.GONE);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriInOut())
                .create();
        editor.putBoolean("trackerFlag", trackerFlag);
        editor.putString("trackedArticles", gson.toJson(trackedItems));
        editor.commit();

        lv.setVisibility(View.VISIBLE);
    }

    private boolean isInSet(String name, List<String> set) {
        for (String s : set) {
            if (s.equals(name))return true;
        }

        return false;
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

    private TextWatcher mTextChangeListener = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Nothing to do
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            // Nothing to do
        }

        @Override
        public void afterTextChanged(Editable s) {

            if (s.length() > 0)
                mClearSearchButton.setVisibility(View.VISIBLE);
            else {
                mClearSearchButton.setVisibility(View.GONE);
                //entriesListView.setAdapter(rowsAdapter);
            }

            String[] keywords = s.toString().trim().split(" ");

            List<RSSItem> filteredItems = new ArrayList<RSSItem>();
            for (News category : news) {
                for (RSSItem item : category.getContent()) {
                    if (item.matches(keywords)) {
                        //We make sure that the item is only added once
                        //based on the items title, tho
                        if (!existInSet(item, filteredItems))
                            filteredItems.add(item);
                    }
                }
            }

            entriesListView = (ListView) findViewById(R.id.mainVerticalList);
            mFilteredAdapter = new MainActivityReviewers.FilteredListAdapter(MainActivityReviewers.this, filteredItems);
            entriesListView.setVisibility(View.VISIBLE);
            entriesListView.setAdapter(mFilteredAdapter);
        }
    };

    private boolean existInSet(RSSItem toAdd, List<RSSItem> set) {
        for (RSSItem item : set) {
            if (item.getTitle().equals(toAdd.getTitle()))
                return true;
        }

        return false;
    }

    private static class FilteredListAdapter extends BaseAdapter {

        private Context mContext;
        private List<RSSItem> mItems;
        private ArrayList<String> imgURLs = new ArrayList<String>();

        public FilteredListAdapter(Context context, List<RSSItem> items) {
            mContext = context;
            mItems = items;

            for (int i = 0; i < mItems.size(); i++) {
                if (!mItems.get(i).getThumbnails().isEmpty()) {

                    if (mItems.get(i).getThumbnails().get(0).getUrl().toString() != null) {
                        imgURLs.add(mItems.get(i).getThumbnails().get(0).getUrl()
                                .toString());
                    }
                }
            }
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public RSSItem getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(R.layout.filtered_list_item, parent, false);

                MainActivityReviewers.FilteredListAdapter.ViewHolder viewHolder = new MainActivityReviewers.FilteredListAdapter.ViewHolder();
                viewHolder.iconView = (ImageView) convertView.findViewById(R.id.image);
                viewHolder.topText = (TextView) convertView.findViewById(R.id.title);
                viewHolder.contentPreview = (TextView) convertView.findViewById(R.id.preview_content);

                convertView.setTag(viewHolder);
            }

            MainActivityReviewers.FilteredListAdapter.ViewHolder viewHolder = (MainActivityReviewers.FilteredListAdapter.ViewHolder) convertView.getTag();
            TextView title = viewHolder.topText;
            ImageView image = viewHolder.iconView;
            TextView previewContent = viewHolder.contentPreview;

            final RSSItem item = getItem(position);

            title.setText(item.getTitle());

            if (!item.getThumbnails().isEmpty()) {
                // imageLoader.DisplayImage(rssItems.get(position)
                // .getThumbnails().get(1).getUrl().toString(),
                // holder.iconView);
                BitmapManager.INSTANCE.loadBitmap(imgURLs.get(position),
                        image, 230, 150);
            }

            String desc;
            if (item.getDescription().length() >= 100) {
                desc = item.getDescription().substring(0, 90) + "...";
            } else {
                desc = item.getDescription();
            }
            previewContent.setText(desc);

            // GO TO ARTICLE AFTER SEARCH
            viewHolder.iconView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    // Toast.makeText(context,"Clicked item at: "+ cPos ,
                    // Toast.LENGTH_LONG)
                    // .show();
                    // Set the flag of switching activity


                    MainActivityTrackers.activitySwitchFlag = true;
                    Intent intent = new Intent(context, ArticleActivity.class);

                    // navDAO.setOrderID(orderID)
                    // Toast.makeText(context,"Content: "+
                    // rssItems.get(cPos).getTitle() , Toast.LENGTH_LONG)
                    // .show();
                    RSSItems rss = new RSSItems(item.getTitle(),
                            item.getLink().toString());
                    intent.putExtra(EXTRA_MESSAGE, rss);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
//					intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    context.startActivity(intent);
                }

            });

            return convertView;
        }

        private static class ViewHolder {
            public ImageView iconView;
            public TextView topText;
            public TextView contentPreview;
        }
    }

}
