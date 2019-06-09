package com.mad.customer.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mad.customer.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

import static com.mad.mylibrary.SharedClass.RESTAURATEUR_INFO;

public class OrderDetailsRecyclerAdapter extends RecyclerView.Adapter<OrderDetailsRecyclerAdapter.MyViewHolder> {
    private ArrayList<String> keys;
    private ArrayList<String> nums;
    private String key;
    LayoutInflater mInflater;


    class MyViewHolder extends RecyclerView.ViewHolder{

        TextView dish_name;
        TextView dish_quant;
        TextView dish_price;
        View view_item;

        public MyViewHolder(View itemView){
            super(itemView);
            this.view_item = itemView;
            dish_name = itemView.findViewById(R.id.orderdetail_dishname);
            dish_price = itemView.findViewById(R.id.orderdetail_price);
            dish_quant = itemView.findViewById(R.id.orderdetail_quantity);
        }
        public View getView_item (){
            return this.view_item;
        }

        public void setData(String key_dish, String num){
            Query query = FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO).child(key).child("dishes").child(key_dish);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    dish_name.setText((String)dataSnapshot.child("name").getValue());
                    dish_price.setText((new DecimalFormat("#.##")).format(dataSnapshot.child("price").getValue()).toString()+" €");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            dish_quant.setText(num);
        }
    }


    public OrderDetailsRecyclerAdapter(Context context, ArrayList<String> keys, ArrayList<String> nums, String key) {
        mInflater = LayoutInflater.from(context);
        this.keys = keys;
        this.nums = nums;
        this.key = key;
    }

    @Override
    public OrderDetailsRecyclerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view =  mInflater.inflate(R.layout.detailorder_dishitem, parent, false);
        return new OrderDetailsRecyclerAdapter.MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        holder.setData(keys.get(position), nums.get(position));

    }


    @Override
    public int getItemCount() {
        return this.keys.size();
    }
}
