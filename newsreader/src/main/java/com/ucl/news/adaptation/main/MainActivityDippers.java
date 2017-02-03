package com.ucl.news.adaptation.main;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import main.java.org.mcsoxford.rss.RSSItem;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
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
import com.ucl.news.adaptation.adapters.RowsAdapterDippers;
import com.ucl.news.adaptation.dao.LatestReadArticlesDAO;
import com.ucl.news.api.NavigationDAO;
import com.ucl.news.api.Session;
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

public class MainActivityDippers extends Activity implements AsyncResponse {

	private static final String TAG = MainActivityDippers.class.getSimpleName();
	private ArrayList<News> news;
	private ProgressBar progress;
	public final static String EXTRA_MESSAGE = "com.ucl.news.adapters.EXTRA_MESSAGE";
	private FilteredListAdapter mFilteredAdapter;
	private RowsAdapterDippers rowsAdapter;
	private static MainActivityDippers activity;
	private static Context context;
	private Button mClearSearchButton;
	private EditText mSearchBoxEt;
	private ListView entriesListView;
	private RetrieveFeedTask asyncTask;
	private String[] categories = { "Top Stories", "World", "UK", "Business",
			"Sports", "Politics", "Health", "Education & Family",
			"Science & Environment", "Technology", "Entertainment & Arts" };
	private NetworkConnection network = new NetworkConnection(MainActivityDippers.this);
	private Intent logger;
	public static Session userSession = new Session();
	public static ArrayList<NavigationDAO> navigationDAO = new ArrayList<NavigationDAO>();
	public static boolean activitySwitchFlag = false;
	// public static File scrollPositionFile;
	// public static File runningAppsFile;
	// public static File navigationalDataFile;
	private Intent newsAppsService;
	private boolean trackerFlag;
	private boolean dipperFlag;
	private ViewGroup dippersLayout;
    private LatestReadArticlesDAO mLatestReadArticlesDAO;
    private List<RSSItem> trackedItems;
	
	public static boolean CallingFromArticleActivity = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.dipper_layout);

		//Check which flag is set (search box || tracked articles || none)
        Intent i = getIntent();
        trackerFlag = i.getExtras().getBoolean("trackerflag");
        dipperFlag = i.getExtras().getBoolean("dipperflag");
        dippersLayout = (ViewGroup) findViewById(R.id.dipper_layout);
        mLatestReadArticlesDAO = new LatestReadArticlesDAO(this);

        if (network.haveNetworkConnection()) {

            //If trackerFlag set, attach tracked articles view to base layout
            if (trackerFlag) {
                RelativeLayout rl = (RelativeLayout) findViewById(R.id.dippers_lay);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) rl.getLayoutParams();
                params.addRule(RelativeLayout.BELOW, R.id.tracked_articles_placeholder);
                rl.setLayoutParams(params);

                View trackerTop = LayoutInflater.from(this).inflate(R.layout.top_static_tracker, dippersLayout, false);
                dippersLayout.addView(trackerTop);
            }

            //If dipperFlag set, attach search box view to base layout
            if (dipperFlag) {
                RelativeLayout rl = (RelativeLayout) findViewById(R.id.content);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) rl.getLayoutParams();
                params.topMargin = 175;
                rl.setLayoutParams(params);
                View dipperTop = LayoutInflater.from(this).inflate(R.layout.top_static_dipper, dippersLayout, false);
                dippersLayout.addView(dipperTop);
            }

            progress = (ProgressBar) findViewById(R.id.progressBar);

            context = getApplicationContext();
            int resourceID = R.layout.viewpager_main_dippers;
            entriesListView = (ListView) findViewById(R.id.mainVerticalList);
            news = new ArrayList<News>();
            rowsAdapter = new RowsAdapterDippers(this, resourceID, news, this);
            entriesListView.setAdapter(rowsAdapter);

			/* Initialise search button */
            if (dipperFlag) {
                mClearSearchButton = (Button) findViewById(R.id.clear_search_et);
                mSearchBoxEt = (EditText) findViewById(R.id.searchText);
                mSearchBoxEt.addTextChangedListener(mTextChangeListener);
            }
            // if(!AutoLogin.getIsLoggedIN(AutoLogin.getSettingsFile(getApplicationContext())))
            fetchRSS("*");
            CallingFromArticleActivity = false;
        } else {
            new Dialogs().createDialogINTERNET(MainActivityDippers.this,
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
		asyncTask.delegate = MainActivityDippers.this;
	}

	/* Clear search and refresh articles on cancel search click */
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.clear_search_et:
			// Clear search box
			mSearchBoxEt.setText("");
			// Close keyboard 
			InputMethodManager imm = (InputMethodManager)getSystemService(
			Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mSearchBoxEt.getWindowToken(), 0);
			// Refresh articles
			entriesListView.setAdapter(rowsAdapter);
			rowsAdapter.clear();
			findViewById(R.id.header).setVisibility(View.GONE);
			fetchRSS("*");
			break;
		default:
			break;
		}
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
			// Clear search box
			mSearchBoxEt.setText("");
			// Close keyboard
			InputMethodManager imm = (InputMethodManager)getSystemService(
			Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mSearchBoxEt.getWindowToken(), 0);
			// Refresh articles
			entriesListView.setAdapter(rowsAdapter);
			rowsAdapter.clear();
			findViewById(R.id.header).setVisibility(View.GONE);
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

        if(dipperFlag) {
            findViewById(R.id.header).setVisibility(View.VISIBLE);
        }
        if(trackerFlag) updateLatestReadArticles();
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
		Intent i = new Intent(MainActivityDippers.this, LoginActivity.class);
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

        if(trackerFlag) updateLatestReadArticles();
	}

	//this is where I should handle the articles that have been added to the tracker segment
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

        // SHOW TEXT IF NO ARTICLES in viewpager_main_trackers.xml
        findViewById(R.id.no_articles_message).setVisibility(View.GONE);

        lv.setVisibility(View.VISIBLE);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriInOut())
                .create();
        editor.putBoolean("trackerFlag", trackerFlag);
        editor.putString("trackedArticles", gson.toJson(trackedItems));
        editor.commit();
    }

    private boolean isInSet(String name, List<String> set) {
        for (String s : set) {
            if (s.equals(name))return true;
        }

        return false;
    }

	//Text Watcher to filter the news as the user types in
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
				entriesListView.setAdapter(rowsAdapter);
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
			
			mFilteredAdapter = new FilteredListAdapter(MainActivityDippers.this, filteredItems);
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

    public String getCategory(RSSItem item){
        String link = item.getLink().toString();
        int index = link.indexOf("news");
        StringBuilder category = new StringBuilder();

        for (int i = index; i < link.length(); i++) {
            if (link.charAt(i) != '-') {
                category.append(link.charAt(i));
            } else{
                break;
            }
        }

        return category.toString();
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
				
				ViewHolder viewHolder = new ViewHolder();
				viewHolder.iconView = (ImageView) convertView.findViewById(R.id.image);
				viewHolder.topText = (TextView) convertView.findViewById(R.id.title);
				viewHolder.contentPreview = (TextView) convertView.findViewById(R.id.preview_content);
				
				convertView.setTag(viewHolder);
			}
			
			ViewHolder viewHolder = (ViewHolder) convertView.getTag();
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
			viewHolder.iconView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					// Toast.makeText(context,"Clicked item at: "+ cPos ,
					// Toast.LENGTH_LONG)
					// .show();

					
					
					
					// Set the flag of switching activity
					
					
					MainActivityDippers.activitySwitchFlag = true;
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

    /*public static class ArticlesPagerAdapter extends FragmentStatePagerAdapter {
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
    }*/


}
