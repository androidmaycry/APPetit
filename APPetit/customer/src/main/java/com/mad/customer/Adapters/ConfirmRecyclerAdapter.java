package com.mad.customer.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mad.customer.UI.Confirm;
import com.mad.customer.R;

import java.util.ArrayList;

public class ConfirmRecyclerAdapter extends RecyclerView.Adapter<ConfirmRecyclerAdapter.MyViewHolder>  {

    ArrayList<String> names;
    ArrayList<String> prices;
    ArrayList<String> quantities;
    Confirm confirm;
    LayoutInflater mInflater;


    public ConfirmRecyclerAdapter(Context context, ArrayList<String> names, ArrayList<String> prices, ArrayList<String> quantities, Confirm confirm){
        mInflater = LayoutInflater.from(context);
        this.confirm = confirm;
        this.names = names;
        this.prices = prices;
        this.quantities = quantities;
    }


    class MyViewHolder extends RecyclerView.ViewHolder{

        TextView dish_name;
        TextView dish_quant;
        TextView dish_price;
        View view_item;

        public MyViewHolder(View itemView){
            super(itemView);
            this.view_item = itemView;
            dish_name = itemView.findViewById(R.id.dish_conf_name);
            dish_price = itemView.findViewById(R.id.dish_conf_price);
            dish_quant = itemView.findViewById(R.id.dish_conf_quantity);
        }
        public View getView_item (){
            return this.view_item;
        }
    }

    @NonNull
    @Override
    public ConfirmRecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view =  mInflater.inflate(R.layout.dish_confirm_item, viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConfirmRecyclerAdapter.MyViewHolder myViewHolder, int position) {
        String name = names.get(position);
        String price = prices.get(position);
        String quantity  = quantities.get(position);
        myViewHolder.dish_name.setText(name);
        myViewHolder.dish_quant.setText(quantity);
        myViewHolder.dish_price.setText(price + " €");


    }

    @Override
    public int getItemCount() {
        return names.size();
    }


}
