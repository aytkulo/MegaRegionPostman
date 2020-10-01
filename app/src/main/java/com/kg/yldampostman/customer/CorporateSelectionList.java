package com.kg.yldampostman.customer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.kg.yldampostman.R;
import com.kg.yldampostman.HomeActivity;
import com.kg.yldampostman.app.AppConfig;
import com.kg.yldampostman.app.AppController;
import com.kg.yldampostman.helper.CustomJsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CorporateSelectionList extends AppCompatActivity {

    private static final String TAG = CorporateSelectionList.class.getSimpleName();
    private ProgressDialog pDialog;
    private Button btnClose;
    private ListView listViewCustomers;
    private List<Customer> customerList = new ArrayList<>();
    Customer customer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corporate_selection_list);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        listViewCustomers = findViewById(R.id.listViewCustomers);
        btnClose = findViewById(R.id.btnClose);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width), (int) (height));

        Intent deliveryIntent = getIntent();
        Bundle extras = deliveryIntent.getExtras();
        String city = "";

        if (extras != null) {
            city = extras.getString("city");
        }

        listCustomers("", "", city);

        listViewCustomers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                customer = (Customer) parent.getItemAtPosition(position);
                Intent intent = new Intent();
                intent.putExtra("company", customer.cus_Company);
                intent.putExtra("address", customer.cus_Address);
                intent.putExtra("city", customer.cus_City);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

    }


    public void listCustomers(final String address, final String company, final String city) {

        String tag_string_req = "req_list_customers";
        pDialog.setMessage("Listing Corporate Customers...");
        showDialog();

        listViewCustomers.setAdapter(null);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("address", address);
            jsonObject.put("city", city);
            jsonObject.put("company", company);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        CustomJsonArrayRequest strReq = new CustomJsonArrayRequest(Request.Method.POST, AppConfig.URL_CORPORATE_CUSTOMER_LIST, jsonObject,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        hideDialog();
                        try {
                            if (response.length() > 0) {

                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject c = response.getJSONObject(i);
                                    // Storing each json item in variable
                                    Customer cust = new Customer();

                                    cust.cusFullName = c.getString("company") + " (" + c.getString("city") + ")";
                                    cust.cus_Address = c.getString("address");
                                    cust.cusFullAddress = c.getString("address");
                                    cust.cus_City = c.getString("city");
                                    cust.cus_Company = c.getString("company");
                                    cust.cus_Phone = c.getString("phone");
                                    cust.cus_id = c.getString("id");

                                    customerList.add(cust);
                                }
                                // update the adapater
                                if (customerList.size() > 0) {
                                    CustomerListAdapter custListAdapter = new CustomerListAdapter(customerList, CorporateSelectionList.this);
                                    listViewCustomers.setAdapter(custListAdapter);
                                }
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
                hideDialog();
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
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
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
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
