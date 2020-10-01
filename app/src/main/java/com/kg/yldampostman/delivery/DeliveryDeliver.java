package com.kg.yldampostman.delivery;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kg.yldampostman.R;
import com.kg.yldampostman.app.AppConfig;
import com.kg.yldampostman.app.AppController;
import com.kg.yldampostman.customer.CustomerHelper;
import com.kg.yldampostman.helper.HelperConstants;
import com.kg.yldampostman.helper.SMSManager;
import com.kg.yldampostman.helper.SessionManager;
import com.kg.yldampostman.helper.Signature;
import com.kg.yldampostman.helper.StringData;
import com.kg.yldampostman.users.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DeliveryDeliver extends AppCompatActivity {
    private static final String TAG = DeliveryDeliver.class.getSimpleName();
    public static final int SIGNATURE_ACTIVITY = 1;

    private Spinner sCity, rCity, delType, sProvince, rProvince;
    private EditText sName, sPhone, sComp, sAdres;
    private EditText rName, rPhone, rComp, rAdres;
    private EditText delCount, delPrice, delItemPrice, delExpl, paidAmount;
    private Button btnTakeSignature, btnSaveData;
    private ProgressDialog pDialog;
    private RadioGroup rg_payment;
    private RadioButton rb_rc, rb_sc, rb_sb, rb_rb;
    private RadioButton rb_bc, rb_bd, rb_bt;

    private String signatureString = "";
    private String differentReceiver = "";
    private LinearLayout mContent;
    private Delivery deliveryData;
    private String currentUser;
    private String usersCity;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_deliver);
        mContent = findViewById(R.id.linearLayout);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        initializeItems();
        disableItems();

        SessionManager session = new SessionManager(getApplicationContext());
        usersCity = session.getCity();
        currentUser = session.getLogin();
        token = session.getToken();


        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<String>(
                DeliveryDeliver.this,
                android.R.layout.simple_spinner_dropdown_item,
                StringData.getProvinceList()
        );

        sProvince.setAdapter(provinceAdapter);
        rProvince.setAdapter(provinceAdapter);


        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(
                DeliveryDeliver.this,
                android.R.layout.simple_spinner_dropdown_item,
                StringData.getCityList()
        );

        sCity.setAdapter(cityAdapter);
        rCity.setAdapter(cityAdapter);


        btnTakeSignature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeliveryDeliver.this, Signature.class);
                startActivityForResult(intent, SIGNATURE_ACTIVITY);
            }
        });

        btnSaveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (differentReceiver.length() > 0) {
                    deliverDelivery(deliveryData.id, signatureString, deliveryData.ed_sPhone, currentUser, differentReceiver);
                    CustomerHelper.saveCustomer(rName.getText().toString(), rPhone.getText().toString(), rComp.getText().toString(), rCity.getSelectedItem().toString(), rAdres.getText().toString(), token);
                } else
                    Toast.makeText(getApplicationContext(), getString(R.string.FillTheReceiverData), Toast.LENGTH_LONG).show();// Set your own toast  message
            }
        });

        Intent deliveryIntent = getIntent();
        Bundle extras = deliveryIntent.getExtras();
        if (extras != null) {
            deliveryData = (Delivery) deliveryIntent.getSerializableExtra("delivery");
            putIncomingData(deliveryData);
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SIGNATURE_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    signatureString = bundle.getString("signature");
                    differentReceiver = bundle.getString("nameSurname");
                    byte[] a = Base64.decode(signatureString, Base64.DEFAULT);
                    Bitmap image = BitmapFactory.decodeByteArray(a, 0, a.length);
                    BitmapDrawable background = new BitmapDrawable(this.getResources(), image);
                    mContent.setBackground(background);
                }
                break;
        }
    }

    private void deliverDelivery(final String id, final String signature, final String sPhone, final String currentUser, final String differentReceiver) {
        // Tag used to cancel the request
        String tag_string_req = "req_deliver_delivery";

        pDialog.setMessage("Saving Data ...");
        showDialog();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("deliveryId", id);
            jsonObject.put("signature", signature);
            jsonObject.put("user", currentUser);
            jsonObject.put("receiverName", differentReceiver);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest req = new JsonObjectRequest(AppConfig.URL_DELIVERY_DELIVER, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Data Saving Response: " + response);
                        hideDialog();
                        try {

                            if (response.getString("deliveryId").length() > 0) {

                                String expl = getResources().getString(R.string.DeliveryDelivered);
                                expl = getResources().getString(R.string.DeliveryMessage) + " ЫЛДАМ Express.";
                                if (differentReceiver.length() > 0)
                                    expl = expl + "(" + differentReceiver + ")";
                                SMSManager.sendAcceptanceSMS(sPhone, expl);

                                Bundle b = new Bundle();
                                b.putString("STATUS", HelperConstants.DELIVERYDELIVERED);
                                Intent intent = new Intent();
                                intent.putExtras(b);
                                setResult(RESULT_OK, intent);
                                finish();

                            } else {
                                // Error in login. Get the error message
                                Toast.makeText(getApplicationContext(),
                                        "Error on saving dalivery data.", Toast.LENGTH_LONG).show();
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
                    Intent loginIntent = new Intent(DeliveryDeliver.this, LoginActivity.class);
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

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
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
        else if (selectedValue.equalsIgnoreCase("C"))
            rb_bc.setChecked(true);
        else if (selectedValue.equalsIgnoreCase("T"))
            rb_bt.setChecked(true);
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

        sProvince.setSelection(getIndex(sProvince, StringData.getProvince(delivery.ed_sCity)));
        rProvince.setSelection(getIndex(rProvince, StringData.getProvince(delivery.ed_rCity)));

        delType.setSelection(getIndex(delType, delivery.ed_dType));
        delCount.setText(delivery.ed_dCount);
        delPrice.setText(delivery.ed_dCost);
        delItemPrice.setText(delivery.ed_diCost);
        paidAmount.setText(delivery.ed_paidAmount);

        delExpl.setText(delivery.ed_dExpl);

        setRadioGroupValue(delivery.ed_payment);
        setBuyingRadioGroupValue(delivery.ed_buytype);
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

    public void initializeItems() {
        rg_payment = findViewById(R.id.rg_payment);
        rb_rb = findViewById(R.id.rb_rb);
        rb_rc = findViewById(R.id.rb_rc);
        rb_sb = findViewById(R.id.rb_sb);
        rb_sc = findViewById(R.id.rb_sc);

        rb_bc = findViewById(R.id.rb_buy_cash);
        rb_bd = findViewById(R.id.rb_buy_debt);
        rb_bt = findViewById(R.id.rb_buy_transfer);

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

        btnTakeSignature = findViewById(R.id.btn_takesignature);
        btnSaveData = findViewById(R.id.btn_save_data);

        sCity = findViewById(R.id.spinner_senderCity);
        rCity = findViewById(R.id.spinner_receiverCity);
        delType = findViewById(R.id.spinner_deliveryType);

        sProvince = findViewById(R.id.spinner_senderProvince);
        rProvince = findViewById(R.id.spinner_receiverProvince);
    }

    public void disableItems() {

        rg_payment.setEnabled(false);
        rb_rb.setEnabled(false);
        rb_rc.setEnabled(false);
        rb_sc.setEnabled(false);
        rb_sb.setEnabled(false);
        rb_bc.setEnabled(false);
        rb_bd.setEnabled(false);
        rb_bt.setEnabled(false);
        sName.setEnabled(false);
        sPhone.setEnabled(false);
        sAdres.setEnabled(false);
        sComp.setEnabled(false);
        rPhone.setEnabled(false);
        /*
        rName.setEnabled(false);
        rAdres.setEnabled(false);
        rComp.setEnabled(false);
*/
        delExpl.setEnabled(false);
        delCount.setEnabled(false);
        delPrice.setEnabled(false);
        delItemPrice.setEnabled(false);
        paidAmount.setEnabled(false);

        btnTakeSignature.setText(getResources().getString(R.string.TakeReceiversSignature));
        btnSaveData.setText(getResources().getString(R.string.DeliverDelivery));

        sCity.setEnabled(false);
        rCity.setEnabled(false);
        delType.setEnabled(false);

        sProvince.setEnabled(false);
        rProvince.setEnabled(false);
    }
}
