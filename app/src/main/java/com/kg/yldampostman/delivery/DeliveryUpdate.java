package com.kg.yldampostman.delivery;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kg.yldampostman.HomeActivity;
import com.kg.yldampostman.R;
import com.kg.yldampostman.app.AppConfig;
import com.kg.yldampostman.app.AppController;
import com.kg.yldampostman.helper.HelperConstants;
import com.kg.yldampostman.helper.SQLiteHandler;
import com.kg.yldampostman.helper.StringData;
import com.kg.yldampostman.utils.MyDialog;
import com.kg.yldampostman.utils.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class DeliveryUpdate extends AppCompatActivity {

    private static final String TAG = DeliveryUpdate.class.getSimpleName();
    private Spinner sCity, rCity, delType, sProvince, rProvince;
    ;
    private EditText sName, sPhone, sComp, sAdres;
    private EditText rName, rPhone, rComp, rAdres;
    private EditText delCount, delPrice, delItemPrice, delExpl, paidAmount;
    private Button btnTakeSignature;
    private ProgressDialog pDialog;
    private RadioGroup rg_payment;
    private RadioGroup rg_buying;
    private RadioButton rb_rc, rb_sc, rb_sb, rb_rb, rb_bc, rb_bt, rb_bd;
    private CardView card_view_signature, card_view_saving;

    Delivery deliveryData;
    private SQLiteHandler db;
    private String currentUser;
    private String userRole;
    private String usersCity;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_update);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        initializeItems();

       // SessionManager session = new SessionManager(getApplicationContext());

        usersCity = HomeActivity.userCity;
        currentUser = HomeActivity.userLogin;
        token = HomeActivity.token;



        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<String>(
                DeliveryUpdate.this,
                android.R.layout.simple_spinner_dropdown_item,
                StringData.getProvinceList()
        );

        sProvince.setAdapter(provinceAdapter);
        rProvince.setAdapter(provinceAdapter);


        sProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(
                        DeliveryUpdate.this,
                        android.R.layout.simple_spinner_dropdown_item,
                        StringData.getCityList(sProvince.getSelectedItem().toString())
                );
                sCity.setAdapter(cityAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

        rProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(
                        DeliveryUpdate.this,
                        android.R.layout.simple_spinner_dropdown_item,
                        StringData.getCityList(rProvince.getSelectedItem().toString())
                );
                rCity.setAdapter(cityAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });


        Intent deliveryIntent = getIntent();
        Bundle extras = deliveryIntent.getExtras();
        if (extras != null) {
            deliveryData = (Delivery) deliveryIntent.getSerializableExtra("delivery");
            putIncomingData(deliveryData);
        }

        btnTakeSignature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (deliveryDataCheck()) {
                    try {
                        updateDelivery(sName.getText().toString(), sPhone.getText().toString(), sComp.getText().toString(), sCity.getSelectedItem().toString(), sAdres.getText().toString(),
                                rName.getText().toString(), rPhone.getText().toString(), rComp.getText().toString(), rCity.getSelectedItem().toString(), rAdres.getText().toString(),
                                delType.getSelectedItem().toString(), delCount.getText().toString(), delPrice.getText().toString(), paidAmount.getText().toString(), delItemPrice.getText().toString(),
                                getRadioGroupValue(), delExpl.getText().toString(), deliveryData.deliveryId, currentUser, getBuyingRadioGroupValue());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        });

    }


    private String getBuyingRadioGroupValue() {

        String selected = "";

        int selectedValue = rg_buying.getCheckedRadioButtonId();
        if (selectedValue == R.id.rb_buy_cash)
            selected = "C";
        else if (selectedValue == R.id.rb_buy_debt)
            selected = "D";
        else if (selectedValue == R.id.rb_buy_transfer)
            selected = "T";
        return selected;
    }

    private void updateDelivery(final String sName, final String sPhone, final String sComp, final String sCity, final String sAddress,
                                final String rName, final String rPhone, final String rComp, final String rCity, final String rAddress,
                                final String delType, final String delCount, final String delCost, final String paidAmount, final String deliCost,
                                final String paymentType, final String delExpl, final String id, final String user, final String buyType) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(DeliveryUpdate.this)) {
            MyDialog.createSimpleOkErrorDialog(DeliveryUpdate.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(DeliveryUpdate.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {
            // Tag used to cancel the request
            String tag_string_req = "req_update_delivery";

            pDialog.setMessage("Saving Data ...");
            showDialog();

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("senderName", sName);
                jsonObject.put("senderPhone", sPhone);
                jsonObject.put("senderAddress", sAddress);
                jsonObject.put("senderCity", sCity);
                jsonObject.put("senderCompany", sComp);
                jsonObject.put("receiverName", rName);
                jsonObject.put("receiverPhone", rPhone);
                jsonObject.put("receiverAddress", rAddress);
                jsonObject.put("receiverCity", rCity);
                jsonObject.put("receiverCompany", rComp);
                jsonObject.put("deliveryType", delType);
                jsonObject.put("deliveryCount", delCount);
                jsonObject.put("deliveryCost", delCost);
                jsonObject.put("paidAmount", paidAmount);
                jsonObject.put("deliveryiCost", deliCost);
                jsonObject.put("deliveryExplanation", delExpl);
                jsonObject.put("paymentType", paymentType);
                jsonObject.put("buyType", buyType);
                jsonObject.put("deliveryId", id);
                jsonObject.put("user", user);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest strReq = new JsonObjectRequest(AppConfig.URL_DELIVERY_UPDATE, jsonObject,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, "Data Saving Response: " + response);
                            hideDialog();

                            if (response != null) {

                                Bundle b = new Bundle();
                                b.putString("STATUS", HelperConstants.DELIVERYACCEPTED);
                                Intent intent = new Intent();
                                intent.putExtras(b);
                                setResult(RESULT_OK, intent);
                                finish();

                            } else {
                                MyDialog.createSimpleOkErrorDialog(DeliveryUpdate.this,
                                        getApplicationContext().getString(R.string.dialog_error_title),
                                        getApplicationContext().getString(R.string.ErrorWhenLoading)).show();
                            }

                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkUtil.checkHttpStatus(DeliveryUpdate.this, error);
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


    private String getRadioGroupValue() {

        String selected = "";

        int selectedValue = rg_payment.getCheckedRadioButtonId();
        if (selectedValue == R.id.rb_rb)
            selected = "RB";
        else if (selectedValue == R.id.rb_sb)
            selected = "SB";
        else if (selectedValue == R.id.rb_rc)
            selected = "RC";
        else if (selectedValue == R.id.rb_sc)
            selected = "SC";
        return selected;
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

        if (selectedValue.equalsIgnoreCase("D"))
            rb_bd.setChecked(true);
        else if (selectedValue.equalsIgnoreCase("T"))
            rb_bt.setChecked(true);
        else if (selectedValue.equalsIgnoreCase("C"))
            rb_bc.setChecked(true);

    }

    public void initializeItems() {
        rg_payment = (RadioGroup) findViewById(R.id.rg_payment);
        rg_buying = (RadioGroup) findViewById(R.id.rg_buying);

        rb_rb = (RadioButton) findViewById(R.id.rb_rb);
        rb_rc = (RadioButton) findViewById(R.id.rb_rc);
        rb_sb = (RadioButton) findViewById(R.id.rb_sb);
        rb_sc = (RadioButton) findViewById(R.id.rb_sc);

        rb_bc = (RadioButton) findViewById(R.id.rb_buy_cash);
        rb_bd = (RadioButton) findViewById(R.id.rb_buy_debt);
        rb_bt = (RadioButton) findViewById(R.id.rb_buy_transfer);

        sName = (EditText) findViewById(R.id.senderName);
        sPhone = (EditText) findViewById(R.id.senderPhone);
        sAdres = (EditText) findViewById(R.id.senderAddress);
        sComp = (EditText) findViewById(R.id.senderCompany);
        rName = (EditText) findViewById(R.id.receiverName);
        rPhone = (EditText) findViewById(R.id.receiverPhone);
        rAdres = (EditText) findViewById(R.id.receiverAddress);
        rComp = (EditText) findViewById(R.id.receiverCompany);

        delExpl = (EditText) findViewById(R.id.deliveryExplanation);
        delCount = (EditText) findViewById(R.id.deliveryCount);
        delPrice = (EditText) findViewById(R.id.deliveryCost);
        delItemPrice = (EditText) findViewById(R.id.deliveryItemCost);
        paidAmount = (EditText) findViewById(R.id.deliveryPaidAmount);

        btnTakeSignature = (Button) findViewById(R.id.btn_takesignature);

        sCity = (Spinner) findViewById(R.id.spinner_senderCity);
        rCity = (Spinner) findViewById(R.id.spinner_receiverCity);
        delType = (Spinner) findViewById(R.id.spinner_deliveryType);

        sProvince = (Spinner) findViewById(R.id.spinner_senderProvince);
        rProvince = (Spinner) findViewById(R.id.spinner_receiverProvince);

        btnTakeSignature.setText(getResources().getString(R.string.DeliverUpdate));

        card_view_signature = (CardView) findViewById(R.id.card_view_signature);
        card_view_signature.setVisibility(View.GONE);
        card_view_saving = (CardView) findViewById(R.id.card_view_saving);
        card_view_saving.setVisibility(View.GONE);

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

        String senderProvince = StringData.getProvince(delivery.senderCity);
        sProvince.setSelection(getIndex(sProvince, senderProvince));

        String receiverProvince = StringData.getProvince(delivery.receiverCity);
        rProvince.setSelection(getIndex(rProvince, receiverProvince));

        sCity.setSelection(getIndex(sCity, delivery.senderCity));
        rCity.setSelection(getIndex(rCity, delivery.receiverCity));

        delType.setSelection(getIndex(delType, delivery.deliveryType));
        delCount.setText(delivery.deliveryCount);
        delPrice.setText(delivery.deliveryCost);
        delItemPrice.setText(delivery.deliveryiCost);
        paidAmount.setText(delivery.paidAmount);

        delExpl.setText(delivery.deliveryExplanation);

        setRadioGroupValue(delivery.paymentType);
        setBuyingRadioGroupValue(delivery.buyType);

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

    private boolean deliveryDataCheck() {

        boolean ok = true;

        if (delItemPrice.getText().toString().length() == 0) {
            delItemPrice.setText("0");
        }
        if (sName.length() < 1) {
            sName.setBackground(getShape(Color.MAGENTA));
            ok = false;
        } else {
            sName.setBackgroundColor(Color.WHITE);
        }
        if (sAdres.length() < 1) {
            sAdres.setBackground(getShape(Color.MAGENTA));
            ok = false;
        } else {
            sAdres.setBackgroundColor(Color.WHITE);
        }
        if (sPhone.length() != 10) {
            sPhone.setBackground(getShape(Color.MAGENTA));
            ok = false;
        } else {
            sPhone.setBackgroundColor(Color.WHITE);
        }

        if (rName.length() < 1) {
            rName.setBackground(getShape(Color.MAGENTA));
            ok = false;
        } else {
            rName.setBackgroundColor(Color.WHITE);
        }
        if (rPhone.length() != 10) {
            rPhone.setBackground(getShape(Color.MAGENTA));
            ok = false;
        } else {
            rPhone.setBackgroundColor(Color.WHITE);
        }

        if (delCount.length() < 1) {
            delCount.setBackground(getShape(Color.MAGENTA));
            ok = false;
        } else {
            delCount.setBackgroundColor(Color.WHITE);
        }

        if (delPrice.length() < 1) {
            delPrice.setBackground(getShape(Color.MAGENTA));
            ok = false;
        } else {
            delPrice.setBackgroundColor(Color.WHITE);
        }

        if (!ok) {
            String message = getResources().getString(R.string.FillAllDataCorrectly);
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }

        return ok;
    }

    private ShapeDrawable getShape(int color) {

        ShapeDrawable shape = new ShapeDrawable(new RectShape());
        shape.getPaint().setColor(color);
        shape.getPaint().setStyle(Paint.Style.STROKE);
        shape.getPaint().setStrokeWidth(3);

        return shape;
    }
}
