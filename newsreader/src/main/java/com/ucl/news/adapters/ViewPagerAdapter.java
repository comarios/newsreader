package com.ucl.news.adapters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import main.java.org.mcsoxford.rss.RSSItem;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ucl.news.adaptation.dao.LatestReadArticlesDAO;
import com.ucl.news.api.LoggingNavigationBehavior;
import com.ucl.news.dao.NavigationDAO;
import com.ucl.news.download.BitmapManager;
import com.ucl.news.main.ArticleActivity;
import com.ucl.news.main.MainActivity;
import com.ucl.news.reader.RSSItems;
import com.ucl.news.utils.AutoLogin;
import com.ucl.newsreader.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ViewPagerAdapter extends PagerAdapter {

    // Declare Variables
    Context context;
    List<RSSItem> rssItems;
    int resource;
    LayoutInflater inflater;
    private View itemsView;
    private String viewPagerCategoryID;
    private final String noImage = "http://vignette2.wikia.nocookie.net/legendmarielu/images/b/b4/No_image_available.jpg/revision/latest?cb=20130511180903";
    // public ImageLoader imageLoader;
    private ArrayList<String> imgURLs = new ArrayList<String>();
    private ArrayList<String> imageURLs = new ArrayList<String>();


    public final static String EXTRA_MESSAGE = "com.ucl.news.adapters.EXTRA_MESSAGE";

    private static class ViewHolder {
        public ImageView iconView;
        // public TextView nameTextView;
        public TextView bottomText;
    }

    public ViewPagerAdapter(Context context, List<RSSItem> _rssItems,
                            String _viewPagerCategoryID) {
        this.context = context;
        this.rssItems = _rssItems;
        this.viewPagerCategoryID = _viewPagerCategoryID;


       // new loadImageURIs().execute(rssItems);

        for (int i = 0; i < rssItems.size(); i++) {
           // Log.e("rssLink", "i : (" + i + "): " + rssItems.get(i).getLink() + "");

            //new ParseHTML().execute(rssItems.get(i).getLink() + "");

            if (!rssItems.get(i).getThumbnails().isEmpty()) {

                // System.out.println("LINK: " +
                // rssItems.get(i).getThumbnails().get(0).getUrl().toString());
                // System.out.println("LINK2: " +
                // rssItems.get(i).getThumbnails());
                if (rssItems.get(i).getThumbnails().get(0).getUrl().toString() != null) {
                    imgURLs.add(rssItems.get(i).getThumbnails().get(0).getUrl()
                            .toString());
                }
            } else {
                imgURLs.add(noImage);
            }
        }

//        for (int i = 0; i < imgURLs.size(); i++) {
//            System.out.println("url (" + i + "):" + imgURLs.get(i));
//        }

        // imageLoader=new ImageLoader(context);

        BitmapManager.INSTANCE.setPlaceholder(BitmapFactory.decodeResource(
                context.getResources(), R.drawable.article));
    }

    public String getViewPagerCategoryID() {
        return viewPagerCategoryID;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        // Declare Variables

        Log.e("POSITION", "POS" + position);
        ViewHolder holder;

        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.viewpager_item, container,
                false);
        this.itemsView = itemView;

        ImageView iconView = (ImageView) itemView.findViewById(R.id.image);
        TextView textView = (TextView) itemView.findViewById(R.id.bottomText);
        holder = new ViewHolder();
        holder.iconView = iconView;
        holder.bottomText = textView;

        // System.out.println("RSSITEMS: " + rssItems.size());

        System.out.println("position: " + position + ", imgurl " + imgURLs.get(position));
        BitmapManager.INSTANCE.loadBitmap(imgURLs.get(position),
                holder.iconView, 210, 150);

//        if (!rssItems.get(position).getThumbnails().isEmpty()) {
//            // imageLoader.DisplayImage(rssItems.get(position)
//            // .getThumbnails().get(1).getUrl().toString(),
//            // holder.iconView);
//            Log.e("Position err", Integer.toString(position));
//            //System.out.println("position: " + position + ", imgurl " + imgURLs.get(position));
//
//            BitmapManager.INSTANCE.loadBitmap(imgURLs.get(position),
//                    holder.iconView, 210, 150);
//        }
////		else{
////			return itemView;
////		}
//        else {
//            BitmapManager.INSTANCE.loadBitmap("http://vignette2.wikia.nocookie.net/legendmarielu/images/b/b4/No_image_available.jpg/revision/latest?cb=20130511180903",
//                    holder.iconView, 210, 150);
//        }

        // for (int i = 0; i < rssItems.size(); i++) {
        // TextView text = new TextView(context);

        // text.setLayoutParams(new RelativeLayout.LayoutParams(
        // LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        // text.setPadding(0, 30, 0, 0);

        // text.setText(rssItems.get(i).getTitle());
        // List<MediaThumbnail> media =
        // rssItems.get(position).getThumbnails();

        // new
        // GetData().execute(rssItems.get(position).getThumbnails().get(1)
        // .getUrl().toString());

        // Check whether a thumbnail exists or not. If it doesn't exist show
        // no image icon.
        // if (!rssItems.get(position).getThumbnails().isEmpty()) {
        // // imageLoader.DisplayImage(rssItems.get(position)
        // // .getThumbnails().get(1).getUrl().toString(),
        // // holder.iconView);
        // BitmapManager.INSTANCE.loadBitmap(rssItems.get(position)
        // .getThumbnails().get(1).getUrl().toString(),
        // holder.iconView, 150, 150);
        // }

        // Log.e("Media",
        // rssItems.get(position).getThumbnails().toString());
        holder.bottomText.setText(rssItems.get(position).getTitle());
        final int cPos = position;
        // text.setText(rssItems.get(position).getTitle());

        holder.iconView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // Toast.makeText(context,"Clicked item at: "+ cPos ,
                // Toast.LENGTH_LONG)
                // .show();

                // Set the flag of switching activity
                MainActivity.activitySwitchFlag = true;
                Intent intent = new Intent(context, ArticleActivity.class);

                LatestReadArticlesDAO lrad = new LatestReadArticlesDAO(context);

                lrad.insertOrUpdateLatestReadArticle(rssItems.get(cPos).getTitle(), System.currentTimeMillis());

                NavigationDAO navDAO = new NavigationDAO();
                navDAO.setUserID(AutoLogin.getUserID(AutoLogin
                        .getSettingsFile(context)));
                navDAO.setUserSession(AutoLogin.getUserSession(AutoLogin
                        .getSettingsFile(context)));

                navDAO.setCategoryName(viewPagerCategoryID);
                navDAO.setPosition(cPos + 1);
                navDAO.setOrderID(MainActivity.navigationDAO.size() + 1);

                LoggingNavigationBehavior logNavigationhpt = new LoggingNavigationBehavior(
                        context, navDAO);
                // MainActivity.navigationDAO.add(navDAO);

                // navDAO.setOrderID(orderID)
                // Toast.makeText(context,"Content: "+
                // rssItems.get(cPos).getTitle() , Toast.LENGTH_LONG)
                // .show();
                RSSItems rss = new RSSItems(rssItems.get(cPos).getTitle(),
                        rssItems.get(cPos).getLink().toString());
                intent.putExtra(EXTRA_MESSAGE, rss);

                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
//				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                context.startActivity(intent);
            }

        });
        // List<MediaThumbnail> media =
        // rssItems.get(position).getThumbnails();

        // img.setImageURI(media.get(0).getUrl());
        // UrlImageViewHelper.setUrlDrawable(img, "media.get(0).getUrl()");
        // ((RelativeLayout) itemView).addView(text);
        // ((RelativeLayout) itemView).addView(imgView);
        // }

        // Add viewpager_item.xml to ViewPager
        ((ViewPager) container).addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // Remove viewpager_item.xml from ViewPager
        ((ViewPager) container).removeView((LinearLayout) object);

    }

    @Override
    public float getPageWidth(int position) {
        // TODO Auto-generated method stub

        return 0.4f;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return rssItems.size();
    }

    private class loadImageURIs extends AsyncTask<List<RSSItem>, Void, ArrayList<String>> {

        @Override
        protected  void onPostExecute(ArrayList<String> result){

            imageURLs = result;


            for(String image: result){
                Log.e("result: ", image);
            }
        }

        @Override
        protected ArrayList<String> doInBackground(List<RSSItem>... params) {

            Document doc;
            String imageURI = "";
            int imageListLength = 0;
            ArrayList<String> localImages = new ArrayList<>();

            for (int i = 0; i < params.length; i++) {

                //Log.e("rssLinkAsync", "i : (" + i + "): " + rssItems.get(i).getLink() + "");

                try {
                    doc = Jsoup.connect(rssItems.get(i).getLink().toString()).get();

                    //Log.e("dochtml", doc.html());
                    String scriptContent;

                    Elements scriptElements = doc.select("script");
                    try {
                        JSONObject scriptContents = new JSONObject(scriptElements.first().html().toString());
                        JSONObject articleImages = new JSONObject(scriptContents.getString("image"));
                        JSONArray imageList = articleImages.getJSONArray("@list");

                        imageListLength = imageList.length();

                        if (imageList != null && imageListLength > 0) {
                            imageURI = imageList.get(imageListLength-1).toString();
                            localImages.add(imageURI);
                            Log.e("rssImageURIAsync: ", imageURI + " for " + rssItems.get(i).getLink());

                            //imgURLs.add(imageURI);
                        }else{
                           // imgURLs.add(noImage);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
            return localImages;
        }

    }

    private class ParseHTML extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            Document doc;
            String imageURI = "";

            try {
                doc = Jsoup.connect(params[0]).get();

                Log.e("dochtml", doc.html());
                String scriptContent;

                Elements scriptElements = doc.select("script");
                try {
                    JSONObject scriptContents = new JSONObject(scriptElements.first().html().toString());
                    JSONObject articleImages = new JSONObject(scriptContents.getString("image"));
                    JSONArray imageList = articleImages.getJSONArray("@list");

                    if (imageList != null && imageList.length() > 0) {
                        imageURI = imageList.get(0).toString();
                        Log.e("rssImageURI: ", imageURI);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return imageURI;
        }
    }

}
