package com.kg.yldampostman.delivery;

import java.io.Serializable;

/**
 * Created by ASUS on 7/4/2017.
 */

public class Delivery implements Serializable {

    public String sFullName;
    public String sFullAddress;
    public String rFullName;
    public String rFullAddress;

    public int number;
    public String id;
    public String ed_sCity;
    public String ed_sCompany;
    public String ed_sPhone;
    public String ed_sName;
    public String ed_sAddress;
    public String ed_rCity;
    public String ed_rCompany;
    public String ed_rPhone;
    public String ed_rName;
    public String ed_rAddress;

    public String ed_dType;
    public String ed_dCount;
    public String ed_dCost;
    public String ed_diCost;
    public String ed_dExpl;
    public String ed_payment;
    public String ed_assignedPerson;
    public String ed_deliveredPerson;
    public String ed_acceptedPerson;
    public String ed_buytype;

    public String ed_paidAmount;

    public String entrydate;
    public String deliveredDate;
    public String status;
    public boolean checked;
}
