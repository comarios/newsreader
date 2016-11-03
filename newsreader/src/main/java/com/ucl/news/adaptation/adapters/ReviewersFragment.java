package com.ucl.news.adaptation.adapters;

import main.java.org.mcsoxford.rss.MediaThumbnail;
import main.java.org.mcsoxford.rss.RSSItem;

import com.ucl.news.adaptation.main.MainActivityReviewers;
import com.ucl.news.adaptation.main.MainActivityTrackers;
import com.ucl.news.api.LoggingNavigationBehavior;
import com.ucl.news.api.NavigationDAO;
import com.ucl.news.download.BitmapManager;
import com.ucl.news.main.ArticleActivity;
import com.ucl.news.reader.News;
import com.ucl.news.reader.RSSItems;
import com.ucl.news.utils.AutoLogin;
import com.ucl.newsreader.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class ReviewersFragment extends Fragment {
	
	public final static String EXTRA_MESSAGE = "com.ucl.news.adapters.EXTRA_MESSAGE";
	public static String EXTRA_PAGE_INDX = "index_extra";
	public static Fragment newInstance(int pageIndex) {
		Fragment f = new ReviewersFragment();
		
		Bundle args = new Bundle();
		args.putInt(EXTRA_PAGE_INDX, pageIndex);
		
		f.setArguments(args);
		
		return f;
	}
	
	private static class ViewHolder {
		public ImageView iconView;
		// public TextView nameTextView;
		public TextView bottomText;
	}
	
	private TextView categoryTextView;
	private MainActivityReviewers activity;
	private News news;
	
	@Override
	public void onAttach(Activity act) {
		super.onAttach(act);
		try {
			activity = (MainActivityReviewers)act;
			int indx = getArguments().getInt(EXTRA_PAGE_INDX);
			news = activity.getNewsPage(indx);
		} catch (ClassCastException e) {
			//Log.e(tag, msg)
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.activity_main_trackers, container, false);
		GridView gridLayout = (GridView)layout.findViewById(R.id.articles_grid);
		gridLayout.setAdapter(new ArticlesGridAdapter(news));
		
		categoryTextView = (TextView)layout.findViewById(R.id.category);
		categoryTextView.setText(news.getTitle());
		
//		ImageView arrowDown = new ImageView(getActivity().getApplicationContext()); 
//		arrowDown.setImageDrawable(getActivity().getApplicationContext().getResources().getDrawable(R.drawable.ic_arrow_down));
//		 
		
		categoryTextView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				FragmentManager fm = getFragmentManager();
				fm.popBackStack();
			}
		});
		return layout;
	}
	
	private class ArticlesGridAdapter extends BaseAdapter {
		private News news;
		public ArticlesGridAdapter(News news) {
			this.news  = news;
		}

		@Override
		public int getCount() {
			
			return news.getContent().size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return news.getContent().get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(getActivity());
				convertView = inflater.inflate(R.layout.viewpager_item, parent, false);
			}
			final RSSItem item = news.getContent().get(position);
			
			//Declare title and image
			TextView textView = (TextView)convertView.findViewById(R.id.bottomText);
			ImageView iconView = (ImageView) convertView.findViewById(R.id.image);
			
			//Declare holder
			ViewHolder holder;
			holder = new ViewHolder();
			holder.iconView = iconView;
			holder.bottomText = textView;
			
			//Set title and image
			textView.setText(item.getTitle());
			if (!item.getThumbnails().isEmpty()) {
				BitmapManager.INSTANCE.loadBitmap(item.getThumbnails().get(0).getUrl().toString(),
						iconView, 230, 150);
			}
			
			holder.iconView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					// Toast.makeText(context,"Clicked item at: "+ cPos ,
					// Toast.LENGTH_LONG)
					// .show();

					// Set the flag of switching activity
					MainActivityReviewers.activitySwitchFlag = true;
					Intent intent = new Intent(getActivity(), ArticleActivity.class);

					// navDAO.setOrderID(orderID)
					// Toast.makeText(context,"Content: "+
					// rssItems.get(cPos).getTitle() , Toast.LENGTH_LONG)
					// .show();
					RSSItems rss = new RSSItems(item.getTitle(),
							item.getLink().toString());
					intent.putExtra(EXTRA_MESSAGE, rss);
					
					//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
//					intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					startActivity(intent);
				}

			});
			
			
			return convertView;
		}
		
	}
}


