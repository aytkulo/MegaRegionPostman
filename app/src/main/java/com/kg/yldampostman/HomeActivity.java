package com.kg.yldampostman;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.navigation.NavigationView;
import com.kg.yldampostman.app.AppConfig;
import com.kg.yldampostman.app.AppController;
import com.kg.yldampostman.delivery.DeliveryDebteds;
import com.kg.yldampostman.delivery.DeliveryEntry;
import com.kg.yldampostman.delivery.DeliveryList;
import com.kg.yldampostman.helper.HelperConstants;
import com.kg.yldampostman.helper.SessionManager;
import com.kg.yldampostman.orders.OrderListAssigned;
import com.kg.yldampostman.service.LocServ;
import com.kg.yldampostman.users.LoginActivity;
import com.kg.yldampostman.users.UpdateData;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SessionManager sessionManager;
    private Button btnDeliveryEntry;
    private Button btnDeliverDelivery;
    private Button btnOrderListAssigned;
    private Button btnDeliveryUpdate;

    public static String token = "";
    public static String userCity = "";
    public static String userLogin = "";
    public static String apiDate;

    private ProgressDialog pDialog;

    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Intent intent = new Intent("com.example.asus.yldam.service.LocServ.class");
        startService(new Intent(getBaseContext(), LocServ.class));

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        btnDeliveryEntry = findViewById(R.id.btn_new_delivery);
        btnDeliverDelivery = findViewById(R.id.btn_deliver_delivery);
        btnOrderListAssigned = findViewById(R.id.btn_order_list_assigned);
        btnDeliveryUpdate = findViewById(R.id.btn_update_delivery);

        sessionManager = new SessionManager(getApplicationContext());

        token = sessionManager.getToken();
        userLogin = sessionManager.getLogin();
        userCity = sessionManager.getCity();
        apiDate = sessionManager.getApiDate();


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        btnDeliveryEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, DeliveryEntry.class);
                startActivity(intent);
            }
        });

        btnDeliverDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, DeliveryList.class);
                intent.putExtra(HelperConstants.DELIVERY_OPERATION, HelperConstants.DELIVERY_DELIVER);
                startActivity(intent);
            }
        });

        btnOrderListAssigned.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, OrderListAssigned.class);
                startActivity(intent);
            }
        });

        btnDeliveryUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, DeliveryList.class);
                intent.putExtra(HelperConstants.DELIVERY_OPERATION, HelperConstants.DELIVERY_UPDATE);
                startActivity(intent);
            }
        });

        try {
            isLoginNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
    /*    if (id == R.id.action_settings) {
            Toast.makeText(this, "This is my Toast message!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(HomeActivity.this,
                    MainActivity.class);
            startActivity(intent);
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_delivery_list) {

            Intent intent = new Intent(HomeActivity.this, DeliveryList.class);
            intent.putExtra(HelperConstants.DELIVERY_OPERATION, HelperConstants.DELIVERY_LIST);
            startActivity(intent);

        }
        else if (id == R.id.nav_delivery_delete) {

            Intent intent = new Intent(HomeActivity.this, DeliveryList.class);
            intent.putExtra(HelperConstants.DELIVERY_OPERATION, HelperConstants.DELIVERY_DELETE);
            startActivity(intent);

        }else if (id == R.id.nav_delivery_assign) {

            Intent intent = new Intent(HomeActivity.this, DeliveryList.class);
            intent.putExtra(HelperConstants.DELIVERY_OPERATION, HelperConstants.DELIVERY_ASSIGN);
            startActivity(intent);

        } else if (id == R.id.nav_delivery_debt) {

            Intent intent = new Intent(HomeActivity.this, DeliveryDebteds.class);
            startActivity(intent);

        } else if (id == R.id.nav_changePassword) {

            Intent intent = new Intent(this, UpdateData.class);
            startActivity(intent);

        } else if (id == R.id.nav_logout) {
            logoutUser();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logoutUser() {
        sessionManager.setUser("", "", "", "", "", "");
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void isLoginNeeded() throws ParseException {
        if (isTokenExpired()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, 111);
        }
    }

    public boolean isTokenExpired() {
        if (apiDate == null || apiDate.length() == 1)
            return true;
        long tillDate = Long.parseLong(apiDate);
        return tillDate < new Date().getTime();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 111) {
            if (resultCode == RESULT_OK) {
                token = sessionManager.getToken();
                apiDate = sessionManager.getApiDate();
                userLogin = sessionManager.getLogin();
                userCity = sessionManager.getCity();
            }
        }
    }




}
