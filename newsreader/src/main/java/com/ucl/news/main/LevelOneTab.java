package com.ucl.news.main;

/**
 * Created by danyaalmasood on 15/12/2016.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ucl.adaptationmechanism.AdaptInterfaceActivity;
import com.ucl.news.adapters.ViewPagerAdapter;
import com.ucl.news.api.ArticleDAO;
import com.ucl.news.articles.ArticleWebView;
import com.ucl.news.dao.ArticleMetaDataDAO;
import com.ucl.news.reader.RSSItems;
import com.ucl.news.utils.ReadingLevelUtil;
import com.ucl.newsreader.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ucl.news.main.ArticleActivity.articleMetaData;

public class LevelOneTab extends Fragment {

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
    private ReadingLevelUtil readingLevelUtil;
    private CoordinatorLayout coordinatorLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final RSSItems rss = getActivity().getIntent().getParcelableExtra(
                ViewPagerAdapter.EXTRA_MESSAGE);
        String rssLink = rss.getLink();
        String mLinkURL = rssLink.replaceAll("www", "m");
        readingLevelUtil = new ReadingLevelUtil(getActivity()); //Common methods for reading level features
        new ParseHTML().execute(mLinkURL);
        progressBarArticle = (ProgressBar) getActivity().findViewById(R.id.progressBarArticleActivity);
        progressBarArticle.setVisibility(View.VISIBLE);
        View rootView = inflater.inflate(R.layout.level_one_tab, container, false);
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

            //Document doc;

            String htmlcode = "";

            System.out.println("hereHTML: " + htmlcode);

            try {
                doc = Jsoup.connect(params[0]).get();
                //regex to remove tags

                System.out.println("################");
                //System.out.println("TESTINGDOC"+doc);

                //String scriptContent;

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
//
//
//				Log.e("sizescript", scriptElements.size()+"");
//
//				Log.e("element", scriptElements.first().html());
//
//
//				Log.e("sizescript", scriptElements.size()+"");

                Elements el = doc.select("div#orb-footer");
                Log.e("el size: ", el.size() + "");
                Log.e("el first: ", el.first().html() + "");

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
                doc.select("div.story-more").remove();
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

                // System.out
                // .println("image size: " + doc.select("figure").size());
                // // doc.select("div#most-popular").remove();
                // doc.select("div.layout-block-b").remove();
                // doc.select("div.share-body-bottom").remove();
                // doc.select("div#page-bookmark-links-head").remove();
                // doc.select("div#id-status-nav").remove();
                // doc.select("div#blq-sign-in").remove();
                // doc.select("div#blq-acc-links").remove();
                // doc.select("div#blq-nav").remove();
                // doc.select("div#related-services").remove();
                // doc.select("div#news-related-sites").remove();
                // doc.select("div#blq-foot").remove();
                // doc.select("div#header-wrapper").remove();
                // doc.select("div#blq-masthead").remove();
                // //doc.select("div#blq-container-outer").remove();
                //
                //
                // // Remove new stuff added
                // doc.select("div#blq-global").remove();
                // doc.select("div.story-related").remove();
                htmlcode = doc.html();
                // System.out.println(doc);

                System.out.println("hereDOC: " + doc.toString());

                //Log.v("HTMLCODE", htmlcode);

                numberOfWordsInArticle = countWords(htmlcode);

                // System.out.println("countWords4: " + numberOfWordsInArticle);

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
            String story = extractStory();
            for (String reading : readingLevelFeatures) {
                if (reading.equals("paragraphSummary")) {
                    paragraphSummary(story);
                } else if (reading.equals("highlightedTerms")) {
                    highlightTerms(story);
                } else if (reading.equals("wordcloud")) {
                    wordCloud(story);
                } else if (reading.equals("keywordList")){
                    keywordsList(story);
                }
            }
        }
    }

    //Extract only the story text from the article
    public String extractStory() {
        StringBuilder sb = new StringBuilder();
        Elements paragraphs = doc.select("div.story-body__inner").select("p");
        for (Element p : paragraphs) {
            sb.append(p.text());
        }
        return sb.toString();
    }

    //Call the keyword extraction API and format the response according to the type passed in
    public void generateKeywords(String story, final String type) {
        readingLevelUtil.keywords(story, new ReadingLevelUtil.VolleyCallback(){
            @Override
            public void onSuccess(String response){
                doc.select("ul.story-body__unordered-list").remove();
                doc.select("h2.story-body__crosshead").remove();
                keywordList = createKeywordList(response); //Create the list of keywords from response
                if (type.equals("highlightTerms")) {
                    highlight();
                } else if (type.equals("keywords")) {
                    keywords();
                }
                showWebView(doc.html());
            }
        });
    }

    //Generate keywords to form keyword list
    public void keywordsList(String story) {
        generateKeywords(story, "keywords");
    }

    //Format the keyword list to display as an unordered list of keywords in the webview
    public void keywords() {
        doc.select("div.story-body__inner").first().select("p").remove();
        if(keywordList != null) {
            for (String key : keywordList) {
                doc.select("div.story-body__inner").append("<p>"+"- "+key+"</p>");
            }
        }
        showWebView(doc.html());
    }

    //Create a wordcloud from the story text
    public void wordCloud(String story){
        //Correct format for API
        final String storyText = story.replaceAll("\\.", " ").replaceAll("[^a-zA-Z0-9\\s]", "");
        RequestQueue requestQueue = null;
        if(isAdded()) {
            requestQueue = Volley.newRequestQueue(getActivity());
        }
        final String URL = "https://wordcloudservice.p.mashape.com/generate_wc";
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,
                URL, getjsonObj(storyText),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            //Remove paragraphs and append image of word cloud in place
                            doc.select("div.story-body__inner").first().select("p").remove();
                            doc.select("ul.story-body__unordered-list").remove();
                            doc.select("h2.story-body__crosshead").remove();
                            String cloudURL = response.getString("url");
                            doc.select("div.story-body__inner").append("<img src="+cloudURL+"/>");
                            showWebView(doc.html());
                        } catch (JSONException e) {
                            displaySnackbarMessageForAPI("Feature could not be loaded. Please check connection and retry", "Retry");
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                String err = new String(error.networkResponse.data);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Mashape-Key", "Cip8AmAnRfmshuhIBl2DV6cLXSJYp1ucpiFjsn7OF3f390IK46");
                return headers;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(10000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        if(requestQueue != null){
            requestQueue.add(req);
        }
    }

    //Convert text into JSONObject to be used as an API parameter
    public JSONObject getjsonObj(String storyText){
        Map<String, String> params = new HashMap<String, String>();
        params.put("textblock", storyText);
        return new JSONObject(params);
    }

    //Generate keywords to be highlighted
    public void highlightTerms(String story) {
        generateKeywords(story, "highlightTerms");
    }

    //Check each paragraph for a keyword in the list and highlight it
    public void highlight() {
        StringBuilder sb = new StringBuilder();
        Elements paragraphs = doc.select("div.story-body__inner").select("p");
        for (Element p : paragraphs) {
            sb.append(p.text()).append("#");
            p.remove();
        }
        String result = sb.toString();
        for(String key:keywordList) {
            result = result.replaceAll("(?i)"+key, "<mark>"+key+"</mark>");
        }
        String[] split = result.split("#");
        for(String spl:split) {
            doc.select("div.story-body__inner").append("<p>" + spl + "</p>");
        }
        showWebView(doc.html());
    }

    //Response received as JSON and converted into List<String> of keywords
    public List<String> createKeywordList(String keywords){
        List<String> keywordList = new ArrayList<>();
        JSONObject jObject = null;
        try {
            jObject = new JSONObject(keywords);
            JSONArray jArray = jObject.getJSONArray("keywords");
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject keywd = jArray.getJSONObject(i);
                String name = keywd.getString("text");
                keywordList.add(name);
            }
        } catch(Exception e){
            displaySnackbarMessageForAPI("An error occurred, please refresh the page", "Refresh");
        }
        return keywordList;
    }

    //Call the summary API and format the response to display a summarised paragraph
    public void paragraphSummary(String story){
        readingLevelUtil.summary(story, new ReadingLevelUtil.VolleyCallback(){
            @Override
            public void onSuccess(String response){
                String formattedResponse = response.replace("\\n", " ").replace("\\", "").replace(".", ". ");
                doc.select("div.story-body__inner").first().select("p").remove();
                doc.select("ul.story-body__unordered-list").remove();
                doc.select("h2.story-body__crosshead").remove();
                doc.select("div.story-body__inner").append("<p>"+formattedResponse+"</p>");
                showWebView(doc.html());
            }
        });
    }

    //Display the webview after features have been generated
    public void showWebView(String result){
        if(getView() != null) {
            webView = (ArticleWebView) getView().findViewById(R.id.webViewArticleStory);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            webView.setWebViewClient(new MyWebViewClient());
            webView.loadDataWithBaseURL("file:///android_asset/", result, "text/html", null, null);
            //webView.setOnBottomReachedListener(this, 300);
            progressBarArticle.setVisibility(View.GONE);
            // rl.addView(webView);
            // setContentView(rl, rlp);
        }
    }

    public void displaySnackbarMessageForAPI(String message, String action) {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, message, Snackbar.LENGTH_LONG)
                .setAction(action, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getFragmentManager().beginTransaction().detach(getParentFragment()).attach(getParentFragment()).commit();
                    }
                });

        snackbar.setActionTextColor(Color.RED);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }
}