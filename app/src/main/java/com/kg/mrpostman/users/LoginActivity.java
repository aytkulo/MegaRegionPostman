package com.kg.mrpostman.users;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kg.mrpostman.R;
import com.kg.mrpostman.HomeActivity;
import com.kg.mrpostman.app.AppConfig;
import com.kg.mrpostman.app.AppController;
import com.kg.mrpostman.helper.SessionManager;
import com.kg.mrpostman.utils.MyDialog;
import com.kg.mrpostman.utils.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends Activity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        Button btnLogin = findViewById(R.id.btnLogin);

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
                            "Учетные данные...", Toast.LENGTH_LONG)
                            .show();
                }

            }

        });

    }

    @Override
    public void onBackPressed() {
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

            pDialog.setMessage("Вход ...");
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
                                HomeActivity.userCity = city;
                                HomeActivity.userLogin = login;
                                HomeActivity.apiDate = tillDate;
                                HomeActivity.userRole = role;

                                Intent intent = new Intent();
                                setResult(RESULT_OK, intent);
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
                    Toast.makeText(LoginActivity.this, "Туура эмес маалымат кирдиңиз!", Toast.LENGTH_LONG).show();
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
