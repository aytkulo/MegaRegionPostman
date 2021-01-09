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

            viewItem.txtNumber =  convertView.findViewById(R.id.txt_number);
            viewItem.txtSenderAddress =  convertView.findViewById(R.id.txtSenderAddress);
            viewItem.txtSenderName =  convertView.findViewById(R.id.txtSenderName);
            viewItem.txtReceiverAddress =  convertView.findViewById(R.id.txtReceiverAddress);
            viewItem.txtReceiverName =  convertView.findViewById(R.id.txtReceiverName);
            viewItem.txtAssignedPostman =  convertView.findViewById(R.id.txtAssignedPostman);
            viewItem.txtDeliveredPostman =  convertView.findViewById(R.id.txtDeliveredPostman);
            viewItem.txtAcceptedPostman =  convertView.findViewById(R.id.txtAcceptedPostman);


            viewItem.ed_id =  convertView.findViewById(R.id.ed_id);

            viewItem.ed_sCity =  convertView.findViewById(R.id.ed_sCity);
            viewItem.ed_sPhone =  convertView.findViewById(R.id.ed_sPhone);
            viewItem.ed_sCompany =  convertView.findViewById(R.id.ed_sCompany);
            viewItem.ed_sAddress =  convertView.findViewById(R.id.ed_sAddress);
            viewItem.ed_sName =  convertView.findViewById(R.id.ed_sName);
            viewItem.ed_rCity =  convertView.findViewById(R.id.ed_rCity);
            viewItem.ed_rPhone =  convertView.findViewById(R.id.ed_rPhone);
            viewItem.ed_rCompany =  convertView.findViewById(R.id.ed_rCompany);
            viewItem.ed_rAddress =  convertView.findViewById(R.id.ed_rAddress);
            viewItem.ed_rName =  convertView.findViewById(R.id.ed_rName);
            viewItem.ed_dType =  convertView.findViewById(R.id.ed_dType);
            viewItem.ed_dCost =  convertView.findViewById(R.id.ed_dCost);
            viewItem.ed_dCount =  convertView.findViewById(R.id.ed_dCount);
            viewItem.ed_diCost =  convertView.findViewById(R.id.ed_diCost);
            viewItem.ed_payment =  convertView.findViewById(R.id.ed_payment);
            viewItem.ed_dExpl =  convertView.findViewById(R.id.ed_diExpl);

            viewItem.ed_paidAmount =  convertView.findViewById(R.id.ed_paidAmount);

            viewItem.delivery_icon =  convertView.findViewById(R.id.order_icon);
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
        String payment = valueList.get(position).acceptedPerson + ", "
                + valueList.get(position).deliveryCount + "-" + valueList.get(position).deliveryType.substring(0, 3) + ", "
                + (Integer.parseInt(valueList.get(position).deliveryCost) - Integer.parseInt(valueList.get(position).paidAmount));

        if (valueList.get(position).paymentType.equalsIgnoreCase("SC"))
            payment = payment + " (+) ";
        else if (valueList.get(position).paymentType.equalsIgnoreCase("RC"))
            payment = payment + " (-) ";
        else {
            if (valueList.get(position).paymentType.equalsIgnoreCase("SB"))
                payment = payment + " + (Банк) ";
            else if (valueList.get(position).paymentType.equalsIgnoreCase("RB"))
                payment = payment + " - (Банк) ";
            else
                payment = payment + " (Банк) ";
        }


        if (Integer.valueOf(valueList.get(position).deliveryiCost) > 0)
            payment = payment + ", " + valueList.get(position).deliveryiCost;

        viewItem.txtSenderAddress.setText(valueList.get(position).sFullAddress);
        viewItem.txtSenderName.setText(valueList.get(position).sFullName);
        viewItem.txtReceiverAddress.setText(valueList.get(position).rFullAddress);
        viewItem.txtReceiverName.setText(valueList.get(position).rFullName);
        viewItem.txtAcceptedPostman.setText(payment + " (" + valueList.get(position).entryDate.substring(0, 16) + ")");


        String deliveredPaid = "";
        if (valueList.get(position).deliveredDate != null && valueList.get(position).deliveredDate.length() > 1) {
            deliveredPaid = valueList.get(position).deliveredPerson + " (" + valueList.get(position).deliveredDate.substring(5, 16) + ")";
        }

        if (valueList.get(position).costPaidDate != null && valueList.get(position).costPaidDate.length() > 1) {
            deliveredPaid =deliveredPaid + ", "+ valueList.get(position).costPaidUser + " (" + valueList.get(position).costPaidDate.substring(5, 16) + ")";
        }

        viewItem.txtDeliveredPostman.setText(deliveredPaid);

        viewItem.txtNumber.setText(valueList.get(position).number + ".");

        viewItem.ed_id.setText(valueList.get(position).deliveryId);

        viewItem.ed_sCity.setText(valueList.get(position).senderCity);
        viewItem.ed_sPhone.setText(valueList.get(position).senderPhone);
        viewItem.ed_sCompany.setText(valueList.get(position).senderCompany);
        viewItem.ed_sAddress.setText(valueList.get(position).senderAddress);
        viewItem.ed_sName.setText(valueList.get(position).senderName);

        viewItem.ed_rCity.setText(valueList.get(position).receiverCity);
        viewItem.ed_rPhone.setText(valueList.get(position).receiverPhone);
        viewItem.ed_rCompany.setText(valueList.get(position).receiverCompany);
        viewItem.ed_rAddress.setText(valueList.get(position).receiverAddress);
        viewItem.ed_rName.setText(valueList.get(position).receiverName);

        viewItem.ed_dType.setText(valueList.get(position).deliveryType);
        viewItem.ed_dCost.setText(valueList.get(position).deliveryCost);
        viewItem.ed_dCount.setText(valueList.get(position).deliveryCount);
        viewItem.ed_diCost.setText(valueList.get(position).deliveryiCost);
        viewItem.ed_payment.setText(valueList.get(position).paymentType);
        viewItem.ed_dExpl.setText(valueList.get(position).deliveryExplanation);

        String s = valueList.get(position).assignedSector;
        if (!s.equalsIgnoreCase("null") && s.length() > 0)
            viewItem.txtAssignedPostman.setText("(" + valueList.get(position).assignedSector + ") "+valueList.get(position).deliveryExplanation);
        else
            viewItem.txtAssignedPostman.setText(valueList.get(position).deliveryExplanation);
        viewItem.ed_paidAmount.setText(valueList.get(position).paidAmount);

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
