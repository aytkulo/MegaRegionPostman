package com.kg.mrpostman.delivery;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.util.Log;
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
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.kg.mrpostman.HomeActivity;
import com.kg.mrpostman.R;
import com.kg.mrpostman.app.AppConfig;
import com.kg.mrpostman.app.AppController;
import com.kg.mrpostman.helper.CustomJsonArrayRequest;
import com.kg.mrpostman.helper.HelperConstants;
import com.kg.mrpostman.helper.PostmanHelper;
import com.kg.mrpostman.helper.StringData;
import com.kg.mrpostman.utils.MyDialog;
import com.kg.mrpostman.utils.NetworkUtil;

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

public class DeliveryList extends AppCompatActivity {

    private static final String TAG = DeliveryList.class.getSimpleName();
    public static int DIALOG_ID = 0;
    ListView listViewDeliveries;

    private ProgressDialog pDialog;
    private EditText ed_Date, ed_Address, ed_Name, ed_Phone;
    private Button btn_dList;
    private Calendar calendar;
    private Spinner sCity, rCity, postmans, spinner_pt;
    int year_x, month_x, day_x;
    private String senderCity = "";
    private String receiverCity = "";
    private String status = "%";
    private String acceptedPostman = "%";
    private String assignedPostman = "%";
    private String userCity = "";
    private String userName = "";
    private String token = "";

    private List<Delivery> deliveryList = new ArrayList<>();
    private Delivery delivery;
    private String operationType;
    private String strDate = "";
    private String payment_type = "%";
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
        ed_Address = findViewById(R.id.ed_Address);
        ed_Name = findViewById(R.id.ed_Name);
        ed_Phone = findViewById(R.id.ed_Tel);
        imageBulk = findViewById(R.id.imageBulk);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = this.getLayoutInflater();
        View vDialog = inflater.inflate(R.layout.delivery_search_filter, null);  // this line
        dialog.setContentView(vDialog);


        btn_dList = (Button) vDialog.findViewById(R.id.btn_submit);

        ed_Date = vDialog.findViewById(R.id.ed_Date);
        ed_Address = vDialog.findViewById(R.id.ed_Address);
        ed_Name = vDialog.findViewById(R.id.ed_Name);
        ed_Phone = vDialog.findViewById(R.id.ed_Tel);
        sCity = vDialog.findViewById(R.id.sp_Origin);
        rCity = vDialog.findViewById(R.id.sp_Destination);
        postmans = vDialog.findViewById(R.id.spinner_postman);

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");//dd/MM/yyyy
        Date now = new Date();
        strDate = sdfDate.format(now);

        userCity = HomeActivity.userCity;
        userName = HomeActivity.userLogin;
        token = HomeActivity.token;

        ArrayAdapter<String> cityAdapterAll = new ArrayAdapter<String>(
                DeliveryList.this,
                android.R.layout.simple_spinner_dropdown_item,
                StringData.getCityList()
        );

        sCity.setAdapter(cityAdapterAll);
        rCity.setAdapter(cityAdapterAll);

        PostmanHelper.populateUserSpinner(DeliveryList.this, postmans, HomeActivity.postmanList);

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

        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();


        btn_dList.setOnClickListener(v -> {
            deliveryList.clear();
            listViewDeliveries.setAdapter(null);
            try {
                boolean check = true;
                if (operationType.equalsIgnoreCase(HelperConstants.DELIVERY_LIST))
                {
                    if (ed_Phone.length() < 6 ) {
                        if(!postmans.getSelectedItem().toString().equalsIgnoreCase(HomeActivity.userLogin)) {
                            ed_Phone.setBackground(getShape(Color.MAGENTA));
                            check = false;
                        }else {
                            ed_Phone.setBackgroundColor(Color.WHITE);
                        }
                    }
                }
                if(check)
                {
                    listDeliveries(ed_Date.getText().toString(), ed_Address.getText().toString(), ed_Name.getText().toString(), ed_Phone.getText().toString(), payment_type);

                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        });



        FloatingActionButton fab = findViewById(R.id.fabButtonFilter);
        fab.setOnClickListener(view -> {
            arrangeCities();
            Window window1 = dialog.getWindow();
            window1.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.show();
        });


        listViewDeliveries.setOnItemClickListener((parent, view, position, id) -> {

            delivery = (Delivery) parent.getItemAtPosition(position);
            if (operationType.equalsIgnoreCase(HelperConstants.DELIVERY_DELIVER)) {
                Intent intentDelivery = new Intent(DeliveryList.this, DeliveryDeliver.class);
                intentDelivery.putExtra("delivery", delivery);
                startActivityForResult(intentDelivery, 100);
            } else if (operationType.equalsIgnoreCase(HelperConstants.DELIVERY_UPDATE)) {
                Intent intentDelivery = new Intent(DeliveryList.this, DeliveryUpdate.class);

                if (!delivery.entryDate.substring(0, 10).equalsIgnoreCase(strDate)) {
                    Toast.makeText(getApplicationContext(),
                            "Бүгүндөн башка күндөгү посылканы өзгөртө албайсыз! " +
                                    "Башка күндү өзгөртүү керек болсо, админ менен сүйлөшүңүз.", Toast.LENGTH_LONG).show();
                }
                intentDelivery.putExtra("delivery", delivery);
                startActivityForResult(intentDelivery, 200);

            } else if (operationType.equalsIgnoreCase(HelperConstants.DELIVERY_DELETE)) {
                Intent intentDelivery = new Intent(DeliveryList.this, DeliveryDelete.class);
                intentDelivery.putExtra("delivery", delivery);
                startActivityForResult(intentDelivery, 600);
            } else {
                Intent intentDelivery = new Intent(DeliveryList.this, DeliveryObserve.class);
                intentDelivery.putExtra("delivery", delivery);
                startActivityForResult(intentDelivery, 4);
            }

        });


        postmans.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if (operationType.equalsIgnoreCase(HelperConstants.DELIVERY_DELIVER))
                        assignedPostman = postmans.getSelectedItem().toString();
                    else
                        acceptedPostman = postmans.getSelectedItem().toString();
                }
                catch (Exception e){}

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                    acceptedPostman = "%";
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

        arrangeCities();
    }


    private ShapeDrawable getShape(int color) {

        ShapeDrawable shape = new ShapeDrawable(new RectShape());
        shape.getPaint().setColor(color);
        shape.getPaint().setStyle(Paint.Style.STROKE);
        shape.getPaint().setStrokeWidth(3);

        return shape;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 || requestCode == 600 || requestCode == 100) {
            if (resultCode == RESULT_OK) {
                deliveryList.clear();
                listViewDeliveries.setAdapter(null);
                try {
                    listDeliveries(ed_Date.getText().toString(), ed_Address.getText().toString(), ed_Name.getText().toString(), ed_Phone.getText().toString(), payment_type);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    public void listDeliveries(final String entryDate, final String address, final String name, final String phone, final String payment_type) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(DeliveryList.this)) {
            MyDialog.createSimpleOkErrorDialog(DeliveryList.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(DeliveryList.this,
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
                jsonObject.put("status", status);
                jsonObject.put("receiverCity", receiverCity + "%");
                jsonObject.put("senderCity", senderCity + "%");
                jsonObject.put("assignedSector", assignedPostman + "%");
                jsonObject.put("acceptedPerson", acceptedPostman);
                jsonObject.put("address", address + "%");
                jsonObject.put("paymentType", payment_type);
                jsonObject.put("name", name + "%");
                jsonObject.put("phone", phone + "%");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            CustomJsonArrayRequest req = new CustomJsonArrayRequest(Request.Method.POST, AppConfig.URL_DELIVERY_LIST, jsonObject,
                    new Response.Listener<JSONArray>() {

                        @Override
                        public void onResponse(JSONArray response) {
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
                                        DeliveryListAdapter orderListAdapter = new DeliveryListAdapter(deliveryList, DeliveryList.this);
                                        listViewDeliveries.setAdapter(orderListAdapter);
                                    }
                                    imageBulk.setVisibility(View.GONE);
                                    dialog.dismiss();
                                } else {
                                    imageBulk.setVisibility(View.VISIBLE);
                                    Toast.makeText(DeliveryList.this, getApplicationContext().getString(R.string.NoData), Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                MyDialog.createSimpleOkErrorDialog(DeliveryList.this,
                                        getApplicationContext().getString(R.string.dialog_error_title),
                                        getApplicationContext().getString(R.string.ErrorWhenLoading)).show();
                            }

                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkUtil.checkHttpStatus(DeliveryList.this, error);
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

            req.setRetryPolicy(new DefaultRetryPolicy(6000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            AppController.getInstance().addToRequestQueue(req, tag_string_req);
        }
    }


    private void arrangeCities() {

        Intent orderIntent = getIntent();
        Bundle extras = orderIntent.getExtras();

        if (extras != null) {

            rCity.setEnabled(true);
            sCity.setEnabled(true);

            operationType = extras.getString(HelperConstants.DELIVERY_OPERATION);

            if (operationType.equalsIgnoreCase(HelperConstants.DELIVERY_DELIVER)) {

                receiverCity = userCity;
                rCity.setSelection(getIndex(rCity, receiverCity));
                rCity.setEnabled(false);
                senderCity = "%";
                acceptedPostman = "%";
                status = HelperConstants.DELIVERY_STATUS_NEW;
            }
            else if (operationType.equalsIgnoreCase(HelperConstants.DELIVERY_UPDATE) || operationType.equalsIgnoreCase(HelperConstants.DELIVERY_DELETE))
            {
                postmans.setEnabled(false);
                ed_Date.setEnabled(false);

                status = HelperConstants.DELIVERY_STATUS_NEW;

                senderCity = userCity;
                sCity.setSelection(getIndex(sCity, senderCity));
                sCity.setEnabled(false);
                receiverCity = "%";
                acceptedPostman = userName;
                postmans.setSelection(getIndex(postmans, userName));
            }
            else if (operationType.equalsIgnoreCase(HelperConstants.DELIVERY_ASSIGN))
            {
                postmans.setEnabled(true);
                status = HelperConstants.DELIVERY_STATUS_NEW;
                rCity.setEnabled(false);
                senderCity = "%";
                sCity.setSelection(getIndex(sCity, senderCity));
                receiverCity = userCity;
            }
            else {
                    status = "%";
                    acceptedPostman = "%";
                    sCity.setSelection(getIndex(sCity, userCity));
            }
        }
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

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
