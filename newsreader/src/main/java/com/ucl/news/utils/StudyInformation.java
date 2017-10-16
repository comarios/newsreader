package com.ucl.news.utils;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import com.ucl.news.api.GetStudyInformation;
import android.content.Context;
import java.io.UnsupportedEncodingException;
import android.support.design.widget.CoordinatorLayout;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by marios on 16/10/17.
 */
public class StudyInformation {


    private long userID;
    private Context context;

    public StudyInformation(Context context, long userID) {
        this.context = context;
        this.userID = userID;
    }

    public void getStudyInformation(final long userID, final VolleyCallback callback) {
        RequestQueue requestQueue = null;
        requestQueue = Volley.newRequestQueue(context);


        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("user_id", userID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String requestBody = jsonBody.toString();


        final String URL = Constants.SERVER + Constants.STUDY_API + Constants.GET_StudyInformation;
        StringRequest req = new StringRequest(Request.Method.POST, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            callback.onSuccess(response);
                        } catch (Exception e) {
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
//                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }

        };

        requestQueue.add(req);
    }

    public interface VolleyCallback{
        void onSuccess(String response);
    }

}
