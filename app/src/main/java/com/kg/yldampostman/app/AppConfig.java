package com.kg.yldampostman.app;

/**
 * Created by ASUS on 6/22/2017.
 */
public class AppConfig {

    public static String PURE_URL = "http://10.0.2.2:8080/";

   //  public static String PURE_URL = "http://yldam.nova.kg/yldam-api/";

    public static String URL_DELIVERY_ENTRY = PURE_URL + "delivery/save";

    public static String URL_DELIVERY_GET = PURE_URL + "delivery/get";

    public static String URL_DELIVERY_LIST = PURE_URL + "delivery/list";

    public static String URL_DELIVERY_LIST_WITH_DEBTS = PURE_URL + "delivery/list/debt";

    public static String URL_DELIVERY_PAY_DEBT = PURE_URL + "delivery/payCost";

    public static String URL_DELIVERY_UPDATE = PURE_URL + "delivery/update";

    public static String URL_DELIVERY_DELIVER = PURE_URL + "delivery/deliver";

    public static String URL_DELIVERY_DELETE = PURE_URL + "delivery/delete";

    public static String URL_DELIVERY_ASSIGN = PURE_URL + "delivery/assign";

    public static String URL_CORPORATE_CUSTOMER_LIST = PURE_URL + "customers/corporate/list";

    public static String URL_CORPORATE_CUSTOMER_SAVE = PURE_URL + "customers/corporate/save";

    public static String URL_CORPORATE_CUSTOMER_CHECK = PURE_URL + "customers/corporate/check";

    public static String URL_CUSTOMER_UPDATE = PURE_URL + "customer/retail/update";

    public static String URL_CUSTOMER_GET = PURE_URL + "customers/retail/list";

    public static String URL_CUSTOMER_SAVE = PURE_URL + "customers/retail/save";

    public static String URL_CORPORATE_CUSTOMER_UPDATE = PURE_URL + "customers/corporate/update";

    public static String URL_CUSTOMER_DELETE = PURE_URL + "customers/delete";


    public static String URL_GET_USER_PERMISSION = PURE_URL + "users/getpermission";

    public static String URL_LOGIN = PURE_URL + "users/login";

    public static String URL_GET_USERS = PURE_URL + "users/list";

    public static String URL_UPDATE_PSW = PURE_URL + "users/update";


    public static String URL_GET_SECTORS = PURE_URL + "reports/sectors";

    public static String URL_ORDER_SAVE = PURE_URL + "order/save";

    public static String URL_ORDER_GET = PURE_URL + "order/list";

    public static String URL_ORDER_UPDATE_ACCEPT = PURE_URL + "order/accept";


    public static String URL_SAVE_LOCATION = PURE_URL + "reports/save/location";

    public static String URL_GET_LAST_LOCATION = PURE_URL + "location_get_last.php";


    // global topic to receive app wide push notifications
    public static final String TOPIC_GLOBAL = "global";

    // broadcast receiver intent filters
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOTIFICATION = "pushNotification";

    // id to handle the notification in the notification tray
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;

    public static final String SHARED_PREF = "firebase";
}

