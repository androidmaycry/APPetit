package com.mad.customer.UI;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mad.customer.Adapters.OrderDetailsRecyclerAdapter;
import com.mad.customer.Items.OrderCustomerItem;
import com.mad.customer.R;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static com.mad.mylibrary.SharedClass.*;




public class OrderDetails extends AppCompatActivity {

    private OrderCustomerItem item;
    private RecyclerView recyclerView;
    private OrderDetailsRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<String> keys = new ArrayList<>();
    private ArrayList<String> nums = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);
        item = (OrderCustomerItem) getIntent().getSerializableExtra("order_item");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        insertItems ();
        insertRecyclerView();


    }
    private void insertItems (){
        //Set time in correct format
        Date d = new Date(item.getTime());
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DATE);
        String date = day+"/"+month+"/"+year;
        TextView twdate = findViewById(R.id.order_det_date);
        twdate.setText(date);
        //Set restaurant Name and image
        Query query = FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO).child(item.getKey()).child("info");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ((TextView)findViewById(R.id.order_res_det_name)).setText((String)dataSnapshot.child("name").getValue());
                ((TextView)findViewById(R.id.order_det_name)).setText((String)dataSnapshot.child("name").getValue());
                ((TextView)findViewById(R.id.order_det_addr)).setText((String)dataSnapshot.child("addr").getValue());
                ((TextView)findViewById(R.id.order_det_cell)).setText((String)dataSnapshot.child("phone").getValue());
                if(dataSnapshot.child("photoUri").exists()){
                    Glide.with(getApplicationContext()).load(dataSnapshot.child("photoUri").getValue()).into((ImageView)findViewById(R.id.order_det_image));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        //Set status
        TextView tw_orrder_status = ((TextView)findViewById(R.id.order_det_status));
        switch (item.getStatus()){
            case STATUS_UNKNOWN:
                tw_orrder_status.setText("Order sent");
                break;
            case STATUS_DELIVERED:
                tw_orrder_status.setText("Order delivered");
                tw_orrder_status.setTextColor(Color.parseColor("#59cc33"));
                break;
            case STATUS_DISCARDED:
                tw_orrder_status.setText("Order refused");
                tw_orrder_status.setTextColor(Color.parseColor("#cc3333"));
                break;
            case STATUS_DELIVERING:
                tw_orrder_status.setText("Delivering...");
                tw_orrder_status.setTextColor(Color.parseColor("#ffb847"));
                break;
        }
        //Set total
        ((TextView)findViewById(R.id.order_det_tot1)).setText(item.getTotPrice()+" €");
        ((TextView)findViewById(R.id.order_det_tot)).setText(item.getTotPrice()+" €");
        //Set customer addr
        ((TextView)findViewById(R.id.order_det_deladdr)).setText(item.getAddrCustomer());
        //Set hour
        int hourValue = c.get(Calendar.HOUR);
        int minValue =c.get(Calendar.MINUTE);
        String hourString = Integer.toString(hourValue), minString = Integer.toString(minValue);
        if(hourValue < 10)
            hourString = "0" + hourValue;
        if(minValue < 10)
            minString = "0" + minValue;
        String orario = hourString + ":" + minString;
        ((TextView)findViewById(R.id.order_det_hour)).setText(orario);

    }
    private void insertRecyclerView(){
        recyclerView = findViewById(R.id.order_det_recyclerview);
        HashMap<String, Integer> knv = item.getDishes();

        knv.forEach((key, val)->{
            keys.add(key);
            nums.add(val.toString());

        });
        mAdapter = new OrderDetailsRecyclerAdapter(this, keys, nums,item.getKey());
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }


}
