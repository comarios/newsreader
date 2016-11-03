package com.ucl.news.adaptation.adapters;

import java.util.ArrayList;
import java.util.List;

import main.java.org.mcsoxford.rss.RSSItem;

import com.ucl.news.adaptation.main.MainActivityReviewers;
import com.ucl.news.adapters.RowsAdapter;
import com.ucl.news.reader.News;
import com.ucl.news.reader.RetrieveFeedTask;
import com.ucl.news.reader.RetrieveFeedTask.AsyncResponse;
import com.ucl.newsreader.R;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

public class BaselineFragment extends Fragment {
	private OnCategoryItemClickListener mItemClickListener;
	
	private RetrieveFeedTask asyncTask;
	private String[] categories = { "Top Stories", "World", "UK", "Business",
			"Sports", "Politics", "Health", "Education & Family",
			"Science & Environment", "Technology", "Entertainment & Arts" };
	private ArrayList<News> news;
	private RowsAdapter rowsAdapter;
	private ListView entriesListView;
	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			news = ((MainActivityReviewers)activity).getNews();
			mItemClickListener = (OnCategoryItemClickListener)activity;
			
			//AsyncResponse r = (AsyncResponse)activity;
		} catch (ClassCastException ex) {
			Log.e(BaselineFragment.class.getSimpleName(), 
					activity.getClass().getSimpleName() +
					" must implement " +
		OnCategoryItemClickListener.class.getSimpleName());
		}
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.activity_main, parent, false);
		
		entriesListView = (ListView)layout.findViewById(R.id.mainVerticalList);		
		rowsAdapter = new RowsAdapter(getActivity(), R.layout.viewpager_main, news, mItemClickListener);
		entriesListView.setAdapter(rowsAdapter);
		
		return layout;
	}

	public interface OnCategoryItemClickListener {
		public void onItemClick(int index);
	}
}
