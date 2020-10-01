package com.kg.yldampostman.delivery;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.kg.yldampostman.HomeActivity;
import com.kg.yldampostman.R;
import com.kg.yldampostman.app.AppConfig;
import com.kg.yldampostman.app.AppController;
import com.kg.yldampostman.helper.CustomJsonArrayRequest;
import com.kg.yldampostman.helper.HelperConstants;
import com.kg.yldampostman.helper.SessionManager;
import com.kg.yldampostman.helper.StringData;
import com.kg.yldampostman.users.LoginActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeliveryList extends AppCompatActivity {

    private static final String TAG = DeliveryList.class.getSimpleName();
    public static int DIALOG_ID = 0;
    ListView listViewDeliveries;

    private ProgressDialog pDialog;
    private EditText ed_Date, ed_Address, ed_Name, ed_Phone;
    private Button btn_dList;
    private Calendar calendar;
    private Spinner sCity, rCity, postmans;
    int year_x, month_x, day_x;
    private String senderCity = "";
    private String receiverCity = "";
    private String status = "%";
    private String responsible = "%";
    private String accepted_person = "%";
    private String userCity = "";
    private String userName = "";
    private String token = "";

    private List<Delivery> deliveryList = new ArrayList<>();
    private ArrayList<String> sectorList = new ArrayList<>();
    private Delivery delivery;
    private String operationType;
    private String strDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_list);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        listViewDeliveries = findViewById(R.id.listViewDeliveries);
        ed_Date = findViewById(R.id.ed_Date);
        ed_Address = findViewById(R.id.ed_Address);
        ed_Name = findViewById(R.id.ed_Name);
        ed_Phone = findViewById(R.id.ed_Tel);

        btn_dList = findViewById(R.id.btn_dList);
        sCity = findViewById(R.id.sp_Origin);
        rCity = findViewById(R.id.sp_Destination);
        postmans = findViewById(R.id.spinner_postman);

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");//dd/MM/yyyy
        Date now = new Date();
        strDate = sdfDate.format(now);

        userCity = HomeActivity.userCity;
        userName = HomeActivity.userLogin;
        token = HomeActivity.token;

        // Ushul jerdi duzelttim.. Sectordu artik koddan alacak.
        // getSectors(userCity);
        sectorList = StringData.getSectors(userCity);
        populateUserSpinner();
        arrangeCities();

        ed_Date.setText(strDate);

        calendar = Calendar.getInstance();
        year_x = calendar.get(Calendar.YEAR);
        month_x = calendar.get(Calendar.MONTH);
        day_x = calendar.get(Calendar.DAY_OF_MONTH);

        ed_Date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ID);
            }
        });


        btn_dList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deliveryList.clear();
                listViewDeliveries.setAdapter(null);
                listDeliveries(ed_Date.getText().toString(), ed_Address.getText().toString(), ed_Name.getText().toString(), ed_Phone.getText().toString());
            }
        });


        listViewDeliveries.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                delivery = (Delivery) parent.getItemAtPosition(position);
                if (operationType.equalsIgnoreCase(HelperConstants.DELIVERY_DELIVER))
                {
                    Intent intentDelivery = new Intent(DeliveryList.this, DeliveryDeliver.class);
                    intentDelivery.putExtra("delivery", delivery);
                    startActivityForResult(intentDelivery, 1);
                }
                else if (operationType.equalsIgnoreCase(HelperConstants.DELIVERY_UPDATE))
                {
                    Intent intentDelivery = new Intent(DeliveryList.this, DeliveryUpdate.class);

                    if (!delivery.entrydate.substring(0,10).equalsIgnoreCase(strDate)) {
                        Toast.makeText(getApplicationContext(),
                                "Бүгүндөн башка күндөгү посылканы өзгөртө албайсыз! " +
                                        "Башка күндү өзгөртүү керек болсо, админ менен сүйлөшүңүз.", Toast.LENGTH_LONG).show();
                    }
                    intentDelivery.putExtra("delivery", delivery);
                    startActivityForResult(intentDelivery, 2);

                } else if (operationType.equalsIgnoreCase(HelperConstants.DELIVERY_ASSIGN)) {
                    Intent intentDelivery = new Intent(DeliveryList.this, DeliveryAssign.class);
                    intentDelivery.putExtra("delivery", delivery);
                    startActivityForResult(intentDelivery, 5);
                }
                else
                    {
                    Intent intentDelivery = new Intent(DeliveryList.this, DeliveryObserve.class);
                    intentDelivery.putExtra("delivery", delivery);
                    startActivityForResult(intentDelivery, 4);
                }

            }
        });


        postmans.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                responsible = postmans.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                responsible = "%";
            }
        });

        sCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                senderCity = sCity.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                senderCity = "%";
            }
        });

        rCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                receiverCity = rCity.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                receiverCity = "%";
            }
        });
    }



    private void populateUserSpinner() {

        postmans.setAdapter(null);
        ArrayList<String> lables = new ArrayList<String>();

        for (int i = 0; i < sectorList.size(); i++) {
            lables.add(sectorList.get(i));
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lables);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        postmans.setAdapter(spinnerAdapter);
    }


    public void listDeliveries(final String entryDate, final String address, final String name, final String phone) {

        String tag_string_req = "req_get_deliveries";
        pDialog.setMessage("Listing Deliveries ...");
        showDialog();

        deliveryList.clear();
        listViewDeliveries.setAdapter(null);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("entryDate", entryDate);
            jsonObject.put("status", status);
            jsonObject.put("receiverCity", receiverCity + "%");
            jsonObject.put("senderCity", senderCity + "%");
            jsonObject.put("assignedSector", responsible + "%");
            jsonObject.put("acceptedPerson", accepted_person + "%");
            jsonObject.put("address", address + "%");
            jsonObject.put("name", name + "%");
            jsonObject.put("phone", phone + "%");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        CustomJsonArrayRequest req = new CustomJsonArrayRequest(Request.Method.POST, AppConfig.URL_DELIVERY_LIST, jsonObject,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, "Deliveries List Response: " + response);
                        hideDialog();

                        try {
                            // Check for error node in json
                            if (response.length() > 0) {

                                for (int i = 0; i < response.length(); i++) {

                                    JSONObject c = response.getJSONObject(i);
                                    Delivery o = new Delivery();
                                    // Storing each json item in variable
                                    o.number = i + 1;
                                    o.sFullName = c.getString("senderName") + " - " + c.getString("senderPhone") + " - " + c.getString("senderCompany");
                                    o.sFullAddress = c.getString("senderCity") + " - " + c.getString("senderAddress");
                                    o.rFullName = c.getString("receiverName") + " - " + c.getString("receiverPhone") + " - " + c.getString("receiverCompany");
                                    o.rFullAddress = c.getString("receiverCity") + " - " + c.getString("receiverAddress");

                                    o.id = c.getString("deliveryId");
                                    o.ed_sCity = c.getString("senderCity");
                                    o.ed_sPhone = c.getString("senderPhone");
                                    o.ed_sCompany = c.getString("senderCompany");
                                    o.ed_sAddress = c.getString("senderAddress");
                                    o.ed_sName = c.getString("senderName");
                                    o.ed_rCity = c.getString("receiverCity");
                                    o.ed_rName = c.getString("receiverName");
                                    o.ed_rPhone = c.getString("receiverPhone");
                                    o.ed_rAddress = c.getString("receiverAddress");
                                    o.ed_rCompany = c.getString("receiverCompany");
                                    o.status = c.getString("status");
                                    o.entrydate = c.getString("entryDate");

                                    o.ed_dType = c.getString("deliveryType");
                                    o.ed_dCount = c.getString("deliveryCount");
                                    o.ed_dCost = c.getString("deliveryCost");
                                    o.ed_diCost = c.getString("deliveryiCost");
                                    o.ed_payment = c.getString("paymentType");
                                    o.ed_dExpl = c.getString("deliveryExplanation");
                                    o.ed_assignedPerson = c.getString("assignedSector");
                                    o.ed_acceptedPerson = c.getString("acceptedPerson");
                                    o.ed_deliveredPerson = c.getString("deliveredPerson").toString();
                                    o.deliveredDate = c.getString("deliveredDate").toString();
                                    o.ed_paidAmount = c.getString("paidAmount");
                                    o.ed_buytype = c.getString("buyType");

                                    deliveryList.add(o);
                                }

                                if (deliveryList.size() > 0) {
                                    DeliveryListAdapter orderListAdapter = new DeliveryListAdapter(deliveryList, DeliveryList.this);
                                    listViewDeliveries.setAdapter(orderListAdapter);
                                }

                            } else {
                                // Error in login. Get the error message
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
                    Intent loginIntent = new Intent(DeliveryList.this, LoginActivity.class);
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
                headers.put("Authorization", token);
                return headers;
            }

        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req, tag_string_req);

    }


    private void arrangeCities() {

        Intent orderIntent = getIntent();
        Bundle extras = orderIntent.getExtras();

        if (extras != null) {

            rCity.setEnabled(true);
            sCity.setEnabled(true);

            ArrayAdapter<String> cityAdapterAll = new ArrayAdapter<String>(
                    DeliveryList.this,
                    android.R.layout.simple_spinner_dropdown_item,
                    StringData.getCityList()
            );

            String province = StringData.getProvince(userCity);
            ArrayAdapter<String> cityAdapter1 = new ArrayAdapter<String>(
                    DeliveryList.this,
                    android.R.layout.simple_spinner_dropdown_item,
                    StringData.getCityList(province)
            );

            operationType = extras.getString(HelperConstants.DELIVERY_OPERATION);

            Log.e(TAG, "Operasyon: " + operationType);

            if (operationType.equalsIgnoreCase(HelperConstants.DELIVERY_DELIVER)) {

                receiverCity = userCity;

                sCity.setAdapter(cityAdapterAll);
                rCity.setAdapter(cityAdapter1);

                rCity.setSelection(getIndex(rCity, receiverCity));
                senderCity = "%";

                status = HelperConstants.DELIVERY_STATUS_NEW;
            }
            else if (operationType.equalsIgnoreCase(HelperConstants.DELIVERY_UPDATE)) {


                status = HelperConstants.DELIVERY_STATUS_NEW;

                sCity.setAdapter(cityAdapter1);
                senderCity = userCity;
                sCity.setSelection(getIndex(sCity, senderCity));
                sCity.setEnabled(false);

                rCity.setSelection(getIndex(rCity, receiverCity));
                rCity.setAdapter(cityAdapterAll);
                receiverCity = "%";

                accepted_person = userName;
                postmans.setEnabled(false);
                ed_Date.setEnabled(false);

            }
            else if (operationType.equalsIgnoreCase(HelperConstants.DELIVERY_ASSIGN)) {

                rCity.setAdapter(cityAdapter1);
                sCity.setAdapter(cityAdapterAll);
                rCity.setEnabled(true);

                status = HelperConstants.DELIVERY_STATUS_NEW;
                postmans.setSelection(getIndex(postmans, "%"));
                postmans.setEnabled(true);
                sCity.setEnabled(true);
            }
            else
                {
                status = "%";
                postmans.setSelection(getIndex(postmans, userName));
                postmans.setEnabled(false);
                sCity.setAdapter(cityAdapter1);
                rCity.setAdapter(cityAdapterAll);
                sCity.setSelection(getIndex(sCity, userCity));
            }

        }
    }


    private int getIndex(Spinner spinner, String myString) {

        int index = 0;
        Log.e(TAG, "Spinner Count: " + spinner.getCount());
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).equals(myString)) {
                index = i;
            }
        }
        return index;
    }

    protected Dialog onCreateDialog(int id) {

        if (id == DIALOG_ID)
            return new DatePickerDialog(this, datePickerListener, year_x, month_x, day_x);
        else
            return null;
    }

    protected DatePickerDialog.OnDateSetListener datePickerListener
            = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

            year_x = year;
            month_x = month + 1;
            day_x = dayOfMonth;
            String dateS = String.valueOf(year);
            if (month_x < 10)
                dateS = dateS + "-0" + month_x;
            else
                dateS = dateS + "-" + month_x;
            if (day_x < 10)
                dateS = dateS + "-0" + day_x;
            else
                dateS = dateS + "-" + day_x;
            ed_Date.setText(dateS);

        }
    };

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
