package com.kg.yldampostman.users;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kg.yldampostman.R;
import com.kg.yldampostman.HomeActivity;
import com.kg.yldampostman.app.AppConfig;
import com.kg.yldampostman.app.AppController;
import com.kg.yldampostman.helper.SessionManager;
import com.kg.yldampostman.orders.OrderEntry;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UpdateData extends AppCompatActivity {
    private static final String TAG = UpdateData.class.getSimpleName();

    private Button btn_update;
    private EditText txt_username;
    private EditText txt_fullname;
    private EditText txt_old_password;
    private EditText txt_new_password;
    private ProgressDialog pDialog;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_data);

        btn_update = findViewById(R.id.btnUpdate);
        txt_old_password = findViewById(R.id.old_password);
        txt_new_password = findViewById(R.id.new_password);
        txt_username = findViewById(R.id.username);
        txt_fullname = findViewById(R.id.fullname);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        String login = session.getLogin();
        String fulname = session.getNameSurname();

        // Displaying the user details on the screen
        txt_username.setText(login);
        txt_fullname.setText(fulname);

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String old_password = txt_old_password.getText().toString().trim();
                String username = txt_username.getText().toString().trim();
                String new_password = txt_new_password.getText().toString().trim();
                String fullName = txt_fullname.getText().toString().trim();

                if (!old_password.isEmpty() && !username.isEmpty() && !new_password.isEmpty()) {
                    updatePassword(old_password, username, new_password, fullName);
                    Intent intent = new Intent(UpdateData.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter your details!", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
    }


    private void logoutUser() {
        session.setLogin(false, "2000-01-01");
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void updatePassword(final String old_password, final String username,
                                final String new_password, final String fullname) {
        // Tag used to cancel the request
        String tag_string_req = "req_update";

        pDialog.setMessage("Updating ...");
        showDialog();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("nameSurname", fullname);
            jsonObject.put("email", username);
            jsonObject.put("new_password", new_password);
            jsonObject.put("old_password", old_password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest strReq = new JsonObjectRequest(AppConfig.URL_UPDATE_PSW, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Update Response: " + response.toString());
                        hideDialog();
                        try {
                            if (response.getString("userId").length() > 0) {
                                Toast.makeText(getApplicationContext(), "Пароль өзгөтүлдү!", Toast.LENGTH_LONG).show();
                                logoutUser();
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "Кандайдыр бир ката пайда болду", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof AuthFailureError) {
                    Toast.makeText(getApplicationContext(), "Бул операция үчүн уруксатыңыз жок!", Toast.LENGTH_LONG).show();
                    Intent loginIntent = new Intent(UpdateData.this, LoginActivity.class);
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
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
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
