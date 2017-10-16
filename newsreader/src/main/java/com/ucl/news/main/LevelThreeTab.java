package com.ucl.news.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.ucl.adaptationmechanism.AdaptInterfaceActivity;
import com.ucl.news.adapters.ViewPagerAdapter;
import com.ucl.news.dao.ArticleDAO;
import com.ucl.news.articles.ArticleWebView;
import com.ucl.news.dao.ArticleMetaDataDAO;
import com.ucl.news.reader.RSSItems;
import com.ucl.newsreader.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LevelThreeTab extends Fragment {

    private ArticleWebView webView;
    private ProgressBar progressBarArticle;
    private ArticleDAO aDAO;
    public static ArrayList<ArticleMetaDataDAO> articleMetaData;
    private long startReading;
    private long endReading;
    public static long articleID;
    private static int numberOfWordsInArticle;
    private static Boolean isScrollUsed = false;
    private Boolean isScrollReachedBottom = false;
    public static final String UPDATE = "com.ucl.news.main.ArticleActivity.action.UPDATE";
    public static final String MSG_SEND = "com.ucl.news.main.ArticleActivity.MSG_SEND";
    private View rootView;
    private Document doc;
    private List<String> keywordList;
    private Activity mActivity;
    private List<String> readingLevelFeatures;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final RSSItems rss = getActivity().getIntent().getParcelableExtra(
                ViewPagerAdapter.EXTRA_MESSAGE);
        String rssLink = rss.getLink();
        String mLinkURL = rssLink.replaceAll("www", "m");
        new ParseHTML().execute(mLinkURL);
        progressBarArticle = (ProgressBar) getActivity().findViewById(R.id.progressBarArticleActivity);
        progressBarArticle.setVisibility(View.VISIBLE);
        View rootView = inflater.inflate(R.layout.level_three_tab, container, false);
        readingLevelFeatures = AdaptInterfaceActivity.getReadingLevelFeatures();
        return rootView;
    }

    public void setArticleMetaDataDAO(ArticleMetaDataDAO amdDAO) {
        articleMetaData.add(amdDAO);
    }

    private long extractArticleID(String articleURL) {

        String[] URLTokens;
		/*
		 * Parse it as sport article.
		 */
        if (articleURL.contains("/sport/0/")) {

            URLTokens = articleURL.split("/");
        } else {
            String[] URLTokensTemp = articleURL.split("#");
            if (URLTokensTemp[0].contains("-"))
                URLTokens = URLTokensTemp[0].split("-");
            else
                URLTokens = URLTokensTemp[0].split("/");
        }

        // for(int i = 0; i < URLTokens.length; i++)
        // System.out.println("storyID: " + URLTokens[URLTokens.length - 1]);
        return Long.parseLong(URLTokens[URLTokens.length - 1]);
    }

    private class MyWebViewClient extends WebViewClient {

        //Handle redirects in current webview
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.v("URLNAME", url);
            String urlX = url.replace("file:///", "http://www.bbc.co.uk/");
            String mLinkURL = urlX.replaceAll("www", "m");
            new ParseHTML().execute(mLinkURL);
            progressBarArticle.setVisibility(View.VISIBLE);
            return true;
        }
    }

    public int countWords(String str) {

        if (str == null || str.isEmpty())
            return 0;

        int count = 0;
        for (int e = 0; e < str.length(); e++) {
            if (str.charAt(e) != ' ') {
                count++;
                while (str.charAt(e) != ' ' && e < str.length() - 1) {
                    e++;
                }
            }
        }
        return count;
    }

    private class ParseHTML extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            String htmlcode = "";

            try {
                doc = Jsoup.connect(params[0]).get();

//				Elements scriptElements = doc.select("script");
//				try{
//					JSONObject scriptContents = new JSONObject(scriptElements.first().html().toString());
//
//					JSONObject articleImages = new JSONObject(scriptContents.getString("image"));
//
//					JSONArray imageList = articleImages.getJSONArray("@list");
//
//					Log.e("image", imageList.get(0).toString());
//
//					Log.e("headline", scriptContents.getString("headline"));
//
//				}catch (JSONException e) {
//					e.printStackTrace();
//				}

                Elements el = doc.select("div#orb-footer");

                doc.select("script").remove();
                doc.select("div.tags-container").remove();
                doc.select("div#core-navigation").remove();
                doc.select("div#comp-pattern-library-2").remove();
                doc.select("div#orb-footer").remove();
                doc.select("footer").remove();
                doc.select("div.share").remove();
                doc.select("div.bbccom_display_none").remove();
                doc.select("div.navigation--footer").remove();
                doc.select("img#livestats").remove();
                doc.select("div.push").remove();
                doc.select("div.story-more-push").remove();
                doc.select("div.wrapper").remove();
                doc.select("div.column--secondary").remove();
                doc.select("div.orb-nav-section").remove();
                doc.select("div.site-brand-inner").remove();
                doc.select("div.orb-header").remove();
                doc.select("div#js-navigation-sections-wrapper").remove();
                //doc.select("div.story-more").remove(); //PUT A FLAG ON THIS
                doc.select("script").remove();
                doc.select("div.more-index").remove();
                doc.select("h2.small-promo-group__title").remove();
                doc.select("div.small-promo-group__body").remove();
                doc.select("span.index-title").remove();
                doc.select("span.index-title__container").remove();
                doc.select("div.nav-top").remove();
                doc.select("div.share-this").remove();
                doc.select("div.nav-bottom").remove();
                doc.select("div.alert").remove();
                doc.select("div.pagination-bottom").remove();
                doc.select("div.mod.ll.see-alsos.bg").remove();
                doc.select("div.mod.ll.bg").remove();
                doc.select("div#services-bar").remove();

                doc.select("figure.media-landscape body-width no-caption").remove();
                doc.select("p.story-body__introduction").remove();
                doc.select("div.contact-form optional-is-default ghost-column").remove();
                doc.select("form");
                doc.select("input.contact-form__input--submit").remove();
                doc.select("a.story-body__link-external").remove();

                //extra for sports news
                doc.select("button#sb-2").remove();
                doc.select("h2#related-urls-title").remove();
                doc.select("h2.component__title").remove();
                //doc.select("div.gel-layout").remove();
                doc.select("ul.gel-layout").remove();
                doc.select("div.share-tools").remove();
                doc.select("section#top-stories").remove();
                doc.select("section#related-stories").remove();
                doc.select("section#multi-thumb-promo-1").remove();
                doc.select("section#get-inspired").remove();

                doc.select("div.site-brand site-brand--height").remove();
                doc.select("div.with-extracted-share-icons").remove();
                doc.select("div.container-width-only").remove();
                doc.select("div#breaking-news-container").remove();
                doc.select("div#bbccom_leaderboard_1_2_3_4").remove();

                htmlcode = doc.html();

                System.out.println("hereDOC: " + doc.toString());

                numberOfWordsInArticle = countWords(htmlcode);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return htmlcode;
        }

        //After HTML has been parsed, apply features to story
        @SuppressLint("SetJavaScriptEnabled")
        @Override
        protected void onPostExecute(String result) {
            for (String reading : readingLevelFeatures) {
                if (reading.equals("relatedArticles")) {
                    relatedArticles();
                } else if (reading.equals("originalStory")){
                    doc.select("div.story-more").remove();
                    showWebView(doc.html());
                }
            }
        }
    }

    //Change title and use story's existing related articles
    public void relatedArticles(){
        Element e = doc.select("h2.group__title").first();
        if (e != null) {
            e.html("<h2 class=\"group__title\">Related Articles</h2>");
        }
        showWebView(doc.html());
    }

    //Display the webview after features have been generated
    public void showWebView(String result){
        if(getView() != null) {
            webView = (ArticleWebView) getView().findViewById(R.id.webViewArticleStory);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            webView.setWebViewClient(new MyWebViewClient());
            webView.loadDataWithBaseURL("file:///android_asset/", result, "text/html", null, null); //to load files
            //webView.setOnBottomReachedListener(this, 300);
            progressBarArticle.setVisibility(View.GONE);
            // rl.addView(webView);
            // setContentView(rl, rlp);
        }
    }
}