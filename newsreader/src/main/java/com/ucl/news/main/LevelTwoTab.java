package com.ucl.news.main;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ucl.adaptationmechanism.AdaptInterfaceActivity;
import com.ucl.news.adapters.ExpandableListAdapter;
import com.ucl.news.adapters.ViewPagerAdapter;
import com.ucl.news.dao.ArticleDAO;
import com.ucl.news.articles.ArticleWebView;
import com.ucl.news.dao.ArticleMetaDataDAO;
import com.ucl.news.reader.RSSItems;
import com.ucl.news.utils.ReadingLevelUtil;
import com.ucl.newsreader.R;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LevelTwoTab extends Fragment {

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
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
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
        View rootView = inflater.inflate(R.layout.level_two_tab, container, false);

        //ExandableListView for accordion
        expListView = (ExpandableListView) rootView.findViewById(R.id.lvExp);
        expListView.setVisibility(View.GONE);
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
                doc.select("div.story-more").remove(); //PUT A FLAG ON THIS
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
            String story = extractStory();
            for (String reading : readingLevelFeatures) {
                if (reading.equals("colourGradient")) {
                    colourGradient();
                } else if (reading.equals("accordionInfo")) {
                    accordionBackground(story);
                } else if (reading.equals("bulletPointSummary")){
                    bulletPoints(story);
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

    //Generate keywords to form accordion
    public void accordionBackground(String story){
        generateKeywords(story, "accordion");
    }

    //Add a stylesheet to the head of the HTML and apply a gradient to the text
    public void colourGradient(){

        Element head = doc.head();
        head.append("<link rel=\"stylesheet\" href=\"file:///android_asset/style.css\">");
        Elements paragraphs = doc.select("div.story-body__inner").select("p");

        System.out.println("paragraphs: " + paragraphs);

        for (Element p : paragraphs) {
            p.remove();
//            doc.select("div.story-body__inner").append("<p class=\"gradientText1\">"+p.text()+"</p>");
//            doc.select("div.story-body__inner").append("<p style=\"background-image: linear-gradient(330deg, blue 0%,  blue 25%, #000066 50%, black 75%, black 100%);color: transparent;-webkit-background-clip: text;background-clip: text;\">"+p.text()+"</p>");
//            doc.select("div.story-body__inner").append("<p style=\"color:blue;\">"+p.text()+"</p>");
            doc.select("div.story-body__inner").append("<p style=\"background: #1e5799;background: -moz-linear-gradient(top,  #1e5799 0%, #2989d8 50%, #207cca 51%, #7db9e8 100%);background: -webkit-linear-gradient(top,  #1e5799 0%,#2989d8 50%,#207cca 51%,#7db9e8 100%);background: linear-gradient(to bottom,  #1e5799 0%,#2989d8 50%,#207cca 51%,#7db9e8 100%);filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#1e5799', endColorstr='#7db9e8',GradientType=0 );\">"+p.text()+"</p>");
//            doc.select("div.story-body__inner").append("<p class=\"gradientText3\">"+p.text()+"</p>");
        }

        System.out.println("colour22" + doc);
        showWebView(doc.html());
    }

    //Call the keyword extraction API and format the response according to the type passed in
    public void generateKeywords(String story, final String type) {
        readingLevelUtil.keywords(story, new ReadingLevelUtil.VolleyCallback(){
            @Override
            public void onSuccess(String response){
                doc.select("ul.story-body__unordered-list").remove();
                doc.select("h2.story-body__crosshead").remove();
                keywordList = createKeywordList(response);
                if (type.equals("accordion")) {
                    accordion();
                }
            }
        });
    }

    HashMap<String, String> keywordMap = new HashMap<>();
    RequestQueue wikiRequestQueue = null;

    //Setup wikipedia queries for the accordion
    public void accordion() {
        if (isAdded()) {
            wikiRequestQueue = Volley.newRequestQueue(getActivity());
        }

        //Query Wikipedia for top 20 keywords
        if (keywordList != null && keywordList.size() > 0) {
            for (int i = 0; i < keywordList.size(); i++) {
                makeWikiRequest(keywordList.get(i), keywordList.size());
            }
        }
    }

    //Make Wikipedia requests for top 20 keywords in keywords list
    public void makeWikiRequest(String key, final int keywordsSize) {
        final String queryKey = key.replaceAll(" +", "%20");
        final String url = "https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch="+queryKey+"&srprop=snippet&srlimit=1&format=json";


        System.out.println("wikipedia key" + queryKey);
        System.out.println("wikipedia url" + url);

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //If no information found, populate keyword content with message, else populate with wikipedia snippet
                            if (response.getJSONObject("query").getJSONObject("searchinfo").getInt("totalhits") == 0) {
                                keywordMap.put(queryKey, "No information found");
                            } else {
                                keywordMap.put(queryKey, Jsoup.parse(response.getJSONObject("query").getJSONArray("search").getJSONObject(0).getString("snippet")).text());
                            }

                            System.out.println("size keywordMap:" + keywordMap.size());
                            System.out.println("size keywordsSize:" + keywordsSize);

                            for (HashMap.Entry<String,String> entry : keywordMap.entrySet()) {
                                String key = entry.getKey();
                                String value = entry.getValue();
                                System.out.println("key:" + key + ", value:" + value);
                            }

                            //If map is filled, generate accordion
                            if (keywordMap.size() == keywordsSize) {
                                generateAccordion();
                            }
                        } catch (Exception e) {
                            StringWriter sw = new StringWriter();
                            PrintWriter pw = new PrintWriter(sw);
                            e.printStackTrace(pw);
                            String sStackTrace = sw.toString(); // stack trace as a string
                            System.out.println("error1: " + sStackTrace);

                            displaySnackbarMessageForAPI("Feature could not be loaded. Please check connection and retry", "Retry");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String err = new String(error.networkResponse.data);
                    }
                }
        );

        if (wikiRequestQueue != null) {
            wikiRequestQueue.add(req);
        }
    }

    //Populate expandableListView with keywords and wikipedia content
    public void generateAccordion() {
        expListView.setVisibility(View.VISIBLE);
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();
        List<String> list = null;

        //Setup list headers from keywordList
        for (int i = 0; i < keywordList.size(); i++) {
            listDataHeader.add(keywordList.get(i));
        }

        //Populate with content
        for (int i = 0; i < listDataHeader.size(); i++) {
            list = new ArrayList<>();
            list.add(keywordMap.get(listDataHeader.get(i).replaceAll(" +", "%20")) == null ? "" : keywordMap.get(listDataHeader.get(i).replaceAll(" +", "%20")));
            listDataChild.put(listDataHeader.get(i), list);
        }

        //Display accordion
        listAdapter = new ExpandableListAdapter(getActivity(), listDataHeader, listDataChild);
        expListView.setAdapter(listAdapter);
    }

    //Response received as JSON and converted into List<String> of keywords
    public List<String> createKeywordList(String keywords){
        List<String> keywordList = new ArrayList<>();
        JSONObject jObject = null;
        try {
            jObject = new JSONObject(keywords);
            JSONArray jArray = jObject.getJSONArray("keywords");
            for (int i=0; i<jArray.length(); i++) {
//                JSONObject keywd = jArray.getJSONObject(i);
//                String name = keywd.getString("text");
                keywordList.add(jArray.get(i).toString());
            }
        } catch(Exception e){
            displaySnackbarMessageForAPI("An error occurred, please refresh the page", "Refresh");
        }
        return keywordList;
    }

    //Call the summary API and format the response to display a bullet points summary
    public void bulletPoints(String story){
        readingLevelUtil.summary(story, new ReadingLevelUtil.VolleyCallback() {
            @Override
            public void onSuccess(String response) {
                String formattedResponse = response.replace("\\n", " ").replace("\\", "").replace(".", ". ").replaceAll("\\.", "<br><br> •");
                doc.select("div.story-body__inner").first().select("p").remove();
                doc.select("ul.story-body__unordered-list").remove();
                doc.select("h2.story-body__crosshead").remove();
                doc.select("div.story-body__inner").append("<p>" + "• " + formattedResponse + "</p>");
                showWebView(doc.html());
            }
        });
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