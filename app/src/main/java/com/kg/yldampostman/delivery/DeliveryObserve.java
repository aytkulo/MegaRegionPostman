package com.kg.yldampostman.delivery;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
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
import com.kg.yldampostman.helper.StringData;
import com.kg.yldampostman.users.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DeliveryObserve extends AppCompatActivity {
    private static final String TAG = DeliveryObserve.class.getSimpleName();
    private Spinner sCity, rCity, delType;
    private EditText sName, sPhone, sComp, sAdres;
    private EditText rName, rPhone, rComp, rAdres;
    private EditText delCount, delPrice, delItemPrice, delExpl, differentReceiver, paidAmount;
    private RadioButton rb_rc, rb_sc, rb_sb, rb_rb;
    private RadioButton rb_bc, rb_bd, rb_bt;

    String senderSignatureString = "";
    String receiverSignatureString = "";
    String differentReceiverString = "";
    LinearLayout senderSignature;
    LinearLayout receiverSignature;
    Delivery deliveryData;

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_observe);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        initializeItems();

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(
                DeliveryObserve.this,
                android.R.layout.simple_spinner_dropdown_item,
                StringData.getCityList()
        );

        sCity.setAdapter(cityAdapter);
        rCity.setAdapter(cityAdapter);


        Intent deliveryIntent = getIntent();
        Bundle extras = deliveryIntent.getExtras();
        if (extras != null) {
            deliveryData = (Delivery) deliveryIntent.getSerializableExtra("delivery");
            putIncomingData(deliveryData);
            getDelivery(deliveryData.id);
        }
    }


    public void getDelivery(final String id) {

        String tag_string_req = "req_get_deliveries";
        pDialog.setMessage("Getting Receiver Signature ...");
        showDialog();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("deliveryId", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, AppConfig.URL_DELIVERY_GET, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Delivery Get Response: " + response);
                        hideDialog();

                        try {
                            // Check for error node in json
                            if (response.getString("deliveryId").length() > 0) {

                                senderSignatureString = "";
                                receiverSignatureString = response.getString("receiverSignature");
                                differentReceiverString = response.getString("receiver");
                                showSignatures();

                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "Кандайдыр бир ката пайда болду.", Toast.LENGTH_LONG).show();
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
                    Intent loginIntent = new Intent(DeliveryObserve.this, LoginActivity.class);
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

    private void showSignatures() {

        if (receiverSignatureString.length() > 0) {
            byte[] a = Base64.decode(receiverSignatureString, Base64.DEFAULT);
            Bitmap image = BitmapFactory.decodeByteArray(a, 0, a.length);
            BitmapDrawable background = new BitmapDrawable(this.getResources(), image);
            receiverSignature.setBackground(background);
        }

        differentReceiver.setText(differentReceiverString);

    }

    public void initializeItems() {
        rb_rb = findViewById(R.id.rb_rb);
        rb_rc = findViewById(R.id.rb_rc);
        rb_sb = findViewById(R.id.rb_sb);
        rb_sc = findViewById(R.id.rb_sc);


        rb_bc = findViewById(R.id.rb_buy_cash);
        rb_bt = findViewById(R.id.rb_buy_transfer);
        rb_bd = findViewById(R.id.rb_buy_debt);

        sName = findViewById(R.id.senderName);
        sPhone = findViewById(R.id.senderPhone);
        sAdres = findViewById(R.id.senderAddress);
        sComp = findViewById(R.id.senderCompany);
        rName = findViewById(R.id.receiverName);
        rPhone = findViewById(R.id.receiverPhone);
        rAdres = findViewById(R.id.receiverAddress);
        rComp = findViewById(R.id.receiverCompany);

        delExpl = findViewById(R.id.deliveryExplanation);
        delCount = findViewById(R.id.deliveryCount);
        delPrice = findViewById(R.id.deliveryCost);
        delItemPrice = findViewById(R.id.deliveryItemCost);
        differentReceiver = findViewById(R.id.differentReceiver);
        paidAmount = findViewById(R.id.deliveryPaidAmount);


        sCity = findViewById(R.id.spinner_senderCity);
        rCity = findViewById(R.id.spinner_receiverCity);
        delType = findViewById(R.id.spinner_deliveryType);

        senderSignature = findViewById(R.id.linearLayoutS);
        receiverSignature = findViewById(R.id.linearLayoutR);

        delType.setEnabled(false);
        sCity.setEnabled(false);
        rCity.setEnabled(false);

    }

    public void putIncomingData(Delivery delivery) {

        sName.setText(delivery.ed_sName);
        sPhone.setText(delivery.ed_sPhone);
        sAdres.setText(delivery.ed_sAddress);
        sComp.setText(delivery.ed_sCompany);
        rName.setText(delivery.ed_rName);
        rPhone.setText(delivery.ed_rPhone);
        rAdres.setText(delivery.ed_rAddress);
        rComp.setText(delivery.ed_rCompany);
        sCity.setSelection(getIndex(sCity, delivery.ed_sCity));
        rCity.setSelection(getIndex(rCity, delivery.ed_rCity));

        delType.setSelection(getIndex(delType, delivery.ed_dType));
        delCount.setText(delivery.ed_dCount);
        delPrice.setText(delivery.ed_dCost);
        delItemPrice.setText(delivery.ed_diCost);

        delExpl.setText(delivery.ed_dExpl);
        paidAmount.setText(delivery.ed_paidAmount);

        setRadioGroupValue(delivery.ed_payment);
        setBuyingRadioGroupValue(delivery.ed_buytype);
    }

    private void setRadioGroupValue(String selectedValue) {

        if (selectedValue.equalsIgnoreCase("RB"))
            rb_rb.setChecked(true);
        else if (selectedValue.equalsIgnoreCase("RC"))
            rb_rc.setChecked(true);
        else if (selectedValue.equalsIgnoreCase("SB"))
            rb_sb.setChecked(true);
        else if (selectedValue.equalsIgnoreCase("SC"))
            rb_sc.setChecked(true);
    }

    private void setBuyingRadioGroupValue(String selectedValue) {

        if (selectedValue.equalsIgnoreCase("C"))
            rb_bc.setChecked(true);
        else if (selectedValue.equalsIgnoreCase("D"))
            rb_bd.setChecked(true);
        else if (selectedValue.equalsIgnoreCase("T"))
            rb_bt.setChecked(true);

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

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
