package com.kg.mrpostman.service;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kg.mrpostman.HomeActivity;
import com.kg.mrpostman.app.AppConfig;
import com.kg.mrpostman.app.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ASUS on 6/29/2017.
 */

public class LocationSaver {

    private static final String TAG = LocationSaver.class.getSimpleName();

    public static void saveLocation(final String longtitude, final String latitude, final String user) {
        // Tag used to cancel the request
        String tag_string_req = "req_save_location";

        //2017-07-13 06:23:20
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        final String strDate = dateFormat.format(date);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("longtitude", longtitude);
            jsonObject.put("latitude", latitude);
            jsonObject.put("user", user);
            jsonObject.put("time", strDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest req = new JsonObjectRequest(AppConfig.URL_SAVE_LOCATION, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Location Saving Response: " + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Location Saving Error: " + error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", HomeActivity.token);
                return headers;
            }
        };
        AppController.getInstance().addToRequestQueue(req, tag_string_req);
    }
}
