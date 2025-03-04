package com.kg.mrpostman.delivery;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.kg.mrpostman.HomeActivity;
import com.kg.mrpostman.R;
import com.kg.mrpostman.app.AppConfig;
import com.kg.mrpostman.app.AppController;
import com.kg.mrpostman.helper.StringData;
import com.kg.mrpostman.utils.MyDialog;
import com.kg.mrpostman.utils.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class DeliveryAssign extends AppCompatActivity {
    private static final String TAG = DeliveryAssign.class.getSimpleName();
    private Spinner sCity, rCity, delType;
    private EditText sName, sPhone, sComp, sAdres;
    private EditText rName, rPhone, rComp, rAdres;
    private EditText delCount, delPrice, delItemPrice, delExpl,  paidAmount;
    private RadioButton rb_rc, rb_sc, rb_sb, rb_rb;
    private RadioButton rb_bc, rb_bd, rb_bt;
    private ImageView deliveryImage;
    private Button assignButton;

    Delivery deliveryData;

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_assign);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        initializeItems();

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(
                DeliveryAssign.this,
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

        }

        assignButton.setOnClickListener(v -> {
            try {
                assignDelivery(deliveryData.deliveryId);
            } catch (ParseException e) {
                e.printStackTrace();
            }

        });
    }

    private void assignDelivery(final String deliveryId) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(DeliveryAssign.this)) {
            MyDialog.createSimpleOkErrorDialog(DeliveryAssign.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(DeliveryAssign.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {

            // Tag used to cancel the request
            String tag_string_req = "req_assign_delivery";

            pDialog.setMessage("Saving Data ...");
            showDialog();

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("deliveryId", deliveryId);
                jsonObject.put("assignedSector", HomeActivity.userLogin);
                jsonObject.put("user", HomeActivity.userLogin);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest req = new JsonObjectRequest(AppConfig.URL_PARCEL_ASSIGN, jsonObject,
                    response -> {
                        hideDialog();

                        if (response != null) {
                            Bundle b = new Bundle();
                            Intent intent = new Intent();
                            intent.putExtras(b);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            MyDialog.createSimpleOkErrorDialog(DeliveryAssign.this,
                                    getApplicationContext().getString(R.string.dialog_error_title),
                                    getApplicationContext().getString(R.string.NoData)).show();
                        }

                    }, error -> {
                        NetworkUtil.checkHttpStatus(DeliveryAssign.this, error);
                        hideDialog();
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

    public void initializeItems() {

        assignButton = findViewById(R.id.btn_assign_delivery);
        deliveryImage = findViewById(R.id.imageDeliveryPhoto);
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
        paidAmount = findViewById(R.id.deliveryPaidAmount);


        sCity = findViewById(R.id.spinner_senderCity);
        rCity = findViewById(R.id.spinner_receiverCity);
        delType = findViewById(R.id.spinner_deliveryType);

        delType.setEnabled(false);
        sCity.setEnabled(false);
        rCity.setEnabled(false);

    }

    public void putIncomingData(Delivery delivery) {

        sName.setText(delivery.senderName);
        sPhone.setText(delivery.senderPhone);
        sAdres.setText(delivery.senderAddress);
        sComp.setText(delivery.senderCompany);
        rName.setText(delivery.receiverName);
        rPhone.setText(delivery.receiverPhone);
        rAdres.setText(delivery.receiverAddress);
        rComp.setText(delivery.receiverCompany);
        sCity.setSelection(getIndex(sCity, delivery.senderCity));
        rCity.setSelection(getIndex(rCity, delivery.receiverCity));

        delType.setSelection(getIndex(delType, delivery.deliveryType));
        delCount.setText(delivery.deliveryCount);
        delPrice.setText(delivery.deliveryCost);
        delItemPrice.setText(delivery.deliveryiCost);

        delExpl.setText(delivery.deliveryExplanation);
        paidAmount.setText(delivery.paidAmount);

        setRadioGroupValue(delivery.paymentType);
        setBuyingRadioGroupValue(delivery.buyType);

        if (delivery.deliveryImage != null && delivery.deliveryImage.length() > 1)
            Glide.with(DeliveryAssign.this).load(AppConfig.IMAGES_URL + delivery.deliveryImage).into(deliveryImage);
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
