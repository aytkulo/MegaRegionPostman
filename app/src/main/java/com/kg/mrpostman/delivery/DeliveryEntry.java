package com.kg.mrpostman.delivery;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kg.mrpostman.HomeActivity;
import com.kg.mrpostman.R;
import com.kg.mrpostman.app.AppConfig;
import com.kg.mrpostman.app.AppController;
import com.kg.mrpostman.customer.CorporateSelectionList;
import com.kg.mrpostman.customer.CustomerHelper;
import com.kg.mrpostman.helper.CustomJsonArrayRequest;
import com.kg.mrpostman.helper.HelperConstants;
import com.kg.mrpostman.helper.StringData;
import com.kg.mrpostman.orders.Orders;
import com.kg.mrpostman.users.LoginActivity;
import com.kg.mrpostman.utils.MyDialog;
import com.kg.mrpostman.utils.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class DeliveryEntry extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1888;
    private static final int REQUEST_PERMISSION = 1111;
    private static final String TAG = DeliveryEntry.class.getSimpleName();
    public static final int SIGNATURE_ACTIVITY = 1;
    public static final int SENDER_COMPANY_LIST = 2;
    public static final int RECEIVER_COMPANY_LIST = 3;
    private String signatureString = "";
    private Button btn_saveData;
    private LinearLayout mContent;
    private ProgressDialog pDialog;

    private Spinner sCity, rCity, delType;
    private EditText sName, sAdres;
    private EditText rName, rAdres;
    AutoCompleteTextView sPhone;
    AutoCompleteTextView rPhone;
    AutoCompleteTextView sComp;
    AutoCompleteTextView rComp;

    ArrayAdapter<String> myAdapterS;
    ArrayAdapter<String> myAdapterR;

    ArrayAdapter<String> myAdapterSC;
    ArrayAdapter<String> myAdapterRC;

    private EditText delCount, delPrice, delItemPrice, delExpl, paidAmount, differentReceiver, deliveryImageNote;
    private TextView textLabelReceiver;
    private RadioGroup rg_payment, rg_buying;
    private RadioButton rb_payment_senderbank, rb_payment_receiverbank, rb_payment_sendercash;
    private Orders orderData;
    private String userName;
    private String usersCity;
    private String token;
    String[] item = {""};

    private String imageNameString = "";
    private Button btn_takePhoto;
    private ImageView imageDelivery;

    Uri imageUri;
    File output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_entry);

        btn_saveData = findViewById(R.id.btn_save_data);
        mContent = findViewById(R.id.linearLayout);
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        initializeItems();

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(
                DeliveryEntry.this,
                android.R.layout.simple_spinner_dropdown_item,
                StringData.getCityList()
        );

        sCity.setAdapter(cityAdapter);
        rCity.setAdapter(cityAdapter);

        sCity.setSelection(getIndex(sCity, HomeActivity.userCity));

        usersCity = HomeActivity.userCity;
        token = HomeActivity.token;
        userName = HomeActivity.userLogin;


        Intent orderIntent = getIntent();
        Bundle extras = orderIntent.getExtras();
        if (extras != null) {
            orderData = (Orders) orderIntent.getSerializableExtra("order");
            putIncomingData(orderData);
        }

        btn_saveData.setOnClickListener(v -> {
            btn_saveData.setEnabled(false);
            if (deliveryDataCheck()) {
                try {
                    saveDelivery(sName.getText().toString(), sPhone.getText().toString(), sComp.getText().toString(), sCity.getSelectedItem().toString(), sAdres.getText().toString(),
                            rName.getText().toString(), rPhone.getText().toString(), rComp.getText().toString(), rCity.getSelectedItem().toString(), rAdres.getText().toString(),
                            delType.getSelectedItem().toString(), delCount.getText().toString(), delPrice.getText().toString(), paidAmount.getText().toString(), delItemPrice.getText().toString(),
                            getPaymentRadioGroupValue(), delExpl.getText().toString(), signatureString, userName, getBuyingRadioGroupValue());
                    // Timeout almamak icin bu servisi buraya aldim.. Eskiden backendte bolchu...
                    CustomerHelper.saveCustomer(sName.getText().toString(), sPhone.getText().toString(), sComp.getText().toString(), sCity.getSelectedItem().toString(), sAdres.getText().toString(), HomeActivity.token);

                } catch (Exception e) {
                    btn_saveData.setEnabled(true);
                    e.printStackTrace();
                }
            }
            btn_saveData.setEnabled(true);
        });




        myAdapterS = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, item);
        sPhone.setAdapter(myAdapterS);
        sPhone.setThreshold(3);

        myAdapterR = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, item);
        rPhone.setAdapter(myAdapterR);
        rPhone.setThreshold(3);

        myAdapterSC = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, item);
        sComp.setAdapter(myAdapterSC);
        sComp.setThreshold(3);

        myAdapterRC = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, item);
        rComp.setAdapter(myAdapterRC);
        rComp.setThreshold(3);

        if(sPhone.getText() ==null || sPhone.getText().toString().length()==0) {
            int position = sPhone.length();
            Editable etext = sPhone.getText();
            sPhone.requestFocus();
            sPhone.setSelection(sPhone.getText().length());
            Selection.setSelection(etext, position);
        }

        // autocompletetextview is in activity_main.xml
        sPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() > 3) {
                    try {
                        getSenderCustomers(s.toString(), "");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 3) {
                    String[] parts = s.toString().split("__");
                    if (parts.length > 1) {
                        sName.setText(parts[1]);
                        sPhone.setText(parts[0]);

                        sCity.setSelection(getIndex(sCity, parts[3]));

                        sAdres.setText(parts[2]);
                        if (parts.length > 4)
                            sComp.setText(parts[4]);
                    }
                }
            }
        });

        rPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() > 3) {
                    try {
                        getReceiverCustomers(s.toString(), "");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 3) {
                    String[] parts = s.toString().split("__");
                    if (parts.length > 1) {
                        rName.setText(parts[1]);
                        rPhone.setText(parts[0]);

                        rCity.setSelection(getIndex(sCity, parts[3]));

                        rAdres.setText(parts[2]);
                        if (parts.length > 4)
                            rComp.setText(parts[4]);
                    }
                }
            }
        });


        sComp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() > 3) {
                    try {
                        getSenderCustomers("", s.toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 3) {
                    String[] parts = s.toString().split("__");
                    if (parts.length > 1) {
                        sName.setText(parts[1]);
                        sPhone.setText(parts[0]);

                        sCity.setSelection(getIndex(sCity, parts[3]));

                        sAdres.setText(parts[2]);
                        sComp.setText(parts[4]);
                    }
                }
            }
        });

        rComp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() > 3) {
                    try {
                        getReceiverCustomers("", s.toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 3) {
                    String[] parts = s.toString().split("__");
                    if (parts.length > 1) {
                        rName.setText(parts[1]);
                        rPhone.setText(parts[0]);

                        rCity.setSelection(getIndex(sCity, parts[3]));

                        rAdres.setText(parts[2]);
                        rComp.setText(parts[4]);
                    }
                }
            }
        });


        rg_payment.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton checkedRadioButton = (RadioButton) group.findViewById(checkedId);
            // This puts the value (true/false) into the variable

            if (checkedRadioButton!=null && checkedRadioButton.equals(rb_payment_receiverbank)) {

                Intent intent = new Intent(DeliveryEntry.this, CorporateSelectionList.class);

                if (rCity.getSelectedItem() != null && rCity.getSelectedItem().toString().length() > 0) {
                    if (rComp.getText() == null || rComp.getText().toString().length() < 1) {
                        intent.putExtra("city", rCity.getSelectedItem().toString());
                        startActivityForResult(intent, RECEIVER_COMPANY_LIST);
                    }
                    else
                    {
                        try {
                            checkCorporateCustomer(rCity.getSelectedItem().toString(), rComp.getText().toString(), intent, RECEIVER_COMPANY_LIST);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    rb_payment_receiverbank.setChecked(false);
                    rAdres.setEnabled(true);
                    rComp.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Биринчи кайсыл шаарга кетишин тандаңыз.", Toast.LENGTH_LONG).show();
                }


            } else if (checkedRadioButton!=null && checkedRadioButton.equals(rb_payment_senderbank)) {

                Intent intent = new Intent(DeliveryEntry.this, CorporateSelectionList.class);

                if (sCity.getSelectedItem() != null && sCity.getSelectedItem().toString().length() > 0) {
                    if (sComp.getText() == null || sComp.getText().toString().length() < 1) {
                        intent.putExtra("city", sCity.getSelectedItem().toString());
                        startActivityForResult(intent, SENDER_COMPANY_LIST);
                    }
                    else
                    {
                        try {
                            checkCorporateCustomer(sCity.getSelectedItem().toString(), sComp.getText().toString(), intent, SENDER_COMPANY_LIST);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    rb_payment_senderbank.setChecked(false);
                    sAdres.setEnabled(true);
                    sComp.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Биринчи кайсыл шаарга кетишин тандаңыз.", Toast.LENGTH_LONG).show();
                }
            }
            else
            {
                sAdres.setEnabled(true);
                sComp.setEnabled(true);
                rAdres.setEnabled(true);
                rComp.setEnabled(true);
            }
        });


        btn_takePhoto.setOnClickListener(v -> {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            output = new File(dir, (new Date()).getTime() +".png");

            imageUri = FileProvider.getUriForFile(DeliveryEntry.this, DeliveryEntry.this.getApplicationContext().getPackageName() + ".provider", output);

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(output));
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        });

    }


    public void checkCorporateCustomer(final String city, final String companyName, final Intent intent, final int COMPANY_LIST) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(DeliveryEntry.this)) {
            MyDialog.createSimpleOkErrorDialog(DeliveryEntry.this,
                    DeliveryEntry.this.getString(R.string.dialog_error_title),
                    DeliveryEntry.this.getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(DeliveryEntry.this,
                    DeliveryEntry.this.getString(R.string.dialog_error_title),
                    DeliveryEntry.this.getString(R.string.relogin)).show();
        } else {
            String tag_string_req = "req_check_corporate_customer";

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("company", companyName);
                jsonObject.put("city", city);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, AppConfig.URL_CORPORATE_CUSTOMER_CHECK, jsonObject,
                    response -> {
                        if (response == null) {
                            intent.putExtra("city", city);
                            startActivityForResult(intent, COMPANY_LIST);
                        }
                    }, error -> {
                        intent.putExtra("city", city);
                        startActivityForResult(intent, COMPANY_LIST);
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

    private String getPaymentRadioGroupValue() {

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

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                imageDelivery.setImageURI(imageUri);
                try  {
                    ParcelFileDescriptor pfd = this.getContentResolver().openFileDescriptor(imageUri, "r");
                    if (pfd != null) {
                        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
                        uploadImage(bitmap);
                    }
                } catch (IOException | ParseException ex) {

                }
            } else {
                // User refused to grant permission.
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_PERMISSION);
                }
                else
                {
                    imageDelivery.setImageURI(imageUri);
                    try  {
                        ParcelFileDescriptor pfd = this.getContentResolver().openFileDescriptor(Uri.fromFile(output), "r");
                        if (pfd != null) {
                            Bitmap bitmap = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
                            uploadImage(bitmap);
                        }
                    } catch (IOException | ParseException ex) {

                    }
                }
            }
        }
        else if (requestCode == SIGNATURE_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                signatureString = bundle.getString("signature");
                byte[] a = Base64.decode(signatureString, Base64.DEFAULT);
                Bitmap image = BitmapFactory.decodeByteArray(a, 0, a.length);
                BitmapDrawable background = new BitmapDrawable(this.getResources(), image);
                mContent.setBackground(background);
            }
        } else if (requestCode == SENDER_COMPANY_LIST) {
            if (resultCode == RESULT_OK) {

                Bundle extras = data.getExtras();
                if (extras != null) {
                    sAdres.setText(extras.getString("address"));
                    sComp.setText(extras.getString("company"));
                    sComp.setEnabled(false);
                    Toast.makeText(getApplicationContext(), "Жөнөтүүчү фирма аты жана адреси өзгөрдү!", Toast.LENGTH_LONG).show();
                }
            } else {
                rb_payment_senderbank.setChecked(false);
                rg_payment.clearCheck();
                sComp.setEnabled(true);
                Toast.makeText(getApplicationContext(), "Төлөм түрү өзгөртүлдү!", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == RECEIVER_COMPANY_LIST) {
            if (resultCode == RESULT_OK) {

                Bundle extras = data.getExtras();
                if (extras != null) {
                    rAdres.setText(extras.getString("address"));
                    rComp.setText(extras.getString("company"));
                    rComp.setEnabled(false);
                    Toast.makeText(getApplicationContext(), "Алуучу фирма аты жана адреси өзгөрдү!", Toast.LENGTH_LONG).show();
                }
            } else {
                rb_payment_receiverbank.setChecked(false);
                rg_payment.clearCheck();
                rComp.setEnabled(true);
                Toast.makeText(getApplicationContext(), "Төлөм түрү өзгөртүлдү!", Toast.LENGTH_LONG).show();
            }
        }
    }


    private void uploadImage(final Bitmap bitmap) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(DeliveryEntry.this)) {
            MyDialog.createSimpleOkErrorDialog(DeliveryEntry.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(DeliveryEntry.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {
            String tag_string_req = "req_save_image";
            pDialog.setMessage("Saving Image ...");
            showDialog();

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("imageString", getStringImage(bitmap));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest req = new JsonObjectRequest(AppConfig.URL_IMAGES_SAVE, jsonObject,
                    response -> {
                        hideDialog();
                        try {
                            if (response.getString("fileName").length() > 0)
                            {
                                imageNameString = response.getString("fileName");
                                imageDelivery.setImageBitmap(bitmap);
                                deliveryImageNote.setText("Фото успешно выгружен.");
                            }
                            else
                            {
                                deliveryImageNote.setText("Не удалось выгрузить фото .");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }, error -> {
                        if (error instanceof AuthFailureError) {
                            Toast.makeText(DeliveryEntry.this, "Бул операция үчүн уруксатыңыз жок!", Toast.LENGTH_LONG).show();
                        }
                        else {
                            MyDialog.createSimpleOkErrorDialog(DeliveryEntry.this,
                                    DeliveryEntry.this.getString(R.string.dialog_error_title),
                                    DeliveryEntry.this.getString(R.string.server_error)).show();
                        }
                        hideDialog();
                        deliveryImageNote.setText("Не удалось выгрузить фото .");
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Authorization", token);
                    return headers;
                }
            };

            req.setRetryPolicy(new DefaultRetryPolicy(6000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(req, tag_string_req);
        }
    }


    private String getStringImage(Bitmap bmp){

        int outWidth;
        int outHeight;
        int inWidth = bmp.getWidth();
        int inHeight = bmp.getHeight();
        if(inWidth > 700){
            outWidth = 700;
            outHeight = (inHeight * 700)/inWidth;
        } else {
            outHeight = inWidth;
            outWidth = inHeight;
        }

        Bitmap bitmap1 = Bitmap.createScaledBitmap(bmp, outWidth, outHeight, true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap1.compress(Bitmap.CompressFormat.PNG, 60, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    private void saveDelivery(final String sName, final String sPhone, final String sComp, final String sCity, final String sAddress,
                              final String rName, final String rPhone, final String rComp, final String rCity, final String rAddress,
                              final String delType, final String delCount, final String delCost, final String paidAmount, final String deliCost,
                              final String paymentType, final String delExpl, final String signature, final String acceptedUser, final String buyingType) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(DeliveryEntry.this)) {
            MyDialog.createSimpleOkErrorDialog(DeliveryEntry.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(DeliveryEntry.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {
            // Tag used to cancel the request
            String tag_string_req = "req_save_delivery";

            pDialog.setMessage("Данные отправляются на сервер...");
            btn_saveData.setEnabled(false);
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
                jsonObject.put("signature", signature);
                jsonObject.put("paymentType", paymentType);
                jsonObject.put("acceptedPerson", acceptedUser);
                jsonObject.put("buyType", buyingType);
                jsonObject.put("assignedSector", "");
                jsonObject.put("deliveredPerson", "");
                jsonObject.put("receiver", "");
                jsonObject.put("deliveryImage", imageNameString);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest req = new JsonObjectRequest(AppConfig.URL_DELIVERY_ENTRY, jsonObject,
                    response -> {

                        hideDialog();
                        btn_saveData.setEnabled(true);
                        try {
                            if (response.getString("deliveryId").length() > 0) {

                                Toast.makeText(getApplicationContext(),
                                        "Маалыматтар системага киргизилди!", Toast.LENGTH_LONG).show();

                                // Bul jer buyuk ihtimal order uzerinden delivery kirilgende kerek bolso kerek. Esimde jok.
                                Bundle b = new Bundle();
                                b.putString("STATUS", HelperConstants.DELIVERYACCEPTED);
                                Intent intent = new Intent();
                                intent.putExtras(b);
                                setResult(RESULT_OK, intent);
                                // ---------------

                                finish();
                            } else {
                                MyDialog.createSimpleOkErrorDialog(DeliveryEntry.this,
                                        getApplicationContext().getString(R.string.dialog_error_title),
                                        getApplicationContext().getString(R.string.NoData)).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }, error -> {
                        if (error instanceof AuthFailureError) {
                            if (error.networkResponse.statusCode == 403) {
                                Toast.makeText(DeliveryEntry.this, "Бул операция үчүн уруксатыңыз жок!", Toast.LENGTH_LONG).show();
                                Intent loginIntent = new Intent(DeliveryEntry.this, LoginActivity.class);
                                DeliveryEntry.this.startActivity(loginIntent);
                            } else {
                                Toast.makeText(DeliveryEntry.this, "Бул операция үчүн уруксатыңыз жок!", Toast.LENGTH_LONG).show();
                            }
                        } else if (error.networkResponse.statusCode == 409) {
                            Toast.makeText(DeliveryEntry.this, "Мындай посылка системага киргизилген!", Toast.LENGTH_LONG).show();
                        } else {
                            MyDialog.createSimpleOkErrorDialog(DeliveryEntry.this,
                                    DeliveryEntry.this.getString(R.string.dialog_error_title),
                                    DeliveryEntry.this.getString(R.string.server_error)).show();
                        }
                        hideDialog();
                        btn_saveData.setEnabled(true);
                    }) {

                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Authorization", token);
                    return headers;
                }

            };

            req.setRetryPolicy(new DefaultRetryPolicy(6000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(req, tag_string_req);
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


    public void initializeItems() {

        btn_takePhoto = findViewById(R.id.btn_take_photo);
        imageDelivery = findViewById(R.id.imageDelivery);
        deliveryImageNote= findViewById(R.id.deliveryImageName);

        rg_payment = findViewById(R.id.rg_payment);
        rg_buying = findViewById(R.id.rg_buying);

        rb_payment_senderbank = findViewById(R.id.rb_sb);
        rb_payment_receiverbank = findViewById(R.id.rb_rb);
        rb_payment_sendercash = findViewById(R.id.rb_sc);

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

        sCity.setEnabled(false);

        differentReceiver = findViewById(R.id.differentReceiver);
        textLabelReceiver = findViewById(R.id.textViewReceived);

        textLabelReceiver.setVisibility(View.GONE);
        differentReceiver.setVisibility(View.GONE);
    }


    public void putIncomingData(Orders order) {
        sName.setText(order.senderName);
        sPhone.setText(order.senderPhone);
        sAdres.setText(order.senderAddress);
        sComp.setText(order.senderCompany);
        rName.setText(order.receiverName);
        rPhone.setText(order.receiverPhone);
        rAdres.setText(order.receiverAddress);
        rComp.setText(order.receiverCompany);
        delExpl.setText(order.orderExplanation);

        sCity.setSelection(getIndex(sCity, order.senderCity));
        rCity.setSelection(getIndex(rCity, order.receiverCity));
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

    private ShapeDrawable getShape(int color) {

        ShapeDrawable shape = new ShapeDrawable(new RectShape());
        shape.getPaint().setColor(color);
        shape.getPaint().setStyle(Paint.Style.STROKE);
        shape.getPaint().setStrokeWidth(3);

        return shape;
    }

    private boolean deliveryDataCheck() {

        boolean ok = true;

        if (delItemPrice.getText().toString().length() == 0) {
            delItemPrice.setText("0");
        } else if (Long.parseLong(delItemPrice.getText().toString()) > 0) {
            if (rg_buying.getCheckedRadioButtonId() == -1) {
                String message = "Выкуп түрүн тандабадыңыз.";
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                ok = false;
                return false;
            }
        }

        if (rg_payment.getCheckedRadioButtonId() == -1) {
            String message = "Төлөм түрүн тандабадыңыз.";
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            ok = false;
        }

        if (rCity.getSelectedItem() == null || rCity.getSelectedItem().toString().length() < 1) {
            rCity.setBackground(getShape(Color.MAGENTA));
            ok = false;
        } else {
            rCity.setBackgroundColor(Color.WHITE);
        }

        if (sCity.getSelectedItem()==null || sCity.getSelectedItem().toString().length() < 1) {
            sCity.setBackground(getShape(Color.MAGENTA));
            ok = false;
        } else {
            sCity.setBackgroundColor(Color.WHITE);
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

        if (sCity.getSelectedItem().toString().equalsIgnoreCase("Москва")) {
            if(sPhone.length() < 10) {
                sPhone.setBackground(getShape(Color.MAGENTA));
                ok = false;
            }
        }
        else if (sPhone.length() != 10)
        {
            sPhone.setBackground(getShape(Color.MAGENTA));
            ok = false;
        }
        else {
            sPhone.setBackgroundColor(Color.WHITE);
        }

        if (rName.length() < 1) {
            rName.setBackground(getShape(Color.MAGENTA));
            ok = false;
        } else {
            rName.setBackgroundColor(Color.WHITE);
        }

        if (rCity.getSelectedItem().toString().equalsIgnoreCase("Москва")) {
            if(rPhone.length() < 10) {
                rPhone.setBackground(getShape(Color.MAGENTA));
                ok = false;
            }
        }
        else if (rPhone.length() != 10)
        {
            rPhone.setBackground(getShape(Color.MAGENTA));
            ok = false;
        }
        else {
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

    public void getSenderCustomers(final String phone, final String company) throws ParseException {


        if (!NetworkUtil.isNetworkConnected(DeliveryEntry.this)) {
            MyDialog.createSimpleOkErrorDialog(DeliveryEntry.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(DeliveryEntry.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {

            String tag_string_req = "req_get_customers";

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("phone", phone);
                jsonObject.put("company", company);
                jsonObject.put("city", HomeActivity.userCity);
                jsonObject.put("address", "");
                jsonObject.put("responsiblePerson", "");
                jsonObject.put("customerId", "");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            CustomJsonArrayRequest req = new CustomJsonArrayRequest(Request.Method.POST, AppConfig.URL_CUSTOMER_GET, jsonObject,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            Log.d(TAG, "List customers Response: " + response);
                            try {
                                if (response.length() > 0) {

                                    String[] custs = new String[response.length()];
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject c = response.getJSONObject(i);
                                        custs[i] = c.getString("phone") + "__" + c.getString("responsiblePerson") + "__" + c.getString("address") + "__" + c.getString("city") + "__" + c.getString("company");
                                    }
                                    // update the adapater
                                    myAdapterS = new ArrayAdapter<String>(DeliveryEntry.this, android.R.layout.simple_dropdown_item_1line, custs);
                                    myAdapterSC = new ArrayAdapter<String>(DeliveryEntry.this, android.R.layout.simple_dropdown_item_1line, custs);
                                    sPhone.setAdapter(myAdapterS);
                                    sComp.setAdapter(myAdapterSC);
                                    myAdapterS.notifyDataSetChanged();
                                    myAdapterSC.notifyDataSetChanged();
                                }
                            } catch (JSONException e) {
                                MyDialog.createSimpleOkErrorDialog(DeliveryEntry.this,
                                        getApplicationContext().getString(R.string.dialog_error_title),
                                        getApplicationContext().getString(R.string.ErrorWhenLoading)).show();
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkUtil.checkHttpStatus(DeliveryEntry.this, error);
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
    }


    public void getReceiverCustomers(final String phone, final String company) throws ParseException {


        if (!NetworkUtil.isNetworkConnected(DeliveryEntry.this)) {
            MyDialog.createSimpleOkErrorDialog(DeliveryEntry.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(DeliveryEntry.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {
            String tag_string_req = "req_get_customers";

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("phone", phone);
                jsonObject.put("company", company);
                jsonObject.put("city", "");
                jsonObject.put("address", "");
                jsonObject.put("responsiblePerson", "");
                jsonObject.put("customerId", "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            CustomJsonArrayRequest req = new CustomJsonArrayRequest(Request.Method.POST, AppConfig.URL_CUSTOMER_GET, jsonObject,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            Log.d(TAG, "List customers Response: " + response);

                            try {
                                if (response.length() > 0) {

                                    String[] custs = new String[response.length()];
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject c = response.getJSONObject(i);
                                        custs[i] = c.getString("phone") + "__" + c.getString("responsiblePerson") + "__" + c.getString("address") + "__" + c.getString("city") + "__" + c.getString("company");
                                    }
                                    myAdapterR = new ArrayAdapter<String>(DeliveryEntry.this, android.R.layout.simple_dropdown_item_1line, custs);
                                    rPhone.setAdapter(myAdapterR);
                                    myAdapterR.notifyDataSetChanged();
                                    myAdapterRC = new ArrayAdapter<String>(DeliveryEntry.this, android.R.layout.simple_dropdown_item_1line, custs);
                                    rComp.setAdapter(myAdapterRC);
                                    myAdapterRC.notifyDataSetChanged();

                                }
                            } catch (JSONException e) {
                                MyDialog.createSimpleOkErrorDialog(DeliveryEntry.this,
                                        getApplicationContext().getString(R.string.dialog_error_title),
                                        getApplicationContext().getString(R.string.ErrorWhenLoading)).show();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkUtil.checkHttpStatus(DeliveryEntry.this, error);
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
            AppController.getInstance().addToRequestQueue(req, tag_string_req);
        }
    }
}
