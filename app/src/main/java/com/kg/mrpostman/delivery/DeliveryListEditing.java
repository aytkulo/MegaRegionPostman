package com.kg.mrpostman.delivery;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.kg.mrpostman.HomeActivity;
import com.kg.mrpostman.R;
import com.kg.mrpostman.app.AppConfig;
import com.kg.mrpostman.app.AppController;
import com.kg.mrpostman.helper.CustomJsonArrayRequest;
import com.kg.mrpostman.helper.StringData;
import com.kg.mrpostman.utils.MyDialog;
import com.kg.mrpostman.utils.NetworkUtil;

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

public class DeliveryListEditing extends AppCompatActivity {

    public static int DIALOG_ID = 0;
    ListView listViewDeliveries;

    private ProgressDialog pDialog;
    private EditText ed_Date, ed_Phone;
    private Button btn_dList;
    private Calendar calendar;
    private Spinner sCity;
    int year_x, month_x, day_x;

    private String userCity = "";
    private String userName = "";
    private String senderCity = "%";
    private String token = "";

    private List<Delivery> deliveryList = new ArrayList<>();
    private Delivery delivery;
    private String strDate = "";
    private Dialog dialog = null;
    private ImageView imageBulk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_list);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        dialog = new Dialog(this);

        listViewDeliveries = findViewById(R.id.listViewDeliveries);
        ed_Date = findViewById(R.id.ed_Date);
        ed_Phone = findViewById(R.id.ed_Tel);
        imageBulk = findViewById(R.id.imageBulk);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = this.getLayoutInflater();
        View vDialog = inflater.inflate(R.layout.delivery_search_filter_short, null);  // this line
        dialog.setContentView(vDialog);

        btn_dList = (Button) vDialog.findViewById(R.id.btn_submit);

        ed_Date = vDialog.findViewById(R.id.ed_Date);
        ed_Phone = vDialog.findViewById(R.id.ed_Tel);
        sCity = vDialog.findViewById(R.id.sp_Origin);

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");//dd/MM/yyyy
        Date now = new Date();
        strDate = sdfDate.format(now);

        userCity = HomeActivity.userCity;
        userName = HomeActivity.userLogin;
        token = HomeActivity.token;

        ArrayAdapter<String> cityAdapterAll = new ArrayAdapter<String>(
                DeliveryListEditing.this,
                android.R.layout.simple_spinner_dropdown_item,
                StringData.getCityList()
        );

        sCity.setAdapter(cityAdapterAll);
        ed_Date.setText(strDate);

        calendar = Calendar.getInstance();
        year_x = calendar.get(Calendar.YEAR);
        month_x = calendar.get(Calendar.MONTH);
        day_x = calendar.get(Calendar.DAY_OF_MONTH);

        ed_Date.setOnClickListener(v -> showDialog(DIALOG_ID));

        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
     //   dialog.show();

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


        btn_dList.setOnClickListener(v -> {
            deliveryList.clear();
            listViewDeliveries.setAdapter(null);
            try {
                    listDeliveries(ed_Date.getText().toString(), ed_Phone.getText().toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }

        });



        FloatingActionButton fab = findViewById(R.id.fabButtonFilter);
        fab.setOnClickListener(view -> {
            Window window1 = dialog.getWindow();
            window1.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.show();
        });

        fab.setVisibility(View.GONE);


        listViewDeliveries.setOnItemClickListener((parent, view, position, id) -> {

            delivery = (Delivery) parent.getItemAtPosition(position);
                Intent intentDelivery = new Intent(DeliveryListEditing.this, DeliveryAssign.class);
                intentDelivery.putExtra("delivery", delivery);
                startActivityForResult(intentDelivery, 400);
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

        try {
            listDeliveries(ed_Date.getText().toString(), "%");
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 400 ) {
            if (resultCode == RESULT_OK) {
                deliveryList.clear();
                listViewDeliveries.setAdapter(null);
                try {
                    listDeliveries(ed_Date.getText().toString(), ed_Phone.getText().toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void listDeliveries(final String entryDate, final String phone) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(DeliveryListEditing.this)) {
            MyDialog.createSimpleOkErrorDialog(DeliveryListEditing.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(DeliveryListEditing.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {
            String tag_string_req = "req_get_deliveries";
            pDialog.setMessage("Идет запрос...");
            showDialog();

            deliveryList.clear();
            listViewDeliveries.setAdapter(null);

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("entryDate", entryDate);
                jsonObject.put("receiverCity", "%");
                jsonObject.put("senderCity", userCity);
                jsonObject.put("phone", phone + "%");
                jsonObject.put("acceptedPerson", userName);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            CustomJsonArrayRequest req = new CustomJsonArrayRequest(Request.Method.POST, AppConfig.URL_PARCEL_LIST_ACCEPTED, jsonObject,
                    response -> {
                        hideDialog();

                        try {
                            // Check for error node in json
                            if (response.length() > 0) {

                                JsonParser parser = new JsonParser();
                                Gson gson = new Gson();

                                for (int i = 0; i < response.length(); i++) {

                                    JsonElement mJsonM = parser.parse(response.getString(i));
                                    Delivery dd = gson.fromJson(mJsonM, Delivery.class);

                                    dd.number = i + 1;
                                    deliveryList.add(dd);
                                }

                                if (deliveryList.size() > 0) {
                                    DListAdapterAssigning orderListAdapter = new DListAdapterAssigning(deliveryList, DeliveryListEditing.this);
                                    listViewDeliveries.setAdapter(orderListAdapter);
                                }
                                imageBulk.setVisibility(View.GONE);
                                dialog.dismiss();
                            } else {
                                imageBulk.setVisibility(View.VISIBLE);
                                Toast.makeText(DeliveryListEditing.this, getApplicationContext().getString(R.string.NoData), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            MyDialog.createSimpleOkErrorDialog(DeliveryListEditing.this,
                                    getApplicationContext().getString(R.string.dialog_error_title),
                                    getApplicationContext().getString(R.string.ErrorWhenLoading)).show();
                        }

                    }, error -> {
                        NetworkUtil.checkHttpStatus(DeliveryListEditing.this, error);
                        hideDialog();
                    }) {

                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Authorization", token);
                    return headers;
                }

            };
            // Adding request to request queue

            req.setRetryPolicy(new DefaultRetryPolicy(6000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            AppController.getInstance().addToRequestQueue(req, tag_string_req);
        }
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
