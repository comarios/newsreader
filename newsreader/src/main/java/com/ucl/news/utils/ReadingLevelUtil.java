package com.ucl.news.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by danyaalmasood on 31/01/2017.
 */

public class ReadingLevelUtil {

    private Context context;

    public ReadingLevelUtil(Context context) {
        this.context = context;
    }

    //Summarise text from story
    public void summary(final String story, final VolleyCallback callback) {
        final String storyText = story;
        RequestQueue requestQueue = null;
        requestQueue = Volley.newRequestQueue(context);

        final String URL = "https://cotomax-summarizer-text-v1.p.mashape.com/summarizer";
        StringRequest req = new StringRequest(Request.Method.POST, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            callback.onSuccess(response);
                        } catch (Exception e) {
                            e.printStackTrace();
                            //Add snackbar message
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.e("Response: ", error.getMessage());
            }
        }) {
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("Percent", "1");
                params.put("Language", "en");
                params.put("Text", storyText);
                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Mashape-Key", "lFI4AQonpGmshBMou4yPqjqdM87fp1aUT2ljsnoTV0t6nQvd0J");
                return headers;
            }
        };

        requestQueue.add(req);
    }

    //Generate keywords from story
    public void keywords(String story, final VolleyCallback callback) {
        try {
            final String storyText = URLEncoder.encode(story, "UTF-8");
            RequestQueue requestQueue = null;
            requestQueue = Volley.newRequestQueue(context);
            final String URL = "https://alchemy.p.mashape.com/text/TextGetRankedKeywords?outputMode=json&text="+storyText+"";
            StringRequest req = new StringRequest(Request.Method.POST, URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                try {
                                    callback.onSuccess(response);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    //Add snackbar message
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                //Add snackbar message
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                @Override
                public Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    return params;
                }
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("X-Mashape-Key", "lFI4AQonpGmshBMou4yPqjqdM87fp1aUT2ljsnoTV0t6nQvd0J");
                    return headers;
                }
            };
            requestQueue.add(req);
        } catch (IOException e) {
            //Add snackbar message
        }
    }

    public interface VolleyCallback{
        void onSuccess(String response);
    }
}


