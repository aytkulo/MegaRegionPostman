package com.kg.mrpostman.utils;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;
import com.kg.mrpostman.HomeActivity;
import com.kg.mrpostman.R;
import com.kg.mrpostman.users.LoginActivity;

import java.text.ParseException;
import java.util.Date;

public class NetworkUtil {

    /**
     * Returns true if the Throwable is an instance of RetrofitError with an
     * http status code equals to the given one.
     */

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static void checkConnection(Context context) {
        if (!NetworkUtil.isNetworkConnected(context)) {
            MyDialog.createSimpleOkErrorDialog(context,
                    context.getString(R.string.dialog_error_title),
                    context.getString(R.string.check_internet)).show();
        }
    }

    public static boolean isTokenExpired() throws ParseException {
        try {
            if (HomeActivity.apiDate == null || HomeActivity.apiDate.length() < 1)
                return true;
            else {
                long tillDate = Long.parseLong(HomeActivity.apiDate);
                return tillDate < new Date().getTime();
            }
        } catch (Exception e) {
            return true;
        }
    }

    public static void checkHttpStatus(Context context, VolleyError error) {
        if (error instanceof AuthFailureError) {
            if (error.networkResponse.statusCode == 403) {
                Toast.makeText(context, "Бул операция үчүн уруксатыңыз жок!", Toast.LENGTH_LONG).show();
                Intent loginIntent = new Intent(context, LoginActivity.class);
                context.startActivity(loginIntent);
            } else if (error.networkResponse.statusCode == 401) {
                Toast.makeText(context, "Өзгөртүү үчүн уруксат жок. Администратор тарабынан жабылган!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Бул операция үчүн уруксатыңыз жок!", Toast.LENGTH_LONG).show();
            }

        } else {
            MyDialog.createSimpleOkErrorDialog(context,
                    context.getString(R.string.dialog_error_title),
                    context.getString(R.string.server_error)).show();
        }

    }
}
