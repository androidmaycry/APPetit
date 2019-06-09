package com.mad.customer.UI;

import static com.mad.mylibrary.SharedClass.CUSTOMER_PATH;
import static com.mad.mylibrary.SharedClass.RESTAURATEUR_INFO;
import static com.mad.mylibrary.SharedClass.STATUS_DELIVERED;
import static com.mad.mylibrary.SharedClass.STATUS_DELIVERING;
import static com.mad.mylibrary.SharedClass.STATUS_DISCARDED;
import static com.mad.mylibrary.SharedClass.STATUS_UNKNOWN;
import static com.mad.mylibrary.SharedClass.orderToTrack;
import static com.mad.mylibrary.SharedClass.orderToTrack;
import static com.mad.mylibrary.SharedClass.user;
import static com.mad.mylibrary.SharedClass.ROOT_UID;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hsalf.smilerating.SmileRating;
import com.mad.customer.R;
import com.mad.mylibrary.ReviewItem;
import com.mad.mylibrary.StarItem;
import com.mad.mylibrary.User;

import java.util.HashMap;



public class NavApp extends AppCompatActivity implements
        Restaurant.OnFragmentInteractionListener,
        Profile.OnFragmentInteractionListener,
        Order.OnFragmentInteractionListener{

    public static final String PREFERENCE_NAME = "ORDER_LIST";

    private SharedPreferences order_to_listen;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item ->  {

        switch (item.getItemId()) {
            case R.id.navigation_home:
                //onRefuseOrder();
                if(!(getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof Restaurant)) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Restaurant()).commit();
                }
                return true;
            case R.id.navigation_profile:
                //onRefuseOrder();
                if(!(getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof Profile)) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Profile()).commit();
                }
                return true;
            case R.id.navigation_reservation:
                //onRefuseOrder();
                if(!(getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof Order)) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Order()).commit();
                }
                return true;
        }
        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_nav_app);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Toolbar toolbar = findViewById(R.id.toolbar);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new Restaurant()).commit();
        }
        //Get the hashMap from sharedPreferences

        order_to_listen = this.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String storedHashMapString = order_to_listen.getString("HashMap",null);
        java.lang.reflect.Type type = new TypeToken<HashMap<String, Integer>>(){}.getType();
        orderToTrack = gson.fromJson(storedHashMapString, type);


        getUserInfo();


    }

    public void getUserInfo() {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(CUSTOMER_PATH).child(ROOT_UID);

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.child("customer_info").getValue(User.class);
                Log.d("user", ""+user);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("MAIN", "Failed to read value.", error.toException());
            }
        });

    }

    private void onRefuseOrder (){

        if(orderToTrack!=null){
            for(HashMap.Entry<String, Integer> entry : orderToTrack.entrySet()){
                Query query = FirebaseDatabase.getInstance().getReference(CUSTOMER_PATH).child(ROOT_UID).child("orders").child(entry.getKey());
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Long changed_statusi = (Long) dataSnapshot.child("status").getValue();
                            Integer changed_status = changed_statusi.intValue();
                            if(!changed_status.equals(entry.getValue())) {
                                if (changed_status == STATUS_DISCARDED) {
                                    entry.setValue(changed_status);
                                    orderToTrack.replace(entry.getKey(), changed_status);
                                    showAlertDialogDiscarded((String)dataSnapshot.child("key").getValue(), dataSnapshot.getKey());
                                }
                                else if (changed_status==STATUS_DELIVERING){
                                    entry.setValue(changed_status);
                                    orderToTrack.replace(entry.getKey(), changed_status);

                                }
                                else if (changed_status==STATUS_DELIVERED){
                                    entry.setValue(changed_status);
                                    orderToTrack.replace(entry.getKey(), changed_status);
                                    setRated(dataSnapshot.getKey(), false);
                                    showAlertDialogDelivered((String)dataSnapshot.child("key").getValue(), dataSnapshot.getKey());
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    private void showAlertDialogDiscarded (String resKey, String orderKey){
        Query query = FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO).child(resKey).child("info");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                AlertDialog alertDialog = new AlertDialog.Builder(NavApp.this).create();
                LayoutInflater factory = LayoutInflater.from(NavApp.this);
                final View view = factory.inflate(R.layout.discarded_dialog, null);

                alertDialog.setView(view);
                if(dataSnapshot.child("photoUri").exists()){
                    Glide.with(view).load(dataSnapshot.child("photoUri").getValue()).into((ImageView) view.findViewById(R.id.dialog_discarded_rating_icon));
                }
                ((TextView)view.findViewById(R.id.discarded_res_name)).setText((String)dataSnapshot.child("name").getValue());
                alertDialog.show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setRated (String orderKey, boolean bool) {
        DatabaseReference myRef2 = FirebaseDatabase.getInstance().getReference(CUSTOMER_PATH).child(ROOT_UID).child("orders").child(orderKey);
        HashMap <String, Object> rated = new HashMap<>();
        rated.put("rated", bool);
        myRef2.updateChildren(rated);
    }

    private void showAlertDialogDelivered (String resKey, String orderKey){
        Query query = FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO).child(resKey).child("info");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                AlertDialog alertDialog = new AlertDialog.Builder(NavApp.this).create();
                LayoutInflater factory = LayoutInflater.from(NavApp.this);
                final View view = factory.inflate(R.layout.rating_dialog, null);

                alertDialog.setView(view);
                if(dataSnapshot.child("photoUri").exists()){
                    Glide.with(view).load(dataSnapshot.child("photoUri").getValue()).into((ImageView) view.findViewById(R.id.dialog_rating_icon));
                }
                SmileRating smileRating = (SmileRating) view.findViewById(R.id.dialog_rating_rating_bar);
                //Button confirm pressed
                view.findViewById(R.id.dialog_rating_button_positive).setOnClickListener(a->{
                    if(smileRating.getRating()!=0) {
                        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO + "/" + resKey).child("review");
                        HashMap<String, Object> review = new HashMap<>();
                        String comment = ((EditText)view.findViewById(R.id.dialog_rating_feedback)).getText().toString();
                        String rate_key = myRef.push().getKey();
                        updateRestaurantStars(resKey, smileRating.getRating());
                        if(!comment.isEmpty()){
                            setRated(orderKey, true);
                            review.put(rate_key, new ReviewItem(smileRating.getRating(), comment, ROOT_UID, user.getPhotoPath(), user.getName()));
                            myRef.updateChildren(review);
                        }
                        else{
                            setRated(orderKey, true);
                            review.put(rate_key, new ReviewItem(smileRating.getRating(), null, ROOT_UID, user.getPhotoPath(), user.getName()));
                            myRef.updateChildren(review);
                        }
                        Toast.makeText(getApplicationContext(), "Thanks for your review!", Toast.LENGTH_LONG).show();
                        alertDialog.dismiss();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "You forgot to rate!", Toast.LENGTH_LONG).show();
                    }
                });
                view.findViewById(R.id.dialog_rating_button_negative).setOnClickListener(b->{
                    alertDialog.dismiss();
            });
                alertDialog.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateRestaurantStars (String resKey, int stars) {
        Query query = FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO).child(resKey).child("stars");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> star = new HashMap<>();
                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO + "/" + resKey);
                if(dataSnapshot.exists()){
                    int s = ((Long)dataSnapshot.child("tot_stars").getValue()).intValue();
                    int p = ((Long)dataSnapshot.child("tot_review").getValue()).intValue();
                    star.put("stars", new StarItem(s+stars, p+1, -s-stars));
                    myRef.updateChildren(star);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        onRefuseOrder();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        Gson gson = new Gson();
        order_to_listen = this.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        String mapString = gson.toJson(orderToTrack);
        order_to_listen.edit().putString("HashMap", mapString).apply();
        super.onStop();
    }
}

