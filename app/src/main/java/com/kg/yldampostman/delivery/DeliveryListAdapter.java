package com.kg.yldampostman.delivery;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kg.yldampostman.R;

import java.util.List;

/**
 * Created by ASUS on 7/4/2017.
 */

class DeliveryListAdapter extends BaseAdapter {
    private Context context;
    private List<Delivery> valueList;

    DeliveryListAdapter(List<Delivery> listValue, Context context) {
        this.context = context;
        this.valueList = listValue;
    }

    @Override
    public int getCount() {
        return this.valueList.size();
    }

    @Override
    public Object getItem(int position) {
        return this.valueList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DeliveryViewItem viewItem;
        if (convertView == null) {
            viewItem = new DeliveryViewItem();
            LayoutInflater layoutInfiater = (LayoutInflater) this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            //LayoutInflater layoutInfiater = LayoutInflater.from(context);
            convertView = layoutInfiater.inflate(R.layout.template_delivery_list, null);

            viewItem.txtNumber = (TextView) convertView.findViewById(R.id.txt_number);
            viewItem.txtSenderAddress = (TextView) convertView.findViewById(R.id.txtSenderAddress);
            viewItem.txtSenderName = (TextView) convertView.findViewById(R.id.txtSenderName);
            viewItem.txtReceiverAddress = (TextView) convertView.findViewById(R.id.txtReceiverAddress);
            viewItem.txtReceiverName = (TextView) convertView.findViewById(R.id.txtReceiverName);
            viewItem.txtAssignedPostman = (TextView) convertView.findViewById(R.id.txtAssignedPostman);
            viewItem.txtDeliveredPostman = (TextView) convertView.findViewById(R.id.txtDeliveredPostman);
            viewItem.txtAcceptedPostman = (TextView) convertView.findViewById(R.id.txtAcceptedPostman);


            viewItem.ed_id = (TextView) convertView.findViewById(R.id.ed_id);

            viewItem.ed_sCity = (TextView) convertView.findViewById(R.id.ed_sCity);
            viewItem.ed_sPhone = (TextView) convertView.findViewById(R.id.ed_sPhone);
            viewItem.ed_sCompany = (TextView) convertView.findViewById(R.id.ed_sCompany);
            viewItem.ed_sAddress = (TextView) convertView.findViewById(R.id.ed_sAddress);
            viewItem.ed_sName = (TextView) convertView.findViewById(R.id.ed_sName);
            viewItem.ed_rCity = (TextView) convertView.findViewById(R.id.ed_rCity);
            viewItem.ed_rPhone = (TextView) convertView.findViewById(R.id.ed_rPhone);
            viewItem.ed_rCompany = (TextView) convertView.findViewById(R.id.ed_rCompany);
            viewItem.ed_rAddress = (TextView) convertView.findViewById(R.id.ed_rAddress);
            viewItem.ed_rName = (TextView) convertView.findViewById(R.id.ed_rName);
            viewItem.ed_dType = (TextView) convertView.findViewById(R.id.ed_dType);
            viewItem.ed_dCost = (TextView) convertView.findViewById(R.id.ed_dCost);
            viewItem.ed_dCount = (TextView) convertView.findViewById(R.id.ed_dCount);
            viewItem.ed_diCost = (TextView) convertView.findViewById(R.id.ed_diCost);
            viewItem.ed_payment = (TextView) convertView.findViewById(R.id.ed_payment);
            viewItem.ed_dExpl = (TextView) convertView.findViewById(R.id.ed_diExpl);

            viewItem.ed_paidAmount = (TextView) convertView.findViewById(R.id.ed_paidAmount);

            viewItem.delivery_icon = (ImageView) convertView.findViewById(R.id.order_icon);
            //     viewItem.btn_Deliver = (Button) convertView.findViewById(R.id.btn_ls_Deliver);
            //    viewItem.btn_Update = (Button) convertView.findViewById(R.id.btn_ls_Update);

            convertView.setTag(viewItem);
        } else {
            viewItem = (DeliveryViewItem) convertView.getTag();
        }

        if (valueList.get(position).status.equalsIgnoreCase("1")) {
            viewItem.delivery_icon.setImageResource(R.drawable.logo);
            //   viewItem.btn_Deliver.setEnabled(false);
            //   viewItem.btn_Update.setEnabled(false);
        } else {
            viewItem.delivery_icon.setImageResource(R.drawable.dash_delivered);
            //  viewItem.btn_Deliver.setEnabled(true);
            //  viewItem.btn_Update.setEnabled(true);
        }
        String payment = valueList.get(position).ed_acceptedPerson + ", "
                + valueList.get(position).ed_dCount + "-" + valueList.get(position).ed_dType + ", "
                + (Integer.parseInt(valueList.get(position).ed_dCost) - Integer.parseInt(valueList.get(position).ed_paidAmount));

        if (valueList.get(position).ed_payment.equalsIgnoreCase("SC"))
            payment = payment + " (+) ";
        else if (valueList.get(position).ed_payment.equalsIgnoreCase("RC"))
            payment = payment + " (-) ";
        else {
            if (valueList.get(position).ed_payment.equalsIgnoreCase("SB"))
                payment = payment + " + (Банк) ";
            else if (valueList.get(position).ed_payment.equalsIgnoreCase("RB"))
                payment = payment + " - (Банк) ";
            else
                payment = payment + " (Банк) ";
        }


        if (Integer.valueOf(valueList.get(position).ed_diCost) > 0)
            payment = payment + ", " + valueList.get(position).ed_diCost;

        viewItem.txtSenderAddress.setText(valueList.get(position).sFullAddress);
        viewItem.txtSenderName.setText(valueList.get(position).sFullName);
        viewItem.txtReceiverAddress.setText(valueList.get(position).rFullAddress);
        viewItem.txtReceiverName.setText(valueList.get(position).rFullName);
        viewItem.txtAcceptedPostman.setText(payment + " (" + valueList.get(position).entrydate.substring(0, 10) + ")");
        if (valueList.get(position).ed_deliveredPerson != "null" && valueList.get(position).ed_deliveredPerson.length() > 0)
            viewItem.txtDeliveredPostman.setText(valueList.get(position).ed_deliveredPerson + "(" + valueList.get(position).deliveredDate.substring(5, 19) + ")");
        else
            viewItem.txtDeliveredPostman.setText(" ");
        viewItem.txtNumber.setText(valueList.get(position).number + ".");

        viewItem.ed_id.setText(valueList.get(position).id);

        viewItem.ed_sCity.setText(valueList.get(position).ed_sCity);
        viewItem.ed_sPhone.setText(valueList.get(position).ed_sPhone);
        viewItem.ed_sCompany.setText(valueList.get(position).ed_sCompany);
        viewItem.ed_sAddress.setText(valueList.get(position).ed_sAddress);
        viewItem.ed_sName.setText(valueList.get(position).ed_sName);

        viewItem.ed_rCity.setText(valueList.get(position).ed_rCity);
        viewItem.ed_rPhone.setText(valueList.get(position).ed_rPhone);
        viewItem.ed_rCompany.setText(valueList.get(position).ed_rCompany);
        viewItem.ed_rAddress.setText(valueList.get(position).ed_rAddress);
        viewItem.ed_rName.setText(valueList.get(position).ed_rName);

        viewItem.ed_dType.setText(valueList.get(position).ed_dType);
        viewItem.ed_dCost.setText(valueList.get(position).ed_dCost);
        viewItem.ed_dCount.setText(valueList.get(position).ed_dCount);
        viewItem.ed_diCost.setText(valueList.get(position).ed_diCost);
        viewItem.ed_payment.setText(valueList.get(position).ed_payment);
        viewItem.ed_dExpl.setText(valueList.get(position).ed_dExpl);

        String s = valueList.get(position).ed_assignedPerson;
        if (!s.equalsIgnoreCase("null") && s.length() > 0)
            viewItem.txtAssignedPostman.setText("(" + valueList.get(position).ed_assignedPerson + ") "+valueList.get(position).ed_dExpl);
        else
            viewItem.txtAssignedPostman.setText(valueList.get(position).ed_dExpl);
        viewItem.ed_paidAmount.setText(valueList.get(position).ed_paidAmount);

        /*
        viewItem.btn_Deliver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Delivery delivery = valueList.get(pos);
                Intent intentDelivery = new Intent(context, DeliveryDeliver.class);
                intentDelivery.putExtra("delivery", delivery);
                context.startActivity(intentDelivery);
            }
        });

        viewItem.btn_Update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Delivery delivery = valueList.get(pos);
                Intent intentDelivery = new Intent(context, DeliveryUpdate.class);
                intentDelivery.putExtra("delivery", delivery);
                context.startActivity(intentDelivery);
            }
        });
        */

        return convertView;
    }
}
