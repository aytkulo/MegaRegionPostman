package com.kg.yldampostman.helper;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.kg.yldampostman.HomeActivity;
import com.kg.yldampostman.R;
import com.kg.yldampostman.app.AppConfig;
import com.kg.yldampostman.app.AppController;
import com.kg.yldampostman.users.User;
import com.kg.yldampostman.utils.MyDialog;
import com.kg.yldampostman.utils.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PostmanHelper {

    static ArrayList<User> userList = new ArrayList<>();

    public static void listPostmans(final String city, final Context context, final Spinner postmans) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(context)) {
            MyDialog.createSimpleOkErrorDialog(context,
                    context.getString(R.string.dialog_error_title),
                    context.getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(context,
                    context.getString(R.string.dialog_error_title),
                    context.getString(R.string.relogin)).show();
        } else {
            String tag_string_req = "req_get_deliveries";

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("city", city);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            CustomJsonArrayRequest req = new CustomJsonArrayRequest(Request.Method.POST, AppConfig.URL_GET_USERS, jsonObject,
                    new Response.Listener<JSONArray>() {

                        @Override
                        public void onResponse(JSONArray response) {

                            try {
                                if (response.length() > 0) {

                                    userList.clear();
                                    JsonParser parser = new JsonParser();
                                    Gson gson = new Gson();

                                    for (int i = 0; i < response.length(); i++) {

                                        JsonElement mJsonM = parser.parse(response.getString(i));
                                        User dd = gson.fromJson(mJsonM, User.class);
                                        userList.add(dd);
                                    }

                                    if (userList.size() > 0) {
                                        populateUserSpinner(context, postmans);
                                    }

                                } else {
                                    MyDialog.createSimpleOkErrorDialog(context,
                                            context.getString(R.string.dialog_error_title),
                                            context.getString(R.string.NoData)).show();
                                }
                            } catch (JSONException e) {
                                MyDialog.createSimpleOkErrorDialog(context,
                                        context.getString(R.string.dialog_error_title),
                                        context.getString(R.string.ErrorWhenLoading)).show();
                            }

                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkUtil.checkHttpStatus(context, error);
                }
            }) {

                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Authorization", HomeActivity.token);
                    return headers;
                }

            };
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(req, tag_string_req);
        }
    }



    private static void populateUserSpinner(Context context, Spinner postmans) {

        postmans.setAdapter(null);
        ArrayList<String> lables = new ArrayList<String>();

        lables.add("%");
        for (int i = 0; i < userList.size(); i++) {
            lables.add(userList.get(i).getEmail());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, lables);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        postmans.setAdapter(spinnerAdapter);
    }

}
