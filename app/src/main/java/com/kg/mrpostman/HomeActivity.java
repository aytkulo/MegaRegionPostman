package com.kg.mrpostman;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.kg.mrpostman.delivery.DeliveryDebteds;
import com.kg.mrpostman.delivery.DeliveryEntry;
import com.kg.mrpostman.delivery.DeliveryList;
import com.kg.mrpostman.helper.HelperConstants;
import com.kg.mrpostman.helper.PostmanHelper;
import com.kg.mrpostman.helper.SessionManager;
import com.kg.mrpostman.helper.StringData;
import com.kg.mrpostman.orders.OrderList;
import com.kg.mrpostman.orders.OrderListAssigned;
import com.kg.mrpostman.users.LoginActivity;
import com.kg.mrpostman.users.UpdateData;
import com.kg.mrpostman.users.User;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private NavigationView navigationView;
    private SessionManager sessionManager;
    private CardView cardNewDelivery, cardUpdateDelivery, cardDeliverDelivery, cardDeliveryList;
 //   private ImageView imageShare, imageProfile;
    public static String token, apiDate;


    public static String userCity = "";
    public static String userLogin = "";
    public static String userRole;
    public static String sector;

    public static ArrayList<User> postmanList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {

            cardNewDelivery = findViewById(R.id.cardNewDelivery);
            cardUpdateDelivery = findViewById(R.id.cardUpdateDelivery);
            cardDeliverDelivery = findViewById(R.id.cardDeliverDelivery);
            cardDeliveryList = findViewById(R.id.cardDeliveryList);

        //    imageShare = findViewById(R.id.imageShare);
        //    imageProfile = findViewById(R.id.imageProfile);

            sessionManager = new SessionManager(this);
            token = sessionManager.getToken();
            apiDate = sessionManager.getApiDate();
            userRole = sessionManager.getRole();
            userCity = sessionManager.getCity();
            userLogin = sessionManager.getLogin();
            sector = sessionManager.getSector();

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            //  getSupportActionBar().setDisplayShowTitleEnabled(false);

            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);


            cardDeliverDelivery.setOnClickListener(view -> {
                Intent intent = new Intent(HomeActivity.this, DeliveryList.class);
                intent.putExtra(HelperConstants.DELIVERY_OPERATION, HelperConstants.DELIVERY_DELIVER);
                startActivity(intent);
            });

            cardNewDelivery.setOnClickListener(view -> {
                Intent intent = new Intent(HomeActivity.this, DeliveryEntry.class);
                startActivity(intent);
            });

            cardUpdateDelivery.setOnClickListener(view -> {
                Intent intent = new Intent(HomeActivity.this, DeliveryList.class);
                intent.putExtra(HelperConstants.DELIVERY_OPERATION, HelperConstants.DELIVERY_UPDATE);
                startActivity(intent);
            });

            cardDeliveryList.setOnClickListener(view -> {
                Intent intent = new Intent(HomeActivity.this, DeliveryList.class);
                intent.putExtra(HelperConstants.DELIVERY_OPERATION, HelperConstants.DELIVERY_LIST);
                startActivity(intent);
            });

            PostmanHelper.getPostmans(userCity,HomeActivity.this, postmanList);
            isLoginNeeded();

        } catch (Exception e) {
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
        }

    }



    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getApplicationContext().getResources().getString(R.string.PressAgainToExit), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_password_change: {
                Intent intent = new Intent(this, UpdateData.class);
                startActivity(intent);
                return true;
            }
            case R.id.action_logout: {
                logoutUser();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logoutUser() {
        sessionManager.setToken("");
        sessionManager.setTillDate("10000");
        sessionManager.setRole("");
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, 111);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_preorder_list) {

            Intent intent = new Intent(HomeActivity.this, OrderList.class);
            startActivity(intent);

        } else if (id == R.id.nav_preorder_assigned) {

            Intent intent = new Intent(HomeActivity.this, OrderListAssigned.class);
            startActivity(intent);

        }  else if (id == R.id.nav_delivery_debt) {

            Intent intent = new Intent(HomeActivity.this, DeliveryDebteds.class);
            startActivity(intent);

        }else if (id == R.id.nav_delivery_delete) {

            Intent intent = new Intent(HomeActivity.this, DeliveryList.class);
            intent.putExtra(HelperConstants.DELIVERY_OPERATION, HelperConstants.DELIVERY_DELETE);
            startActivity(intent);
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void isLoginNeeded() throws ParseException {
        if (isTokenExpired()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, 111);
        }
    }

    public boolean isTokenExpired() {
        sessionManager = new SessionManager(getApplicationContext());
        apiDate = sessionManager.getApiDate();
        if (apiDate == null || apiDate.length() <= 1)
            return true;
        long tillDate = Long.parseLong(apiDate);
        return tillDate < new Date().getTime();
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 111) {
            if (resultCode == RESULT_OK) {
                token = sessionManager.getToken();
                apiDate = sessionManager.getApiDate();
                userLogin = sessionManager.getLogin();
                userCity = sessionManager.getCity();
                userRole = sessionManager.getRole();

                PostmanHelper.getPostmans(userCity,HomeActivity.this, postmanList);
                StringData.listRegions(HomeActivity.this);
            }
        }
    }


}