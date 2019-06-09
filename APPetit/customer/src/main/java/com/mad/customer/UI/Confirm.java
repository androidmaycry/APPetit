package com.mad.customer.UI;

import com.mad.customer.Adapters.ConfirmRecyclerAdapter;
import com.mad.customer.Items.OrderCustomerItem;
import com.mad.customer.R;
import com.mad.mylibrary.OrderItem;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static com.mad.mylibrary.SharedClass.CUSTOMER_PATH;
import static com.mad.mylibrary.SharedClass.RESERVATION_PATH;
import static com.mad.mylibrary.SharedClass.RESTAURATEUR_INFO;
import static com.mad.mylibrary.SharedClass.ROOT_UID;
import static com.mad.mylibrary.SharedClass.STATUS_UNKNOWN;
import static com.mad.mylibrary.SharedClass.TimeOpen;
import static com.mad.mylibrary.SharedClass.orderToTrack;
import static com.mad.mylibrary.SharedClass.user;

public class Confirm extends AppCompatActivity {

    private String tot;
    private String resAddr;
    private String resName;
    private String resPhoto;
    private ArrayList<String> keys;
    private ArrayList<String> names;
    private ArrayList<String> prices;
    private ArrayList<String> nums;
    private String key;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private String desiredTime = "";
    private Button desiredTimeButton;
    private Long time;
    private boolean timeOpen_open = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getIncomingIntent();
        desiredTimeButton =  findViewById(R.id.desired_time);
        desiredTimeButton.setOnClickListener(l->{setDesiredTimeDialog();});
        findViewById(R.id.confirm_order_button).setOnClickListener(e->{
            if(desiredTime.trim().length() > 0){


                DatabaseReference myRef1 = FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO + "/" +
                        key + RESERVATION_PATH);

                //Push sul database per storico ordini utente
                DatabaseReference myRef2 = FirebaseDatabase.getInstance().getReference(CUSTOMER_PATH + "/" + ROOT_UID).child("orders");
                HashMap<String, Object> order = new HashMap<>();
                HashMap<String, Integer> dishes = new HashMap<>();
                for(String key:keys){
                    dishes.put(key, Integer.parseInt(nums.get(keys.indexOf(key))));
                }
                String keyOrder = myRef2.push().getKey(); //key dell'ordine generata
                order.put(keyOrder, new OrderCustomerItem(key,user.addr,tot, dishes,time,Long.MAX_VALUE-time,STATUS_UNKNOWN, true));
                myRef2.updateChildren(order);

                //Push sul database per ristoratore
                HashMap<String, Integer> piatti = new HashMap<>();
                for(String name:names){
                    piatti.put(name, Integer.parseInt(nums.get(name.indexOf(name))));
                }
                HashMap<String, Object> orderMap = new HashMap<>();
                orderMap.put(keyOrder, new OrderItem(ROOT_UID, user.getAddr(), tot, STATUS_UNKNOWN, piatti,time));
                myRef1.updateChildren(orderMap);

                //Aggiungo nella lista ordini da tracciare
                if(orderToTrack==null){
                    orderToTrack=new HashMap<>();
                }
                orderToTrack.put(keyOrder, STATUS_UNKNOWN);

                Toast.makeText(this, "Order confirmed", Toast.LENGTH_LONG).show();
                setResult(1);
                finish();
            }
            else
                Toast.makeText(this, "Please select desired time", Toast.LENGTH_LONG).show();
        });
    }

    private void getIncomingIntent (){
        keys = getIntent().getStringArrayListExtra("keys");
        names = getIntent().getStringArrayListExtra("names");
        prices = getIntent().getStringArrayListExtra("prices");
        nums = getIntent().getStringArrayListExtra("nums");
        key = getIntent().getStringExtra("key");
        resAddr = getIntent().getStringExtra("raddr");
        resPhoto = getIntent().getStringExtra("photo");
        resName = getIntent().getStringExtra("rname");

        recyclerView = findViewById(R.id.dish_conf_recyclerview);
        mAdapter = new ConfirmRecyclerAdapter(this, names, prices, nums, Confirm.this);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(layoutManager);

        updatePrice();
    }

    private String calcoloTotale (ArrayList<String> prices, ArrayList<String> nums){
        float tot=0;
        for(int i=0; i<prices.size(); i++){
            float price = Float.parseFloat(prices.get(i));
            float num = Float.parseFloat(nums.get(i));
            tot=tot+(price*num);
        }
        return Float.toString(tot);
    }

    private String[] setTimeValue(){
        String[] cent = new String[100];
        for(int i=0; i<100; i++){
            if(i<10) {
                cent[i] = "0" +i;
            }
            else{
                cent[i] = ""+i;
            }
        }
        return cent;
    }

    private void setDesiredTimeDialog(){
        AlertDialog openingTimeDialog = new AlertDialog.Builder(this, R.style.AppTheme_AlertDialogStyle).create();
        LayoutInflater inflater = LayoutInflater.from(Confirm.this);
        final View viewOpening = inflater.inflate(R.layout.opening_time_dialog, null);

        timeOpen_open = true;

        NumberPicker hour = viewOpening.findViewById(R.id.hour_picker);
        NumberPicker min = viewOpening.findViewById(R.id.min_picker);

        openingTimeDialog.setView(viewOpening);

        openingTimeDialog.setButton(AlertDialog.BUTTON_POSITIVE,"OK", (dialog, which) -> {
            timeOpen_open = false;

            int hourValue = hour.getValue();
            int minValue = min.getValue();

            String hourString = Integer.toString(hourValue), minString = Integer.toString(minValue);

            if(hourValue < 10)
                hourString = "0" + hourValue;
            if(minValue < 10)
                minString = "0" + minValue;

            desiredTime = hourString + ":" + minString;
            time = getDate(hourValue,minValue);
            desiredTimeButton.setText(desiredTime);
        });

        String[] hours = setTimeValue();
        hour.setDisplayedValues(hours);
        hour.setMinValue(0);
        hour.setMaxValue(23);
        hour.setValue(0);

        String[] mins = setTimeValue();
        min.setDisplayedValues(mins);
        min.setMinValue(0);
        min.setMaxValue(59);
        min.setValue(0);

        openingTimeDialog.show();
    }

    private Long getDate (int hour, int min) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
        Date date = cal.getTime();
        if (cal.before(Calendar.getInstance())){
            cal.set(Calendar.DATE,cal.get(Calendar.DATE)+1);
            date = cal.getTime();
        }
        return date.getTime();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {

            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();

    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putBoolean(TimeOpen, timeOpen_open);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState.getBoolean(TimeOpen))
            setDesiredTimeDialog();
    }

    public void updatePrice (){
        tot =calcoloTotale(prices, nums);
        TextView totale = findViewById(R.id.totale);
        totale.setText(tot + " €");
    }

}

