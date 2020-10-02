package com.kg.yldampostman.users;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kg.yldampostman.R;
import com.kg.yldampostman.HomeActivity;
import com.kg.yldampostman.app.AppConfig;
import com.kg.yldampostman.app.AppController;
import com.kg.yldampostman.delivery.DeliveryAssign;
import com.kg.yldampostman.helper.SessionManager;
import com.kg.yldampostman.utils.MyDialog;
import com.kg.yldampostman.utils.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class LoginActivity extends Activity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private Button russian, kyrgyz;
    private SessionManager session;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        Button btnLogin = findViewById(R.id.btnLogin);
        russian = findViewById(R.id.btn_russian);
        kyrgyz = findViewById(R.id.btn_kyrgyz);

        kyrgyz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Resources resources = getResources();
                Configuration configuration = resources.getConfiguration();
                Locale locale = new Locale("en");
                configuration.setLocale(locale);
                getBaseContext().createConfigurationContext(configuration);
                showAlert();
            }
        });

        russian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Resources resources = getResources();
                Configuration configuration = resources.getConfiguration();
                Locale locale = new Locale("ru");
                configuration.setLocale(locale);
                getBaseContext().createConfigurationContext(configuration);
                showAlert();
            }
        });

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        session = new SessionManager(getApplicationContext());

        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                // Check for empty data in the form
                if (!email.isEmpty() && !password.isEmpty()) {
                    // login user
                    checkLogin(email, password);
                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(getApplicationContext(),
                            "Please enter the credentials!", Toast.LENGTH_LONG)
                            .show();
                }

            }

        });

    }

    private void showAlert() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setMessage(getResources().getString(R.string.LanguageChanged));
        adb.setIcon(R.drawable.dash_delivery_assign);

        adb.setPositiveButton("МАКУЛ", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        adb.show();
    }


    private void checkLogin(final String email, final String password) {
        // Tag used to cancel the request
        if (!NetworkUtil.isNetworkConnected(LoginActivity.this)) {
            MyDialog.createSimpleOkErrorDialog(LoginActivity.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        }
        else {
            String tag_string_req = "req_login";

            pDialog.setMessage("Logging in ...");
            showDialog();

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("email", email);
                jsonObject.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest req = new JsonObjectRequest(AppConfig.URL_LOGIN, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            hideDialog();
                            try {

                                String token = response.getString("Authorization");
                                String role = response.getString("UserRole");
                                String login = response.getString("UserLogin");
                                String city = response.getString("UserCity");
                                String name = response.getString("UserName");
                                String tillDate = response.getString("TillDate");

                                session.setUser(token, role, city, login, name, tillDate);

                                HomeActivity.token = token;
                                HomeActivity.apiDate = tillDate;

                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();

                            } catch (JSONException e) {
                                MyDialog.createSimpleOkErrorDialog(LoginActivity.this,
                                        getApplicationContext().getString(R.string.dialog_error_title),
                                        getApplicationContext().getString(R.string.ErrorWhenLoading)).show();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkUtil.checkHttpStatus(LoginActivity.this, error);
                    hideDialog();
                }
            }) {

            };

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
}
