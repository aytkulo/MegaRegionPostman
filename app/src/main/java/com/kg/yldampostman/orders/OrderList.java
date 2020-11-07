package com.kg.yldampostman.orders;

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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.kg.yldampostman.HomeActivity;
import com.kg.yldampostman.R;
import com.kg.yldampostman.app.AppConfig;
import com.kg.yldampostman.app.AppController;
import com.kg.yldampostman.delivery.DeliveryAssign;
import com.kg.yldampostman.delivery.DeliveryDeliver;
import com.kg.yldampostman.helper.CustomJsonArrayRequest;
import com.kg.yldampostman.helper.PostmanHelper;
import com.kg.yldampostman.helper.SessionManager;
import com.kg.yldampostman.helper.StringData;
import com.kg.yldampostman.users.LoginActivity;
import com.kg.yldampostman.utils.MyDialog;
import com.kg.yldampostman.utils.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderList extends AppCompatActivity {

    private static final String TAG = OrderList.class.getSimpleName();
    public static int DIALOG_ID = 0;

    ListView listViewOrders;
    private ArrayList<String> sectorList = new ArrayList<>();
    private ProgressDialog pDialog;
    private EditText ed_Date;
    private Button btn_List;
    private Spinner sp_Origin;
    private Spinner sp_Sectors;
    private Calendar calendar;
    private CheckBox chck_not_accepted;
    int year_x, month_x, day_x;
    String status = "0";

    String usersCity = "";
    String token = "";

    private List<Orders> orderList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        listViewOrders = findViewById(R.id.listViewOrders);
        ed_Date = findViewById(R.id.ed_Date);
        btn_List = findViewById(R.id.btn_oList);
        sp_Origin = findViewById(R.id.sp_Origin);
        sp_Sectors = findViewById(R.id.sp_sectors);
        chck_not_accepted = findViewById(R.id.chck_not_accepted_yet);

        chck_not_accepted.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked)
                    status = "-1";
                else
                    status = "0";
            }
        });

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);

        ed_Date.setText(strDate);


        calendar = Calendar.getInstance();
        year_x = calendar.get(Calendar.YEAR);
        month_x = calendar.get(Calendar.MONTH);
        day_x = calendar.get(Calendar.DAY_OF_MONTH);

        usersCity = HomeActivity.userCity;
        token = HomeActivity.token;

        String province = StringData.getProvince(usersCity);

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(
                OrderList.this,
                android.R.layout.simple_spinner_dropdown_item,
                StringData.getCityList(province)
        );

        sp_Origin.setAdapter(cityAdapter);
        sp_Origin.setSelection(getIndex(sp_Origin, usersCity));


        sp_Origin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sectorList.clear();
                sp_Sectors.setAdapter(null);
                String senderCity = sp_Origin.getSelectedItem().toString();
                try {
                    PostmanHelper.listPostmans(senderCity, OrderList.this, sp_Sectors);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        ed_Date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ID);
            }
        });

        btn_List.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderList.clear();
                listViewOrders.setAdapter(null);
                try {
                    listOrders(ed_Date.getText().toString(), status, sp_Sectors.getSelectedItem().toString(), sp_Origin.getSelectedItem().toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

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


    private void populateSectorSpinner() {

        sp_Sectors.setAdapter(null);

        ArrayList<String> lables = new ArrayList<String>();

        for (int i = 0; i < sectorList.size(); i++) {
            lables.add(sectorList.get(i));
        }
        // Creating adapter for spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lables);

        // Drop down layout style - list view with radio button
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        sp_Sectors.setAdapter(spinnerAdapter);
    }



    public void listOrders(final String entryDate, final String status, final String responsible, final String origin) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(OrderList.this)) {
            MyDialog.createSimpleOkErrorDialog(OrderList.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(OrderList.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {
            String tag_string_req = "req_getOrders";
            pDialog.setMessage("Listing orders ...");
            showDialog();

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("entryDate", entryDate);
                jsonObject.put("status", status);
                jsonObject.put("sector", responsible);
                jsonObject.put("origin", origin);
                jsonObject.put("destination", "");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            CustomJsonArrayRequest strReq = new CustomJsonArrayRequest(Request.Method.POST, AppConfig.URL_ORDER_GET, jsonObject,
                    new Response.Listener<JSONArray>() {

                        @Override
                        public void onResponse(JSONArray response) {
                            Log.d(TAG, "Orders List Response: " + response);
                            hideDialog();

                            try {
                                // Check for error node in json
                                if (response.length() > 0) {

                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject c = response.getJSONObject(i);
                                        Orders o = new Orders();
                                        // Storing each json item in variable

                                        o.Name = c.getString("senderPhone") + " - " + c.getString("senderName") + " - " + c.getString("senderCompany");
                                        o.Address = c.getString("senderCity") + " - " + c.getString("senderAddress");
                                        o.responsible = c.getString("assignedSector");

                                        o.id = c.getString("orderId");
                                        o.senderCity = c.getString("senderCity");
                                        o.senderPhone = c.getString("senderPhone");
                                        o.senderCompany = c.getString("senderCompany");
                                        o.senderAddress = c.getString("senderAddress");
                                        o.senderName = c.getString("senderName");
                                        o.receiverCity = c.getString("receiverCity");
                                        o.receiverName = c.getString("receiverName");
                                        o.receiverPhone = c.getString("receiverPhone");
                                        o.receiverAddress = c.getString("receiverAddress");
                                        o.receiverCompany = c.getString("receiverCompany");
                                        o.status = c.getString("status");
                                        o.entrydate = c.getString("entryDate");
                                        o.time = c.getString("entryTime");

                                        orderList.add(o);
                                    }

                                    if (orderList.size() > 0) {
                                        OrderListAdapter orderListAdapter = new OrderListAdapter(orderList, OrderList.this);
                                        listViewOrders.setAdapter(orderListAdapter);
                                    }

                                } else {
                                    MyDialog.createSimpleOkErrorDialog(OrderList.this,
                                            getApplicationContext().getString(R.string.dialog_error_title),
                                            getApplicationContext().getString(R.string.NoData)).show();
                                }
                            } catch (JSONException e) {
                                MyDialog.createSimpleOkErrorDialog(OrderList.this,
                                        getApplicationContext().getString(R.string.dialog_error_title),
                                        getApplicationContext().getString(R.string.ErrorWhenLoading)).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkUtil.checkHttpStatus(OrderList.this, error);
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
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        }
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
