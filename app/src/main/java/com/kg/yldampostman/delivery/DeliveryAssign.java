package com.kg.yldampostman.delivery;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kg.yldampostman.R;
import com.kg.yldampostman.HomeActivity;
import com.kg.yldampostman.app.AppConfig;
import com.kg.yldampostman.app.AppController;
import com.kg.yldampostman.helper.CustomJsonArrayRequest;
import com.kg.yldampostman.helper.HelperConstants;
import com.kg.yldampostman.helper.SQLiteHandler;
import com.kg.yldampostman.helper.StringData;
import com.kg.yldampostman.users.LoginActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DeliveryAssign extends AppCompatActivity {
    private static final String TAG = DeliveryAssign.class.getSimpleName();
    private ArrayList<String> sectorList = new ArrayList<>();
    private Button btn_assign;
    private Spinner spn_users;
    private Delivery delivery;
    private ProgressDialog pDialog;
    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_assign);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width), (int) (height));

        btn_assign = (Button) findViewById(R.id.btn_assign);
        spn_users = (Spinner) findViewById(R.id.spinner_assigned_postman);

        db = new SQLiteHandler(getApplicationContext());
        HashMap<String, String> user = db.getUserDetails();
        String userName = user.get("name");

        Intent deliveryIntent = getIntent();
        Bundle extras = deliveryIntent.getExtras();
        if (extras != null) {
            delivery = (Delivery) deliveryIntent.getSerializableExtra("delivery");
        }


        sectorList = StringData.getSectors(HomeActivity.userCity);
        populateUserSpinner();
        spn_users.setSelection(getIndex(spn_users, userName));

        btn_assign.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                updateDelivery(spn_users.getSelectedItem().toString(), delivery.id);
            }
        });
    }

    private void updateDelivery(final String assignedSector, final String id) {
        // Tag used to cancel the request
        String tag_string_req = "req_assign_delivery";

        pDialog.setMessage("Saving Data ...");
        showDialog();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("deliveryId", id);
            jsonObject.put("assignedSector", assignedSector);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest req = new JsonObjectRequest(AppConfig.URL_DELIVERY_ASSIGN, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hideDialog();

                        if (response != null) {
                            Bundle b = new Bundle();
                            b.putString("STATUS", HelperConstants.DELIVERYASSIGNED);
                            Intent intent = new Intent();
                            intent.putExtras(b);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Error on assigning responsible.", Toast.LENGTH_LONG).show();
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
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


    private void populateUserSpinner() {

        spn_users.setAdapter(null);
        ArrayList<String> lables = new ArrayList<String>();

        for (int i = 0; i < sectorList.size(); i++) {
            lables.add(sectorList.get(i));
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lables);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_users.setAdapter(spinnerAdapter);
    }

    private int getIndex(Spinner spinner, String myString) {

        int index = 0;

        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).equals(myString)) {
                index = i;
            }
        }
        return index;
    }

    public void getSectors() {

        String tag_string_req = "req_getsectors";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("city", HomeActivity.userCity);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        CustomJsonArrayRequest req = new CustomJsonArrayRequest(Request.Method.POST, AppConfig.URL_GET_SECTORS, jsonObject,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {

                        try {
                            if (response.length() > 0) {

                                sectorList.clear();
                                sectorList.add("");

                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject c = response.getJSONObject(i);
                                    String sector = c.getString("sector");
                                    sectorList.add(sector);
                                }
                                populateUserSpinner();

                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "Эч нерсе табылбады, же ката пайда болду!", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof AuthFailureError) {
                    Toast.makeText(getApplicationContext(), "Бул операция үчүн уруксатыңыз жок!", Toast.LENGTH_LONG).show();
                    Intent loginIntent = new Intent(DeliveryAssign.this, LoginActivity.class);
                    startActivity(loginIntent);
                } else {
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                }
                hideDialog();
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


    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

}
